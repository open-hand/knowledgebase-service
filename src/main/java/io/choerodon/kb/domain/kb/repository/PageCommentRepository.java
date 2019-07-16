package io.choerodon.kb.domain.kb.repository;

import java.util.List;

import io.choerodon.kb.infra.dto.PageCommentDTO;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface PageCommentRepository {

    PageCommentDTO insert(PageCommentDTO pageCommentDTO);

    PageCommentDTO update(PageCommentDTO pageCommentDTO);

    PageCommentDTO selectById(Long id);

    List<PageCommentDTO> selectByPageId(Long pageId);

    void delete(Long id);

    void deleteByPageId(Long pageId);
}
