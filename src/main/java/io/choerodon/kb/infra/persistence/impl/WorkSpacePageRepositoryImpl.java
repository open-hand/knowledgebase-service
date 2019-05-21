package io.choerodon.kb.infra.persistence.impl;

import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.kb.repository.WorkSpacePageRepository;
import io.choerodon.kb.infra.dataobject.WorkSpacePageDO;
import io.choerodon.kb.infra.mapper.WorkSpacePageMapper;

/**
 * Created by Zenger on 2019/4/29.
 */
@Service
public class WorkSpacePageRepositoryImpl implements WorkSpacePageRepository {

    private static final String ERROR_WORKSPACEPAGE_INSERT = "error.workSpacePage.insert";
    private static final String ERROR_WORKSPACEPAGE_UPDATE = "error.workSpacePage.update";
    private static final String ERROR_WORKSPACEPAGE_SELECT = "error.workSpacePage.select";
    private static final String ERROR_WORKSPACEPAGE_DELETE = "error.workSpacePage.delete";

    private WorkSpacePageMapper workSpacePageMapper;

    public WorkSpacePageRepositoryImpl(WorkSpacePageMapper workSpacePageMapper) {
        this.workSpacePageMapper = workSpacePageMapper;
    }

    @Override
    public WorkSpacePageDO insert(WorkSpacePageDO workSpacePageDO) {
        if (workSpacePageMapper.insert(workSpacePageDO) != 1) {
            throw new CommonException(ERROR_WORKSPACEPAGE_INSERT);
        }
        return workSpacePageMapper.selectByPrimaryKey(workSpacePageDO.getId());
    }

    @Override
    public WorkSpacePageDO update(WorkSpacePageDO workSpacePageDO) {
        if (workSpacePageMapper.updateByPrimaryKey(workSpacePageDO) != 1) {
            throw new CommonException(ERROR_WORKSPACEPAGE_UPDATE);
        }
        return workSpacePageMapper.selectByPrimaryKey(workSpacePageDO.getId());
    }

    @Override
    public WorkSpacePageDO selectByWorkSpaceId(Long workSpaceId) {
        WorkSpacePageDO workSpacePageDO = new WorkSpacePageDO();
        workSpacePageDO.setWorkspaceId(workSpaceId);
        WorkSpacePageDO workSpacePage = workSpacePageMapper.selectOne(workSpacePageDO);
        if (workSpacePage == null) {
            throw new CommonException(ERROR_WORKSPACEPAGE_SELECT);
        }
        return workSpacePage;
    }

    @Override
    public void delete(Long id) {
        if (workSpacePageMapper.deleteByPrimaryKey(id) != 1) {
            throw new CommonException(ERROR_WORKSPACEPAGE_DELETE);
        }
    }
}
