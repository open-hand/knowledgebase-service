package io.choerodon.kb.app.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.app.service.PageService;
import io.choerodon.kb.domain.kb.repository.PageContentRepository;
import io.choerodon.kb.domain.kb.repository.PageRepository;
import io.choerodon.kb.infra.common.utils.Markdown2HtmlUtil;
import io.choerodon.kb.infra.dataobject.PageDO;

/**
 * Created by Zenger on 2019/4/30.
 */
@Service
public class PageServiceImpl implements PageService {

    @Value("${services.attachment.url}")
    private String attachmentUrl;

    private PageRepository pageRepository;
    private PageContentRepository pageContentRepository;

    public PageServiceImpl(PageRepository pageRepository,
                           PageContentRepository pageContentRepository) {
        this.pageRepository = pageRepository;
        this.pageContentRepository = pageContentRepository;
    }

    @Override
    public Boolean checkPageCreate(Long id) {
        PageDO pageDO = pageRepository.selectById(id);
        CustomUserDetails customUserDetails = DetailsHelper.getUserDetails();
        return pageDO.getCreatedBy().equals(customUserDetails.getUserId()) ? true : false;
    }

    @Override
    public String pageToc(Long id) {
        PageDO pageDO = pageRepository.selectById(id);
        return Markdown2HtmlUtil.toc(pageContentRepository.selectByVersionId(pageDO.getLatestVersionId(), pageDO.getId()).getContent());
    }
}
