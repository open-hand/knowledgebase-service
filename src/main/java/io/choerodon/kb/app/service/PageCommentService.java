package io.choerodon.kb.app.service;

import java.util.List;

import io.choerodon.kb.api.dao.PageCommentDTO;
import io.choerodon.kb.api.dao.PageCreateCommentDTO;
import io.choerodon.kb.api.dao.PageUpdateCommentDTO;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageCommentService {

    PageCommentDTO create(PageCreateCommentDTO pageCreateCommentDTO);

    PageCommentDTO update(Long id, PageUpdateCommentDTO pageUpdateCommentDTO);

    List<PageCommentDTO> queryByList(Long pageId);

    void delete(Long id);
}
