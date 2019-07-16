package io.choerodon.kb.domain.kb.repository;

import io.choerodon.kb.infra.dto.PageAttachmentDTO;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface PageAttachmentRepository {

    PageAttachmentDTO baseCreate(PageAttachmentDTO pageAttachmentDTO);

    PageAttachmentDTO baseQueryById(Long id);

    void baseDelete(Long id);
}
