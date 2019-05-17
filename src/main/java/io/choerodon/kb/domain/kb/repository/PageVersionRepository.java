package io.choerodon.kb.domain.kb.repository;

import io.choerodon.kb.infra.dataobject.PageVersionDO;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface PageVersionRepository {

    PageVersionDO selectOne(PageVersionDO pageVersion);

    void deleteByPageId(Long pageId);

    PageVersionDO create(PageVersionDO create);

    void delete(Long versionId);

    void update(PageVersionDO update);

    PageVersionDO queryByVersionId(Long versionId, Long pageId);

    String selectMaxVersionByPageId(Long pageId);
}
