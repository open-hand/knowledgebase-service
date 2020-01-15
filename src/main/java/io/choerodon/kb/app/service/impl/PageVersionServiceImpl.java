package io.choerodon.kb.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import difflib.Delta;
import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.vo.*;
import io.choerodon.kb.app.service.*;
import io.choerodon.kb.infra.dto.*;
import io.choerodon.kb.infra.feign.BaseFeignClient;
import io.choerodon.kb.infra.feign.vo.UserDO;
import io.choerodon.kb.infra.mapper.PageContentMapper;
import io.choerodon.kb.infra.mapper.PageVersionMapper;
import io.choerodon.kb.infra.repository.PageRepository;
import io.choerodon.kb.infra.utils.Version;
import io.choerodon.kb.infra.utils.commonmark.TextContentRenderer;
import io.choerodon.kb.infra.utils.diff.DiffUtil;
import io.choerodon.kb.infra.utils.diff.MyersDiff;
import io.choerodon.kb.infra.utils.diff.PathNode;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author shinan.chen
 * @since 2019/5/16
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class PageVersionServiceImpl implements PageVersionService {

    private static final String ERROR_PAGEVERSION_ILLEGAL = "error.pageVersion.illegal";
    private static final String ERROR_PAGEVERSION_CREATE = "error.pageVersion.create";
    private static final String ERROR_PAGEVERSION_DELETE = "error.pageVersion.delete";
    private static final String ERROR_PAGEVERSION_NOTFOUND = "error.pageVersion.notFound";
    private static final String ERROR_PAGEVERSION_UPDATE = "error.pageVersion.update";

    @Autowired
    private PageVersionMapper pageVersionMapper;
    @Autowired
    private PageContentMapper pageContentMapper;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private PageContentService pageContentService;
    @Autowired
    private PageVersionService pageVersionService;
    @Autowired
    private BaseFeignClient baseFeignClient;
    @Autowired
    private PageService pageService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private WorkSpaceService workSpaceService;
    @Autowired
    private WorkSpacePageService workSpacePageService;

    public void setBaseFeignClient(BaseFeignClient baseFeignClient) {
        this.baseFeignClient = baseFeignClient;
    }

    @Override
    public PageVersionDTO baseCreate(PageVersionDTO create) {
        if (pageVersionMapper.insert(create) != 1) {
            throw new CommonException(ERROR_PAGEVERSION_CREATE);
        }
        return pageVersionMapper.selectByPrimaryKey(create.getId());
    }

    @Override
    public void baseDelete(Long versionId) {
        if (pageVersionMapper.deleteByPrimaryKey(versionId) != 1) {
            throw new CommonException(ERROR_PAGEVERSION_DELETE);
        }
    }

    @Override
    public void baseUpdate(PageVersionDTO update) {
        if (pageVersionMapper.updateByPrimaryKeySelective(update) != 1) {
            throw new CommonException(ERROR_PAGEVERSION_UPDATE);
        }
    }

    @Override
    public PageVersionDTO queryByVersionId(Long versionId, Long pageId) {
        PageVersionDTO version = pageVersionMapper.selectByPrimaryKey(versionId);
        if (version == null) {
            throw new CommonException(ERROR_PAGEVERSION_NOTFOUND);
        }
        if (!version.getPageId().equals(pageId)) {
            throw new CommonException(ERROR_PAGEVERSION_ILLEGAL);
        }
        return version;
    }

    @Override
    public List<PageVersionVO> queryByPageId(Long organizationId, Long projectId, Long pageId) {
        pageRepository.checkById(organizationId, projectId, pageId);
        List<PageVersionDTO> versionDOS = pageVersionMapper.queryByPageId(pageId);
        List<Long> userIds = versionDOS.stream().map(PageVersionDTO::getCreatedBy).distinct().collect(Collectors.toList());
        List<UserDO> userDOList = baseFeignClient.listUsersByIds(userIds.toArray(new Long[userIds.size()]), false).getBody();
        Map<Long, UserDO> userDOMap = userDOList.stream().collect(Collectors.toMap(UserDO::getId, x -> x));
        //去除第一个版本
        versionDOS.remove(versionDOS.size() - 1);
        List<PageVersionVO> vos = modelMapper.map(versionDOS, new TypeToken<List<PageVersionVO>>() {
        }.getType());
        for (PageVersionVO vo : vos) {
            vo.setCreateUser(userDOMap.get(vo.getCreatedBy()));
        }
        return vos;
    }

    @Override
    public Long createVersionAndContent(Long pageId, String title, String content, Long oldVersionId, Boolean isFirstVersion, Boolean isMinorEdit) {
        String versionName;
        if (Boolean.TRUE.equals(isFirstVersion)) {
            versionName = Version.firstVersion;
        } else {
            String oldVersionName = this.queryByVersionId(oldVersionId, pageId).getName();
            versionName = incrementVersion(oldVersionName, isMinorEdit);
        }
        //创建一个新版本
        PageVersionDTO create = new PageVersionDTO();
        create.setName(versionName);
        create.setPageId(pageId);
        this.baseCreate(create);
        Long latestVersionId = create.getId();
        //创建内容
        PageContentDTO pageContent = new PageContentDTO();
        pageContent.setPageId(pageId);
        pageContent.setVersionId(latestVersionId);
        pageContent.setContent(content);
        pageContent.setTitle(title);
        pageContentService.baseCreate(pageContent);
        if (Boolean.FALSE.equals(isFirstVersion)) {
            //更新上个版本内容为diff
            PageContentDTO lastContent = pageContentService.selectByVersionId(oldVersionId, pageId);
            TextDiffVO diffVO = DiffUtil.diff(lastContent.getContent(), content);
            lastContent.setContent(JSONObject.toJSONString(diffVO));
            pageContentService.baseUpdateOptions(lastContent, "content");
        }
        //删除这篇文章当前用户的草稿
        PageDTO select = pageRepository.selectById(pageId);
        pageService.deleteDraftContent(select.getOrganizationId(), select.getProjectId(), pageId);
        return latestVersionId;
    }

    private String incrementVersion(String versionName, Boolean isMinorEdit) {
        if (Boolean.TRUE.equals(isMinorEdit)) {
            return new Version(versionName).next().toString();
        } else {
            return new Version(versionName).getBranchPoint().next().newBranch(1).toString();
        }
    }

    @Override
    public PageVersionInfoVO queryById(Long organizationId, Long projectId, Long pageId, Long versionId) {
        PageDTO pageDTO = pageRepository.baseQueryById(organizationId, projectId, pageId);
        Long latestVersionId = pageDTO.getLatestVersionId();
        PageVersionInfoVO pageVersion = modelMapper.map(this.queryByVersionId(versionId, pageId), PageVersionInfoVO.class);
        //若是最新版本直接返回
        if (versionId.equals(latestVersionId)) {
            PageContentDTO pageContent = pageContentService.selectByVersionId(latestVersionId, pageId);
            pageVersion.setContent(pageContent.getContent());
        }
        List<PageContentDTO> pageContents = pageContentMapper.queryByPageId(pageId);
        //判断正序还是倒序更快速解析
        if (pageContents.get(pageContents.size() / 2).getVersionId() > versionId) {
            //正序解析
            List<TextDiffVO> diffs = pageContents.stream().filter(content -> content.getVersionId() < versionId).map(
                    content -> TextDiffVO.jsonToVO(JSON.parseObject(content.getContent()))).collect(Collectors.toList());
            pageVersion.setContent(DiffUtil.parseObverse(diffs));
        } else {
            //倒序解析
            List<TextDiffVO> diffs = pageContents.stream().filter(content -> (content.getVersionId() >= versionId) && !latestVersionId.equals(content.getVersionId())).map(
                    content -> TextDiffVO.jsonToVO(JSON.parseObject(content.getContent()))).collect(Collectors.toList());
            PageContentDTO pageContent = pageContentService.selectByVersionId(latestVersionId, pageId);
            pageVersion.setContent(DiffUtil.parseReverse(diffs, pageContent.getContent()));
        }
        pageVersion.setTitle(pageContentService.selectByVersionId(versionId, pageId).getTitle());
        return pageVersion;
    }

    @Override
    public PageVersionCompareVO compareVersion(Long organizationId, Long projectId, Long pageId, Long firstVersionId, Long secondVersionId) {
        //规定secondVersionId必须大于firstVersionId
        if (secondVersionId < firstVersionId) {
            Long temp = firstVersionId;
            firstVersionId = secondVersionId;
            secondVersionId = temp;
        }
        PageVersionInfoVO firstVersion = queryById(organizationId, projectId, pageId, firstVersionId);
        PageVersionInfoVO secondVersion = queryById(organizationId, projectId, pageId, secondVersionId);
        PageVersionCompareVO compareVO = new PageVersionCompareVO();
        Parser parser = Parser.builder().build();
        TextContentRenderer textContentRenderer = TextContentRenderer.builder().build();
        Node firstDocument = parser.parse(firstVersion.getContent().replaceAll("<br>", "\n"));
        Node secondDocument = parser.parse(secondVersion.getContent().replaceAll("<br>", "\n"));
        compareVO.setFirstVersionContent(textContentRenderer.render(firstDocument));
        compareVO.setSecondVersionContent(textContentRenderer.render(secondDocument));
        TextDiffVO diffVO = DiffUtil.diff(compareVO.getFirstVersionContent(), compareVO.getSecondVersionContent());
        handleContentDiff(compareVO, diffVO);
        handleTitleDiff(compareVO, firstVersion.getTitle(), secondVersion.getTitle());
        return compareVO;
    }

    private void handleTitleDiff(PageVersionCompareVO compareVO, String firstTitle, String secondTitle) {
        List<String> ori = char2String(firstTitle.toCharArray());
        List<String> rev = char2String(secondTitle.toCharArray());
        MyersDiff myersDiff = new MyersDiff<String>();
        PathNode pathNode = null;
        try {
            pathNode = myersDiff.buildPath(ori, rev);
        } catch (Exception e) {
            e.printStackTrace();
        }
        compareVO.setDiffTitle(myersDiff.buildDiff(pathNode, ori, rev, firstTitle, secondTitle));
    }

    private List<String> char2String(char[] chars) {
        List<String> strs = new ArrayList<>(chars.length);
        for (char c : chars) {
            strs.add(String.valueOf(c));
        }
        return strs;
    }

    /**
     * 处理diff为差异文本显示在页面上
     *
     * @param compareVO
     * @param diffVO
     */
    private void handleContentDiff(PageVersionCompareVO compareVO, TextDiffVO diffVO) {
        List<String> sourceList = DiffUtil.textToLines(compareVO.getFirstVersionContent());
        List<String> targetList = DiffUtil.textToLines(compareVO.getSecondVersionContent());
        Map<Integer, DiffHandleVO> diffMap = new HashMap<>(targetList.size());
        //处理删除
        for (Delta<String> delta : diffVO.getDeleteData()) {
            diffMap.put(delta.getRevised().getPosition(), new DiffHandleVO.Builder().delete(delta.getOriginal().getLines()).build());
        }
        //处理增加
        for (Delta<String> delta : diffVO.getInsertData()) {
            diffMap.put(delta.getRevised().getPosition(), new DiffHandleVO.Builder().insert(delta.getRevised().getLines()).build());
        }
        //处理改变
        for (Delta<String> delta : diffVO.getChangeData()) {
            diffMap.put(delta.getRevised().getPosition(), new DiffHandleVO.Builder().change(delta.getOriginal().getLines(), delta.getRevised().getLines()).build());
        }
        //生成diffContent内容
        List<String> diffList = new ArrayList<>(targetList.size() + sourceList.size());
        //循环结束控制不放在这里，因为若最后是删除时，会被跳过，因此循环控制放在删除中
        for (int i = 0; ; ) {
            DiffHandleVO handleVO = diffMap.get(i);
            if (handleVO == null) {
                //循环i大于目标数组的大小时退出循环
                if (i >= targetList.size()) {
                    break;
                }
                diffList.add(targetList.get(i));
                i++;
            } else {
                switch (handleVO.getType()) {
                    case DELETE:
                        diffList.addAll(handleVO.getStrs());
                        if (i < targetList.size()) {
                            diffList.add(targetList.get(i));
                        }
                        i++;
                        break;
                    case INSERT:
                        diffList.addAll(handleVO.getStrs());
                        i += handleVO.getSkipLine();
                        break;
                    case CHANGE:
                        diffList.addAll(handleVO.getStrs());
                        i += handleVO.getSkipLine();
                        break;
                }
            }
        }
        compareVO.setDiffContent(diffList.stream().collect(Collectors.joining("<br>")));
    }

    @Override
    public void rollbackVersion(Long organizationId, Long projectId, Long pageId, Long versionId) {
        PageVersionInfoVO versionInfo = queryById(organizationId, projectId, pageId, versionId);
        PageDTO pageDTO = pageRepository.baseQueryById(organizationId, projectId, pageId);
        //更新标题
        pageDTO.setTitle(versionInfo.getTitle());
        WorkSpacePageDTO workSpacePageDTO = workSpacePageService.selectByPageId(pageId);
        WorkSpaceDTO workSpaceDTO = workSpaceService.baseQueryById(organizationId, projectId, workSpacePageDTO.getWorkspaceId());
        workSpaceDTO.setName(versionInfo.getTitle());
        workSpaceService.baseUpdate(workSpaceDTO);

        Long latestVersionId = pageVersionService.createVersionAndContent(pageDTO.getId(), versionInfo.getTitle(), versionInfo.getContent(), pageDTO.getLatestVersionId(), false, false);
        pageDTO.setLatestVersionId(latestVersionId);
        pageRepository.baseUpdate(pageDTO, true);
    }
}
