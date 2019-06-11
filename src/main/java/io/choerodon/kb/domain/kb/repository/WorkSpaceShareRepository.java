package io.choerodon.kb.domain.kb.repository;

import io.choerodon.kb.infra.dataobject.WorkSpaceShareDO;

/**
 * Created by Zenger on 2019/6/10.
 */
public interface WorkSpaceShareRepository {

    WorkSpaceShareDO inset(WorkSpaceShareDO workSpaceShareDO);

    WorkSpaceShareDO selectById(Long id);

    WorkSpaceShareDO selectOne(WorkSpaceShareDO workSpaceShareDO);
}
