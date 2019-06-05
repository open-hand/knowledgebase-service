package io.choerodon.kb.domain.kb.repository;

import io.choerodon.kb.api.dao.PageInfo;
import io.choerodon.kb.infra.dataobject.PageDO;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface PageRepository {

    PageDO selectById(Long id);

    PageDO update(PageDO pageDO, Boolean flag);

    void delete(Long id);

    PageDO create(PageDO create);

    PageDO queryById(Long organizationId, Long projectId, Long pageId);

    PageInfo queryInfoById(Long organizationId, Long projectId, Long pageId);

    void checkById(Long organizationId, Long projectId, Long pageId);
}
