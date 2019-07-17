package io.choerodon.kb.app.service;

import io.choerodon.kb.api.dao.PageCommentVO;
import io.choerodon.kb.api.dao.PageCreateCommentVO;
import io.choerodon.kb.api.dao.PageUpdateCommentVO;

import java.util.List;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageCommentService {

    PageCommentVO create(Long organizationId, Long projectId, PageCreateCommentVO pageCreateCommentVO);

    PageCommentVO update(Long organizationId, Long projectId, Long id, PageUpdateCommentVO pageUpdateCommentVO);

    List<PageCommentVO> queryByList(Long organizationId, Long projectId, Long pageId);

    void delete(Long organizationId, Long projectId, Long id, Boolean isAdmin);
}
