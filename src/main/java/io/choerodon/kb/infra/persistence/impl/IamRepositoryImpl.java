package io.choerodon.kb.infra.persistence.impl;

import java.util.List;

import com.github.pagehelper.PageInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.dao.RoleAssignmentSearchVO;
import io.choerodon.kb.domain.kb.repository.IamRepository;
import io.choerodon.kb.infra.dto.iam.*;
import io.choerodon.kb.infra.feign.UserFeignClient;

/**
 * Created by Zenger on 2019/5/14.
 */
@Component
public class IamRepositoryImpl implements IamRepository {

    private static final String ERROR_ORGANIZATION_GET = "error.organization.get";
    private static final String ERROR_PROJECT_GET = "error.project.get";
    private static final String ERROR_ROLE_GET = "error.role.get";
    private static final String ERROR_USER_GET = "error.user.get";

    private UserFeignClient userFeignClient;

    public IamRepositoryImpl(UserFeignClient userFeignClient) {
        this.userFeignClient = userFeignClient;
    }

    @Override
    public OrganizationDO queryOrganizationById(Long organizationId) {
        ResponseEntity<OrganizationDO> organization = userFeignClient.queryOrgById(organizationId);
        if (organization.getStatusCode().is2xxSuccessful()) {
            return organization.getBody();
        } else {
            throw new CommonException(ERROR_ORGANIZATION_GET);
        }
    }

    @Override
    public ProjectDO queryIamProject(Long projectId) {
        ResponseEntity<ProjectDO> projectDO = userFeignClient.queryIamProject(projectId);
        if (!projectDO.getStatusCode().is2xxSuccessful()) {
            throw new CommonException(ERROR_PROJECT_GET);
        }
        return projectDO.getBody();
    }

    @Override
    public List<RoleDO> roleList(String code) {
        RoleSearchDO roleSearchDO = new RoleSearchDO();
        roleSearchDO.setCode(code);
        ResponseEntity<PageInfo<RoleDO>> responseEntity = userFeignClient.roleList(roleSearchDO);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new CommonException(ERROR_ROLE_GET);
        }
        PageInfo<RoleDO> roleDOPage = responseEntity.getBody();
        if (roleDOPage != null && !roleDOPage.getList().isEmpty()) {
            return roleDOPage.getList();
        } else {
            throw new CommonException(ERROR_ROLE_GET);
        }
    }

    @Override
    public List<UserDO> pagingQueryUsersByRoleIdOnOrganizationLevel(Long roleId, Long organizationId, Long userId) {
        RoleAssignmentSearchVO roleAssignmentSearchVO = new RoleAssignmentSearchVO();
        Long[] ids = new Long[1];
        ids[0] = userId;
        List<UserDO> userDOList = userFeignClient.listUsersByIds(ids, false).getBody();
        roleAssignmentSearchVO.setLoginName(userDOList.get(0).getLoginName());
        ResponseEntity<PageInfo<UserDO>> responseEntity =
                userFeignClient.pagingQueryUsersByRoleIdOnOrganizationLevel(roleId, organizationId, 0, 0, roleAssignmentSearchVO);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new CommonException(ERROR_USER_GET);
        }
        PageInfo<UserDO> userDOPage = responseEntity.getBody();
        return userDOPage.getList();
    }

    @Override
    public List<ProjectDO> pageByProject(Long organizationId) {
        ResponseEntity<PageInfo<ProjectDO>> responseEntity = userFeignClient.pageByProject(organizationId, 0, 0);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new CommonException(ERROR_PROJECT_GET);
        }
        return responseEntity.getBody().getList();
    }

    @Override
    public List<ProjectDO> queryProjects(Long id) {
        ResponseEntity<List<ProjectDO>> responseEntity = userFeignClient.queryProjects(id, false);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new CommonException(ERROR_PROJECT_GET);
        }
        return responseEntity.getBody();
    }

    @Override
    public List<UserDO> userDOList(Long[] ids) {
        return userFeignClient.listUsersByIds(ids, false).getBody();
    }

    @Override
    public List<OrganizationDO> pageByOrganization(int page, int size) {
        ResponseEntity<PageInfo<OrganizationDO>> responseEntity = userFeignClient.pageByOrganization(page, size);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new CommonException("error.organization.get");
        }
        return responseEntity.getBody().getList();
    }
}
