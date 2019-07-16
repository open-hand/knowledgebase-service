package io.choerodon.kb.domain.kb.repository;

import io.choerodon.kb.infra.dto.PageVersionDTO;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface PageVersionRepository {

    PageVersionDTO baseCreate(PageVersionDTO create);

    void baseDelete(Long versionId);

    void baseUpdate(PageVersionDTO update);

    PageVersionDTO queryByVersionId(Long versionId, Long pageId);
}
