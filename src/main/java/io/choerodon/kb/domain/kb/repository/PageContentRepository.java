package io.choerodon.kb.domain.kb.repository;

import io.choerodon.kb.infra.dataobject.PageContentDO;

/**
 * @author shinan.chen
 * @since 2019/5/16
 */
public interface PageContentRepository {

    void deleteByPageId(Long pageId);

    PageContentDO create(PageContentDO create);

    void delete(Long id);

    void update(PageContentDO update);

    PageContentDO selectByVersionId(Long versionId, Long pageId);

    PageContentDO selectLatestByPageId(Long pageId);
}
