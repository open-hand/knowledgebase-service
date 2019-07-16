package io.choerodon.kb.domain.kb.repository;

import io.choerodon.kb.infra.dto.PageVersionDTO;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface PageVersionRepository {

    PageVersionDTO selectOne(PageVersionDTO pageVersion);

    void deleteByPageId(Long pageId);

    PageVersionDTO create(PageVersionDTO create);

    void delete(Long versionId);

    void update(PageVersionDTO update);

    PageVersionDTO queryByVersionId(Long versionId, Long pageId);

    String selectMaxVersionByPageId(Long pageId);
}
