package io.choerodon.kb.domain.kb.repository;

import io.choerodon.kb.infra.dto.PageContentDTO;

/**
 * @author shinan.chen
 * @since 2019/5/16
 */
public interface PageContentRepository {

    void deleteByPageId(Long pageId);

    PageContentDTO create(PageContentDTO create);

    void delete(Long id);

    void update(PageContentDTO update);

    void updateOptions(PageContentDTO update, String... fields);

    PageContentDTO selectByVersionId(Long versionId, Long pageId);

    PageContentDTO selectLatestByPageId(Long pageId);
}
