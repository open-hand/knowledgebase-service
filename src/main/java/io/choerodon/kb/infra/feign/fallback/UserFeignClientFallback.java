package io.choerodon.kb.infra.feign.fallback;

import java.util.List;

import com.github.pagehelper.PageInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.FeignException;
import io.choerodon.kb.api.dao.RoleAssignmentSearchDTO;
import io.choerodon.kb.infra.dataobject.iam.*;
import io.choerodon.kb.infra.feign.UserFeignClient;

/**
 * Created by Zenger on 2019/4/30.
 */
@Component
public class UserFeignClientFallback implements UserFeignClient {

    private static final String BATCH_QUERY_ERROR = "error.UserFeign.queryList";
    private static final String ERROR_PROJECT_GET = "error.project.get";
    private static final String ERROR_ORGANIZATION_GET = "error.organization.get";
    private static final String ERROR_ROLE_GET = "error.role.get";

    @Override
    public ResponseEntity<List<UserDO>> listUsersByIds(Long[] ids, Boolean onlyEnabled) {
        throw new FeignException(BATCH_QUERY_ERROR);
    }

    @Override
    public ResponseEntity<OrganizationDO> queryOrgById(Long organizationId) {
        throw new FeignException(ERROR_ORGANIZATION_GET);
    }

    @Override
    public ResponseEntity<ProjectDO> queryIamProject(Long projectId) {
        throw new FeignException(ERROR_PROJECT_GET);
    }

    @Override
    public ResponseEntity<PageInfo<ProjectDO>> pageByProject(Long organizationId, int page, int size) {
        throw new FeignException(ERROR_PROJECT_GET);
    }

    @Override
    public ResponseEntity<PageInfo<UserDO>> pagingQueryUsersByRoleIdOnOrganizationLevel(Long roleId, Long sourceId, int page, int size, RoleAssignmentSearchDTO roleAssignmentSearchDTO) {
        throw new FeignException(ERROR_PROJECT_GET);
    }

    @Override
    public ResponseEntity<PageInfo<RoleDO>> roleList(RoleSearchDO role) {
        throw new FeignException(ERROR_ROLE_GET);
    }

    @Override
    public ResponseEntity<List<ProjectDO>> queryProjects(Long id, Boolean includedDisabled) {
        throw new FeignException(ERROR_PROJECT_GET);
    }

    @Override
    public ResponseEntity<PageInfo<OrganizationDO>> pageByOrganization(int page, int size) {
        throw new FeignException(ERROR_ORGANIZATION_GET);
    }
}