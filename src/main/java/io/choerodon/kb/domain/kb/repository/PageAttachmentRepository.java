package io.choerodon.kb.domain.kb.repository;

import java.util.List;

import io.choerodon.kb.infra.dto.PageAttachmentDTO;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface PageAttachmentRepository {

    PageAttachmentDTO insert(PageAttachmentDTO pageAttachmentDTO);

    PageAttachmentDTO selectById(Long id);

    List<PageAttachmentDTO> selectByIds(List<Long> ids);

    List<PageAttachmentDTO> selectByPageId(Long pageId);

    void delete(Long id);

    List<PageAttachmentDTO> searchAttachment(Long organizationId, Long projectId, String fileName, String attachmentUrl);
}
