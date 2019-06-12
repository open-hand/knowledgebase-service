package io.choerodon.kb.infra.persistence.impl;

import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.kb.repository.WorkSpaceShareRepository;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.common.annotation.DataLog;
import io.choerodon.kb.infra.dataobject.WorkSpaceShareDO;
import io.choerodon.kb.infra.mapper.WorkSpaceShareMapper;

/**
 * Created by Zenger on 2019/6/10.
 */
@Service
public class WorkSpaceShareRepositoryImpl implements WorkSpaceShareRepository {

    private static final String ERROR_WORK_SPACE_SHARE_INSERT = "error.work.space.share.insert";
    private static final String ERROR_WORK_SPACE_SHARE_SELECT = "error.work.space.share.select";

    private WorkSpaceShareMapper workSpaceShareMapper;

    public WorkSpaceShareRepositoryImpl(WorkSpaceShareMapper workSpaceShareMapper) {
        this.workSpaceShareMapper = workSpaceShareMapper;
    }

    @Override
    @DataLog(type = BaseStage.SHARE_CREATE)
    public WorkSpaceShareDO inset(WorkSpaceShareDO workSpaceShareDO) {
        if (workSpaceShareMapper.insert(workSpaceShareDO) != 1) {
            throw new CommonException(ERROR_WORK_SPACE_SHARE_INSERT);
        }
        return workSpaceShareMapper.selectByPrimaryKey(workSpaceShareDO.getId());
    }

    @Override
    public WorkSpaceShareDO selectById(Long id) {
        WorkSpaceShareDO workSpaceShareDO = workSpaceShareMapper.selectByPrimaryKey(id);
        if (workSpaceShareDO == null) {
            throw new CommonException(ERROR_WORK_SPACE_SHARE_SELECT);
        }
        return workSpaceShareDO;
    }

    @Override
    public WorkSpaceShareDO selectOne(WorkSpaceShareDO workSpaceShareDO) {
        workSpaceShareDO = workSpaceShareMapper.selectOne(workSpaceShareDO);
        if (workSpaceShareDO == null) {
            throw new CommonException(ERROR_WORK_SPACE_SHARE_SELECT);
        }
        return workSpaceShareDO;
    }
}
