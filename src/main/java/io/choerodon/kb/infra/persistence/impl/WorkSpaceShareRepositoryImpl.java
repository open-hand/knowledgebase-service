package io.choerodon.kb.infra.persistence.impl;

import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.kb.repository.WorkSpaceShareRepository;
import io.choerodon.kb.infra.dto.WorkSpaceShareDTO;
import io.choerodon.kb.infra.mapper.WorkSpaceShareMapper;

/**
 * Created by Zenger on 2019/6/10.
 */
@Service
public class WorkSpaceShareRepositoryImpl implements WorkSpaceShareRepository {

    private static final String ERROR_WORK_SPACE_SHARE_INSERT = "error.work.space.share.insert";
    private static final String ERROR_WORK_SPACE_SHARE_SELECT = "error.work.space.share.select";
    private static final String ERROR_WORK_SPACE_SHARE_UPDATE = "error.work.space.share.update";
    private static final String ERROR_WORK_SPACE_SHARE_DELETE = "error.work.space.share.delete";

    private WorkSpaceShareMapper workSpaceShareMapper;

    public WorkSpaceShareRepositoryImpl(WorkSpaceShareMapper workSpaceShareMapper) {
        this.workSpaceShareMapper = workSpaceShareMapper;
    }

    @Override
    public WorkSpaceShareDTO baseCreate(WorkSpaceShareDTO workSpaceShareDTO) {
        if (workSpaceShareMapper.insert(workSpaceShareDTO) != 1) {
            throw new CommonException(ERROR_WORK_SPACE_SHARE_INSERT);
        }
        return workSpaceShareMapper.selectByPrimaryKey(workSpaceShareDTO.getId());
    }

    @Override
    public WorkSpaceShareDTO baseUpdate(WorkSpaceShareDTO workSpaceShareDTO) {
        if (workSpaceShareMapper.updateByPrimaryKey(workSpaceShareDTO) != 1) {
            throw new CommonException(ERROR_WORK_SPACE_SHARE_UPDATE);
        }
        return workSpaceShareMapper.selectByPrimaryKey(workSpaceShareDTO.getId());
    }

    @Override
    public void deleteByWorkSpaceId(Long workSpaceId) {
        WorkSpaceShareDTO workSpaceShareDTO = new WorkSpaceShareDTO();
        workSpaceShareDTO.setWorkspaceId(workSpaceId);
        workSpaceShareMapper.delete(workSpaceShareDTO);
    }

    @Override
    public WorkSpaceShareDTO selectById(Long id) {
        WorkSpaceShareDTO workSpaceShareDTO = workSpaceShareMapper.selectByPrimaryKey(id);
        if (workSpaceShareDTO == null) {
            throw new CommonException(ERROR_WORK_SPACE_SHARE_SELECT);
        }
        return workSpaceShareDTO;
    }

    @Override
    public WorkSpaceShareDTO selectByWorkSpaceId(Long workSpaceId) {
        WorkSpaceShareDTO workSpaceShareDTO = new WorkSpaceShareDTO();
        workSpaceShareDTO.setWorkspaceId(workSpaceId);
        return workSpaceShareMapper.selectOne(workSpaceShareDTO);
    }

    @Override
    public WorkSpaceShareDTO selectOne(WorkSpaceShareDTO workSpaceShareDTO) {
        workSpaceShareDTO = workSpaceShareMapper.selectOne(workSpaceShareDTO);
        if (workSpaceShareDTO == null) {
            throw new CommonException(ERROR_WORK_SPACE_SHARE_SELECT);
        }
        return workSpaceShareDTO;
    }
}
