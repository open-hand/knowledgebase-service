package io.choerodon.kb.domain.kb.repository;

import java.util.List;

import io.choerodon.kb.infra.dataobject.PageCommentDO;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface PageCommentRepository {

    PageCommentDO insert(PageCommentDO pageCommentDO);

    PageCommentDO update(PageCommentDO pageCommentDO);

    PageCommentDO selectById(Long id);

    List<PageCommentDO> selectByPageId(Long pageId);

    void delete(Long id);

    void deleteByPageId(Long pageId);
}
