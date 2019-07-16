package io.choerodon.kb.infra.persistence.impl;

import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.kb.repository.WorkSpacePageRepository;
import io.choerodon.kb.infra.dto.WorkSpacePageDTO;
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
    public WorkSpacePageDTO insert(WorkSpacePageDTO workSpacePageDTO) {
        if (workSpacePageMapper.insert(workSpacePageDTO) != 1) {
            throw new CommonException(ERROR_WORKSPACEPAGE_INSERT);
        }
        return workSpacePageMapper.selectByPrimaryKey(workSpacePageDTO.getId());
    }

    @Override
    public WorkSpacePageDTO update(WorkSpacePageDTO workSpacePageDTO) {
        if (workSpacePageMapper.updateByPrimaryKey(workSpacePageDTO) != 1) {
            throw new CommonException(ERROR_WORKSPACEPAGE_UPDATE);
        }
        return workSpacePageMapper.selectByPrimaryKey(workSpacePageDTO.getId());
    }

    @Override
    public WorkSpacePageDTO selectByWorkSpaceId(Long workSpaceId) {
        WorkSpacePageDTO workSpacePageDTO = new WorkSpacePageDTO();
        workSpacePageDTO.setWorkspaceId(workSpaceId);
        WorkSpacePageDTO workSpacePage = workSpacePageMapper.selectOne(workSpacePageDTO);
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
