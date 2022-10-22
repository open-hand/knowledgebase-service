package io.choerodon.kb.domain.repository;

import java.util.List;

import io.choerodon.kb.api.vo.PageAttachmentVO;
import io.choerodon.kb.infra.dto.PageAttachmentDTO;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface PageAttachmentRepository {

    PageAttachmentDTO baseCreate(PageAttachmentDTO pageAttachmentDTO);

    PageAttachmentDTO baseQueryById(Long id);

    void baseDelete(Long id);

    List<PageAttachmentVO> queryByList(Long organizationId, Long projectId, Long pageId);
}
