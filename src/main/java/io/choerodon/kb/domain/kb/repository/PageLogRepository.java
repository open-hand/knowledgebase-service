package io.choerodon.kb.domain.kb.repository;

import java.util.List;

import io.choerodon.kb.infra.dataobject.PageLogDO;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface PageLogRepository {

    PageLogDO insert(PageLogDO pageLogDO);

    List<PageLogDO> selectByPageId(Long pageId);

    void deleteByPageId(Long pageId);
}
