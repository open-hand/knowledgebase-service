package io.choerodon.kb.app.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.api.dao.PageAttachmentDTO;
import io.choerodon.kb.api.dao.PageCommentDTO;
import io.choerodon.kb.api.dao.PageInfoDTO;
import io.choerodon.kb.app.service.PageService;
import io.choerodon.kb.domain.kb.repository.IamRepository;
import io.choerodon.kb.domain.kb.repository.PageAttachmentRepository;
import io.choerodon.kb.domain.kb.repository.PageCommentRepository;
import io.choerodon.kb.domain.kb.repository.PageRepository;
import io.choerodon.kb.infra.dataobject.PageAttachmentDO;
import io.choerodon.kb.infra.dataobject.PageCommentDO;
import io.choerodon.kb.infra.dataobject.PageDO;
import io.choerodon.kb.infra.dataobject.iam.UserDO;

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

    @Override
    public PageInfoDTO queryPageInfo(Long id) {
        PageInfoDTO pageInfoDTO = new PageInfoDTO();
        List<PageAttachmentDO> pageAttachments = pageAttachmentRepository.selectByPageId(id);
        if (pageAttachments != null && !pageAttachments.isEmpty()) {
            String urlSlash = attachmentUrl.endsWith("/") ? "" : "/";
            pageAttachments.stream().forEach(pageAttachmentDO -> pageAttachmentDO.setUrl(attachmentUrl + urlSlash + pageAttachmentDO.getUrl()));
            pageInfoDTO.setAttachment(ConvertHelper.convertList(pageAttachments, PageAttachmentDTO.class));
        }
        List<PageCommentDO> pageComments = pageCommentRepository.selectByPageId(id);
        if (pageComments != null && !pageComments.isEmpty()) {
            List<Long> userIds = pageComments.stream().map(PageCommentDO::getCreatedBy).distinct()
                    .collect(Collectors.toList());
            Long[] ids = new Long[userIds.size()];
            userIds.toArray(ids);
            List<UserDO> userDOList = iamRepository.userDOList(ids);
            Map<Long, UserDO> userMap = new HashMap<>();
            userDOList.forEach(userDO -> userMap.put(userDO.getId(), userDO));
            List<PageCommentDTO> pageCommentDTOList = new ArrayList<>();
            pageComments.forEach(p -> {
                PageCommentDTO pageCommentDTO = new PageCommentDTO();
                pageCommentDTO.setId(p.getId());
                pageCommentDTO.setPageId(p.getPageId());
                pageCommentDTO.setComment(p.getComment());
                pageCommentDTO.setObjectVersionNumber(p.getObjectVersionNumber());
                pageCommentDTO.setUserId(p.getCreatedBy());
                pageCommentDTO.setLastUpdateDate(p.getLastUpdateDate());
                pageCommentDTO.setUserName(userMap.get(p.getCreatedBy()).getLoginName() + userMap.get(p.getCreatedBy()).getRealName());
                pageCommentDTO.setUserImageUrl(userMap.get(p.getCreatedBy()).getImageUrl());
                pageCommentDTOList.add(pageCommentDTO);
            });
            pageInfoDTO.setComment(pageCommentDTOList);
        }

        return pageInfoDTO;
    }
}
