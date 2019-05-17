package io.choerodon.kb.domain.kb.repository;

import io.choerodon.kb.infra.dataobject.PageDO;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface PageRepository {

    PageDO selectById(Long id);

    PageDO insert(PageDO pageDO);

    PageDO update(PageDO pageDO, Boolean flag);

    void delete(Long id);
}
