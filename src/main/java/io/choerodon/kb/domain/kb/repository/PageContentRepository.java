package io.choerodon.kb.domain.kb.repository;

import io.choerodon.kb.infra.dto.PageContentDTO;

/**
 * @author shinan.chen
 * @since 2019/5/16
 */
public interface PageContentRepository {

    PageContentDTO baseCreate(PageContentDTO create);

    void baseUpdate(PageContentDTO update);

    void baseUpdateOptions(PageContentDTO update, String... fields);

    PageContentDTO selectByVersionId(Long versionId, Long pageId);

}
