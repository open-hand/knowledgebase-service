package io.choerodon.kb.domain.kb.repository;

import io.choerodon.kb.infra.dto.PageCommentDTO;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface PageCommentRepository {

    PageCommentDTO baseCreate(PageCommentDTO pageCommentDTO);

    PageCommentDTO baseUpdate(PageCommentDTO pageCommentDTO);

    PageCommentDTO baseQueryById(Long id);

    void baseDelete(Long id);

    void deleteByPageId(Long pageId);
}
