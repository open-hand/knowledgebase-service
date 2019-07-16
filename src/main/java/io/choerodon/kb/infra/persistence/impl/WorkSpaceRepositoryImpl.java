package io.choerodon.kb.infra.persistence.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.kb.repository.WorkSpaceRepository;
import io.choerodon.kb.infra.dto.PageDetailDTO;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.dto.iam.UserDO;
import io.choerodon.kb.infra.feign.UserFeignClient;
import io.choerodon.kb.infra.mapper.WorkSpaceMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Zenger on 2019/4/29.
 */
@Service
public class WorkSpaceRepositoryImpl implements WorkSpaceRepository {

    private static final String ERROR_WORK_SPACE_INSERT = "error.work.space.insert";
    private static final String ERROR_WORK_SPACE_UPDATE = "error.work.space.update";
    private static final String ERROR_WORKSPACE_ILLEGAL = "error.workspace.illegal";
    private static final String ERROR_WORKSPACE_CREATE = "error.workspace.create";
    private static final String ERROR_WORKSPACE_DELETE = "error.workspace.delete";
    private static final String ERROR_WORKSPACE_NOTFOUND = "error.workspace.notFound";
    private static final String ERROR_WORKSPACE_UPDATE = "error.workspace.update";
    private static final String ERROR_WORKSPACE_SELECT = "error.workspace.select";

    private WorkSpaceMapper workSpaceMapper;
    private UserFeignClient userFeignClient;

    public WorkSpaceRepositoryImpl(WorkSpaceMapper workSpaceMapper,
                                   UserFeignClient userFeignClient) {
        this.workSpaceMapper = workSpaceMapper;
        this.userFeignClient = userFeignClient;
    }

    @Override
    public WorkSpaceDTO baseCreate(WorkSpaceDTO workSpaceDTO) {
        if (workSpaceMapper.insert(workSpaceDTO) != 1) {
            throw new CommonException(ERROR_WORK_SPACE_INSERT);
        }
        return workSpaceMapper.selectByPrimaryKey(workSpaceDTO.getId());
    }

    @Override
    public WorkSpaceDTO baseUpdate(WorkSpaceDTO workSpaceDTO) {
        if (workSpaceMapper.updateByPrimaryKey(workSpaceDTO) != 1) {
            throw new CommonException(ERROR_WORK_SPACE_UPDATE);
        }
        return workSpaceMapper.selectByPrimaryKey(workSpaceDTO.getId());
    }

    @Override
    public WorkSpaceDTO selectById(Long id) {
        WorkSpaceDTO workSpaceDTO = workSpaceMapper.selectByPrimaryKey(id);
        if (workSpaceDTO == null) {
            throw new CommonException("error.work.space.select");
        }
        return workSpaceDTO;
    }

    @Override
    public WorkSpaceDTO queryById(Long organizationId, Long projectId, Long workSpaceId) {
        WorkSpaceDTO workSpaceDTO = workSpaceMapper.selectByPrimaryKey(workSpaceId);
        if (workSpaceDTO == null) {
            throw new CommonException(ERROR_WORKSPACE_NOTFOUND);
        }
        if (organizationId != null && workSpaceDTO.getOrganizationId() != null && !workSpaceDTO.getOrganizationId().equals(organizationId)) {
            throw new CommonException(ERROR_WORKSPACE_ILLEGAL);
        }
        if (projectId != null && workSpaceDTO.getProjectId() != null && !workSpaceDTO.getProjectId().equals(projectId)) {
            throw new CommonException(ERROR_WORKSPACE_ILLEGAL);
        }
        return workSpaceDTO;
    }

    @Override
    public void checkById(Long organizationId, Long projectId, Long workSpaceId) {
        queryById(organizationId, projectId, workSpaceId);
    }

    @Override
    public PageDetailDTO queryDetail(Long id) {
        return getPageDetailInfo(workSpaceMapper.queryDetail(id));
    }

    @Override
    public PageDetailDTO queryReferenceDetail(Long id) {
        return getPageDetailInfo(workSpaceMapper.queryReferenceDetail(id));
    }

    private PageDetailDTO getPageDetailInfo(PageDetailDTO pageDetailDTO) {
        Long[] ids = new Long[2];
        ids[0] = pageDetailDTO.getCreatedBy();
        ids[1] = pageDetailDTO.getLastUpdatedBy();
        List<UserDO> userDOList = userFeignClient.listUsersByIds(ids, false).getBody();
        String createName = "";
        String lastUpdatedName = "";
        for (UserDO userDO : userDOList) {
            if (ids[0].equals(userDO.getId())) {
                createName = userDO.getLoginName() + userDO.getRealName();
            }
            if (ids[1].equals(userDO.getId())) {
                lastUpdatedName = userDO.getLoginName() + userDO.getRealName();
            }
        }
        pageDetailDTO.setCreateName(createName);
        pageDetailDTO.setLastUpdatedName(lastUpdatedName);

        return pageDetailDTO;
    }

    @Override
    public List<WorkSpaceDTO> queryAllChildByWorkSpaceId(Long workSpaceId) {
        WorkSpaceDTO workSpaceDTO = selectById(workSpaceId);
        List<WorkSpaceDTO> list = workSpaceMapper.selectAllChildByRoute(workSpaceDTO.getRoute());
        list.add(workSpaceDTO);
        return list;
    }
}
