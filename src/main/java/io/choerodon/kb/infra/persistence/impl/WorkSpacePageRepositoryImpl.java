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

    private WorkSpacePageMapper workSpacePageMapper;

    public WorkSpacePageRepositoryImpl(WorkSpacePageMapper workSpacePageMapper) {
        this.workSpacePageMapper = workSpacePageMapper;
    }

    @Override
    public WorkSpacePageDO insert(WorkSpacePageDO workSpacePageDO) {
        if (workSpacePageMapper.insert(workSpacePageDO) != 1) {
            throw new CommonException("error.workSpacePage.insert");
        }
        return workSpacePageMapper.selectByPrimaryKey(workSpacePageDO.getId());
    }

    @Override
    public WorkSpacePageDO update(WorkSpacePageDO workSpacePageDO) {
        if (workSpacePageMapper.updateByPrimaryKey(workSpacePageDO) != 1) {
            throw new CommonException("error.workSpacePage.update");
        }
        return workSpacePageMapper.selectByPrimaryKey(workSpacePageDO.getId());
    }

    @Override
    public WorkSpacePageDO selectByWorkSpaceId(Long workSpaceId) {
        WorkSpacePageDO workSpacePageDO = new WorkSpacePageDO();
        workSpacePageDO.setWorkspaceId(workSpaceId);
        return workSpacePageMapper.selectOne(workSpacePageDO);
    }

    @Override
    public void delete(Long id) {
        if (workSpacePageMapper.deleteByPrimaryKey(id) != 1) {
            throw new CommonException("error.workSpacePage.delete");
        }
    }
}
