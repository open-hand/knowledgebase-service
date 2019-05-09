package io.choerodon.kb.domain.kb.repository;

import io.choerodon.kb.domain.kb.entity.PageE;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface PageRepository {

    PageE selectById(Long id);

    PageE insert(PageE pageE);

    PageE update(PageE pageE);

    void delete(Long id);
}
