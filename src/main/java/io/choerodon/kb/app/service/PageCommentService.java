package io.choerodon.kb.app.service;

import io.choerodon.kb.api.vo.PageCommentVO;
import io.choerodon.kb.api.vo.PageCreateCommentVO;
import io.choerodon.kb.api.vo.PageUpdateCommentVO;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageCommentService {

    PageCommentVO create(Long organizationId, Long projectId, PageCreateCommentVO pageCreateCommentVO);

    PageCommentVO update(Long organizationId, Long projectId, Long id, PageUpdateCommentVO pageUpdateCommentVO);

    void delete(Long organizationId, Long projectId, Long id, Boolean isAdmin);
}
