package io.choerodon.kb.domain.kb.repository;

import io.choerodon.kb.domain.kb.entity.PageVersionE;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface PageVersionRepository {

    PageVersionE insert(PageVersionE pageVersionE);

    PageVersionE selectById(Long id);

    PageVersionE selectOne(PageVersionE pageVersionE);

    void deleteByPageId(Long pageId);
}
