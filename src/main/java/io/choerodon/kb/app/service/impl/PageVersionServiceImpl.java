package io.choerodon.kb.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.choerodon.kb.api.dao.PageVersionDTO;
import io.choerodon.kb.api.dao.PageVersionInfoDTO;
import io.choerodon.kb.api.dao.TextDiffDTO;
import io.choerodon.kb.app.service.PageVersionService;
import io.choerodon.kb.domain.kb.repository.PageContentRepository;
import io.choerodon.kb.domain.kb.repository.PageRepository;
import io.choerodon.kb.domain.kb.repository.PageVersionRepository;
import io.choerodon.kb.infra.common.utils.DiffUtil;
import io.choerodon.kb.infra.common.utils.Markdown2HtmlUtil;
import io.choerodon.kb.infra.common.utils.Version;
import io.choerodon.kb.infra.dataobject.PageContentDO;
import io.choerodon.kb.infra.dataobject.PageDO;
import io.choerodon.kb.infra.dataobject.PageVersionDO;
import io.choerodon.kb.infra.mapper.PageContentMapper;
import io.choerodon.kb.infra.mapper.PageVersionMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
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
        versionDOS.remove(versionDOS.size()-1);
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
            lastContent.setDrawContent("");
            pageContentRepository.update(lastContent);
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
                    content -> JSONObject.parseObject(content.getContent(), TextDiffDTO.class)).collect(Collectors.toList());
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
}
