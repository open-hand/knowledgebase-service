package io.choerodon.kb.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import difflib.Delta;
import io.choerodon.kb.api.dao.*;
import io.choerodon.kb.app.service.PageVersionService;
import io.choerodon.kb.domain.kb.repository.PageContentRepository;
import io.choerodon.kb.domain.kb.repository.PageRepository;
import io.choerodon.kb.domain.kb.repository.PageVersionRepository;
import io.choerodon.kb.infra.common.utils.Markdown2HtmlUtil;
import io.choerodon.kb.infra.common.utils.Version;
import io.choerodon.kb.infra.common.utils.diff.DiffUtil;
import io.choerodon.kb.infra.dataobject.PageContentDO;
import io.choerodon.kb.infra.dataobject.PageDO;
import io.choerodon.kb.infra.dataobject.PageVersionDO;
import io.choerodon.kb.infra.mapper.PageContentMapper;
import io.choerodon.kb.infra.mapper.PageVersionMapper;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
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
    @Autowired
    private PageVersionMapper pageVersionMapper;
    @Autowired
    private PageContentMapper pageContentMapper;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private PageVersionRepository pageVersionRepository;
    @Autowired
    private PageContentRepository pageContentRepository;
    @Autowired
    private PageVersionService pageVersionService;

    private ModelMapper modelMapper = new ModelMapper();

    @PostConstruct
    public void init() {
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    @Override
    public List<PageVersionDTO> queryByPageId(Long organizationId, Long projectId, Long pageId) {
        pageRepository.checkById(organizationId, projectId, pageId);
        List<PageVersionDO> versionDOS = pageVersionMapper.queryByPageId(pageId);
        //去除第一个版本
        versionDOS.remove(versionDOS.size() - 1);
        return modelMapper.map(versionDOS, new TypeToken<List<PageVersionDTO>>() {
        }.getType());
    }

    @Override
    public Long createVersionAndContent(Long pageId, String content, Long oldVersionId, Boolean isFirstVersion, Boolean isMinorEdit) {
        String versionName;
        if (isFirstVersion) {
            versionName = Version.firstVersion;
        } else {
            String oldVersionName = pageVersionRepository.queryByVersionId(oldVersionId, pageId).getName();
            versionName = incrementVersion(oldVersionName, isMinorEdit);
        }
        PageVersionDO create = new PageVersionDO();
        create.setName(versionName);
        create.setPageId(pageId);
        pageVersionRepository.create(create);
        Long latestVersionId = create.getId();
        //创建内容
        PageContentDO pageContent = new PageContentDO();
        pageContent.setPageId(pageId);
        pageContent.setVersionId(latestVersionId);
        pageContent.setContent(content);
        pageContent.setDrawContent(Markdown2HtmlUtil.markdown2Html(content));
        pageContentRepository.create(pageContent);
        if (!isFirstVersion) {
            //更新上个版本内容为diff
            PageContentDO lastContent = pageContentRepository.selectByVersionId(oldVersionId, pageId);
            TextDiffDTO diffDTO = DiffUtil.diff(lastContent.getContent(), content);
            lastContent.setContent(JSONObject.toJSONString(diffDTO));
            lastContent.setDrawContent(null);
            pageContentRepository.updateOptions(lastContent, "content", "drawContent");
        }
        return latestVersionId;
    }

    private String incrementVersion(String versionName, Boolean isMinorEdit) {
        if (isMinorEdit) {
            return new Version(versionName).next().toString();
        } else {
            return new Version(versionName).getBranchPoint().next().newBranch(1).toString();
        }
    }

    @Override
    public PageVersionInfoDTO queryById(Long organizationId, Long projectId, Long pageId, Long versionId) {
        PageDO pageDO = pageRepository.queryById(organizationId, projectId, pageId);
        Long latestVersionId = pageDO.getLatestVersionId();
        PageVersionInfoDTO pageVersion = modelMapper.map(pageVersionRepository.queryByVersionId(versionId, pageId), PageVersionInfoDTO.class);
        //若是最新版本直接返回
        if (versionId.equals(latestVersionId)) {
            PageContentDO pageContent = pageContentRepository.selectByVersionId(latestVersionId, pageId);
            pageVersion.setContent(pageContent.getContent());
        }
        List<PageContentDO> pageContents = pageContentMapper.queryByPageId(pageId);
        //判断正序还是倒序更快速解析
        if (pageContents.get(pageContents.size() / 2).getVersionId() > versionId) {
            //正序解析
            List<TextDiffDTO> diffs = pageContents.stream().filter(content -> content.getVersionId() < versionId).map(
                    content -> TextDiffDTO.jsonToDTO(JSON.parseObject(content.getContent()))).collect(Collectors.toList());
            pageVersion.setContent(DiffUtil.parseObverse(diffs));
        } else {
            //倒序解析
            List<TextDiffDTO> diffs = pageContents.stream().filter(content -> (content.getVersionId() >= versionId) && !latestVersionId.equals(content.getVersionId())).map(
                    content -> TextDiffDTO.jsonToDTO(JSON.parseObject(content.getContent()))).collect(Collectors.toList());
            PageContentDO pageContent = pageContentRepository.selectByVersionId(latestVersionId, pageId);
            pageVersion.setContent(DiffUtil.parseReverse(diffs, pageContent.getContent()));
        }
        return pageVersion;
    }

    @Override
    public PageVersionCompareDTO compareVersion(Long organizationId, Long projectId, Long pageId, Long firstVersionId, Long secondVersionId) {
        PageVersionInfoDTO firstVersion = queryById(organizationId, projectId, pageId, firstVersionId);
        PageVersionInfoDTO secondVersion = queryById(organizationId, projectId, pageId, secondVersionId);
        PageVersionCompareDTO compareDTO = new PageVersionCompareDTO();
        Parser parser = Parser.builder().build();
        TextContentRenderer textContentRenderer = TextContentRenderer.builder().build();
        Node firstDocument = parser.parse(firstVersion.getContent());
        Node secondDocument = parser.parse(secondVersion.getContent());
        compareDTO.setFirstVersionContent(textContentRenderer.render(firstDocument));
        compareDTO.setSecondVersionContent(textContentRenderer.render(secondDocument));
        TextDiffDTO diffDTO = DiffUtil.diff(compareDTO.getFirstVersionContent(), compareDTO.getSecondVersionContent());
        handleDiff(compareDTO, diffDTO);
        return compareDTO;
    }

    /**
     * 处理diff为差异文本显示在页面上
     *
     * @param compareDTO
     * @param diffDTO
     */
    private void handleDiff(PageVersionCompareDTO compareDTO, TextDiffDTO diffDTO) {
        List<String> sourceList = DiffUtil.textToLines(compareDTO.getFirstVersionContent());
        List<String> targetList = DiffUtil.textToLines(compareDTO.getSecondVersionContent());
        Map<Integer, DiffHandleDTO> diffMap = new HashMap<>(targetList.size());
        //处理删除
        for (Delta<String> delta : diffDTO.getDeleteData()) {
            diffMap.put(delta.getRevised().getPosition(), new DiffHandleDTO.Builder().delete(delta.getOriginal().getLines()).build());
        }
        //处理增加
        for (Delta<String> delta : diffDTO.getInsertData()) {
            diffMap.put(delta.getRevised().getPosition(), new DiffHandleDTO.Builder().insert(delta.getRevised().getLines()).build());
        }
        //处理改变
        for (Delta<String> delta : diffDTO.getChangeData()) {
            diffMap.put(delta.getRevised().getPosition(), new DiffHandleDTO.Builder().change(delta.getOriginal().getLines(), delta.getRevised().getLines()).build());
        }
        List<String> diffList = new ArrayList<>(targetList.size() + sourceList.size());
        for (int i = 0; i < targetList.size(); ) {
            DiffHandleDTO handleDTO = diffMap.get(i);
            if (handleDTO == null) {
                diffList.add(targetList.get(i));
                i++;
            } else {
                switch (handleDTO.getType()) {
                    case DELETE:
                        diffList.addAll(handleDTO.getStrs());
                        diffList.add(targetList.get(i));
                        i++;
                        break;
                    case INSERT:
                        diffList.addAll(handleDTO.getStrs());
                        i += handleDTO.getSkipLine();
                        break;
                    case CHANGE:
                        diffList.addAll(handleDTO.getStrs());
                        i += handleDTO.getSkipLine();
                        break;
                }
            }
        }
        compareDTO.setDiffContent(DiffUtil.linesToText(diffList));
    }

    @Override
    public void rollbackVersion(Long organizationId, Long projectId, Long pageId, Long versionId) {
        PageVersionInfoDTO versionInfo = queryById(organizationId, projectId, pageId, versionId);
        PageDO pageDO = pageRepository.queryById(organizationId, projectId, pageId);
        Long latestVersionId = pageVersionService.createVersionAndContent(pageDO.getId(), versionInfo.getContent(), pageDO.getLatestVersionId(), false, false);
        pageDO.setLatestVersionId(latestVersionId);
        pageRepository.update(pageDO, true);
    }
}
