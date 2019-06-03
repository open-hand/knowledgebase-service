package io.choerodon.kb.app.service;

import io.choerodon.kb.api.dao.PageCommentDTO;
import io.choerodon.kb.api.dao.PageCreateCommentDTO;
import io.choerodon.kb.api.dao.PageUpdateCommentDTO;

import java.util.List;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageCommentService {

    PageCommentDTO create(PageCreateCommentDTO pageCreateCommentDTO);

    PageCommentDTO update(Long id, PageUpdateCommentDTO pageUpdateCommentDTO);

    List<PageCommentDTO> queryByList(Long pageId);

    void delete(Long id, Boolean isAdmin);
}
