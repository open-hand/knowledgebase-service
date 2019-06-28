package io.choerodon.kb.infra.persistence.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.kb.repository.WorkSpaceRepository;
import io.choerodon.kb.infra.dataobject.PageDetailDO;
import io.choerodon.kb.infra.dataobject.WorkSpaceDO;
import io.choerodon.kb.infra.dataobject.iam.UserDO;
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
    public WorkSpaceDO insert(WorkSpaceDO workSpaceDO) {
        if (workSpaceMapper.insert(workSpaceDO) != 1) {
            throw new CommonException(ERROR_WORK_SPACE_INSERT);
        }
        return workSpaceMapper.selectByPrimaryKey(workSpaceDO.getId());
    }

    @Override
    public WorkSpaceDO update(WorkSpaceDO workSpaceDO) {
        if (workSpaceMapper.updateByPrimaryKey(workSpaceDO) != 1) {
            throw new CommonException(ERROR_WORK_SPACE_UPDATE);
        }
        return workSpaceMapper.selectByPrimaryKey(workSpaceDO.getId());
    }

    @Override
    public Boolean hasChildWorkSpace(String type, Long resourceId, Long parentId) {
        return workSpaceMapper.hasChildWorkSpace(type, resourceId, parentId);
    }

    @Override
    public String queryMaxRank(String type, Long resourceId, Long parentId) {
        return workSpaceMapper.queryMaxRank(type, resourceId, parentId);
    }

    @Override
    public String queryMinRank(String type, Long resourceId, Long parentId) {
        return workSpaceMapper.queryMinRank(type, resourceId, parentId);
    }

    @Override
    public String queryRank(String type, Long resourceId, Long id) {
        return workSpaceMapper.queryRank(type, resourceId, id);
    }

    @Override
    public String queryLeftRank(String type, Long resourceId, Long parentId, String rightRank) {
        return workSpaceMapper.queryLeftRank(type, resourceId, parentId, rightRank);
    }

    @Override
    public String queryRightRank(String type, Long resourceId, Long parentId, String leftRank) {
        return workSpaceMapper.queryRightRank(type, resourceId, parentId, leftRank);
    }

    @Override
    public WorkSpaceDO selectById(Long id) {
        WorkSpaceDO workSpaceDO = workSpaceMapper.selectByPrimaryKey(id);
        if (workSpaceDO == null) {
            throw new CommonException("error.work.space.select");
        }
        return workSpaceDO;
    }

    @Override
    public WorkSpaceDO queryById(Long organizationId, Long projectId, Long workSpaceId) {
        WorkSpaceDO workSpaceDO = workSpaceMapper.selectByPrimaryKey(workSpaceId);
        if (workSpaceDO == null) {
            throw new CommonException(ERROR_WORKSPACE_NOTFOUND);
        }
        if (organizationId != null && workSpaceDO.getOrganizationId() != null && !workSpaceDO.getOrganizationId().equals(organizationId)) {
            throw new CommonException(ERROR_WORKSPACE_ILLEGAL);
        }
        if (projectId != null && workSpaceDO.getProjectId() != null && !workSpaceDO.getProjectId().equals(projectId)) {
            throw new CommonException(ERROR_WORKSPACE_ILLEGAL);
        }
        return workSpaceDO;
    }

    @Override
    public void checkById(Long organizationId, Long projectId, Long workSpaceId) {
        queryById(organizationId, projectId, workSpaceId);
    }

    @Override
    public PageDetailDO queryDetail(Long id) {
        return getPageDetailInfo(workSpaceMapper.queryDetail(id));
    }

    @Override
    public PageDetailDO queryReferenceDetail(Long id) {
        return getPageDetailInfo(workSpaceMapper.queryReferenceDetail(id));
    }

    private PageDetailDO getPageDetailInfo(PageDetailDO pageDetailDO) {
        Long[] ids = new Long[2];
        ids[0] = pageDetailDO.getCreatedBy();
        ids[1] = pageDetailDO.getLastUpdatedBy();
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
        pageDetailDO.setCreateName(createName);
        pageDetailDO.setLastUpdatedName(lastUpdatedName);

        return pageDetailDO;
    }

    @Override
    public void deleteByRoute(String route) {
        workSpaceMapper.deleteByRoute(route);
    }

    @Override
    public List<WorkSpaceDO> queryAllChildByWorkSpaceId(Long workSpaceId) {
        WorkSpaceDO workSpaceDO = selectById(workSpaceId);
        List<WorkSpaceDO> list = workSpaceMapper.selectAllChildByRoute(workSpaceDO.getRoute());
        list.add(workSpaceDO);
        return list;
    }

    @Override
    public List<WorkSpaceDO> workSpacesByParentId(Long parentId) {
        return workSpaceMapper.workSpacesByParentId(parentId);
    }

    @Override
    public void updateByRoute(String type, Long resourceId, String odlRoute, String newRoute) {
        workSpaceMapper.updateByRoute(type, resourceId, odlRoute, newRoute);
    }

    @Override
    public List<WorkSpaceDO> queryAll(Long resourceId, String type) {
        return workSpaceMapper.queryAll(resourceId, type);
    }
}
