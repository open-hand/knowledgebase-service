package io.choerodon.kb.app.service;

import io.choerodon.kb.infra.dto.PageContentDTO;

/**
 * @author shinan.chen
 * @since 2019/5/16
 */
public interface PageContentService {

    PageContentDTO baseCreate(PageContentDTO create);

    void baseUpdate(PageContentDTO update);

    void baseUpdateOptions(PageContentDTO update, String... fields);

    PageContentDTO selectByVersionId(Long versionId, Long pageId);

}
