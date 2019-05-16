package io.choerodon.kb.domain.kb.repository;

import io.choerodon.kb.infra.dataobject.PageVersionDO;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface PageVersionRepository {

    PageVersionDO insert(PageVersionDO pageVersionDO);

    PageVersionDO selectById(Long id);

    PageVersionDO selectOne(PageVersionDO pageVersionDO);

    void deleteByPageId(Long pageId);
}
