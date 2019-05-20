package io.choerodon.kb.app.service;

import java.util.List;

import io.choerodon.kb.api.dao.PageCommentDTO;
import io.choerodon.kb.api.dao.PageCommentUpdateDTO;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageCommentService {

    PageCommentDTO create(PageCommentUpdateDTO pageCommentUpdateDTO);

    PageCommentDTO update(Long id, PageCommentUpdateDTO pageCommentUpdateDTO);

    List<PageCommentDTO> queryByList(Long pageId);

    void delete(Long id);
}
