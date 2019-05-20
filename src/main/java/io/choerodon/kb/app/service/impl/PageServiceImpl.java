package io.choerodon.kb.app.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.app.service.PageService;
import io.choerodon.kb.domain.kb.repository.IamRepository;
import io.choerodon.kb.domain.kb.repository.PageAttachmentRepository;
import io.choerodon.kb.domain.kb.repository.PageCommentRepository;
import io.choerodon.kb.domain.kb.repository.PageRepository;
import io.choerodon.kb.infra.dataobject.PageDO;

/**
 * Created by Zenger on 2019/4/30.
 */
@Service
public class PageServiceImpl implements PageService {

    @Value("${services.attachment.url}")
    private String attachmentUrl;

    private IamRepository iamRepository;
    private PageRepository pageRepository;
    private PageCommentRepository pageCommentRepository;
    private PageAttachmentRepository pageAttachmentRepository;

    public PageServiceImpl(IamRepository iamRepository,
                           PageRepository pageRepository,
                           PageCommentRepository pageCommentRepository,
                           PageAttachmentRepository pageAttachmentRepository) {
        this.iamRepository = iamRepository;
        this.pageRepository = pageRepository;
        this.pageCommentRepository = pageCommentRepository;
        this.pageAttachmentRepository = pageAttachmentRepository;
    }

    @Override
    public Boolean checkPageCreate(Long id) {
        PageDO pageDO = pageRepository.selectById(id);
        if (pageDO == null) {
            throw new CommonException("error.page.select");
        }
        CustomUserDetails customUserDetails = DetailsHelper.getUserDetails();
        return pageDO.getCreatedBy().equals(customUserDetails.getUserId()) ? true : false;
    }
}
