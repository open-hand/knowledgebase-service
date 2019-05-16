package io.choerodon.kb.domain.kb.repository;

import io.choerodon.kb.infra.dataobject.WorkSpacePageDO;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface WorkSpacePageRepository {

    WorkSpacePageDO insert(WorkSpacePageDO workSpacePageDO);

    WorkSpacePageDO update(WorkSpacePageDO workSpacePageDO);

    WorkSpacePageDO selectByWorkSpaceId(Long workSpaceId);

    void delete(Long id);
}
