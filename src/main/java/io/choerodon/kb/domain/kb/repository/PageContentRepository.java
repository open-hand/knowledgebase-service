package io.choerodon.kb.domain.kb.repository;

import io.choerodon.kb.infra.dataobject.PageContentDO;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface PageContentRepository {

    PageContentDO insert(PageContentDO pageContentE);

    void deleteByPageId(Long pageId);
}
