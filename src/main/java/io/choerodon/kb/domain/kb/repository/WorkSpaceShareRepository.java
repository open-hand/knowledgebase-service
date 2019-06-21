package io.choerodon.kb.domain.kb.repository;

import io.choerodon.kb.infra.dataobject.WorkSpaceShareDO;

/**
 * Created by Zenger on 2019/6/10.
 */
public interface WorkSpaceShareRepository {

    WorkSpaceShareDO insert(WorkSpaceShareDO workSpaceShareDO);

    WorkSpaceShareDO update(WorkSpaceShareDO workSpaceShareDO);

    void deleteByWorkSpaceId(Long workSpaceId);

    WorkSpaceShareDO selectById(Long id);

    WorkSpaceShareDO selectByWorkSpaceId(Long workSpaceId);

    WorkSpaceShareDO selectOne(WorkSpaceShareDO workSpaceShareDO);
}
