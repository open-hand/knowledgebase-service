package io.choerodon.kb.domain.kb.repository;

import io.choerodon.kb.domain.kb.entity.PageContentE;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface PageContentRepository {

    PageContentE insert(PageContentE pageContentE);

    void deleteByPageId(Long pageId);
}
