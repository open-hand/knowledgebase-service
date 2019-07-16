package io.choerodon.kb.infra.feign;

import com.github.pagehelper.PageInfo;
import io.choerodon.kb.api.dao.RoleAssignmentSearchVO;
import io.choerodon.kb.infra.dataobject.iam.*;
import io.choerodon.kb.infra.feign.fallback.UserFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by Zenger on 2019/4/30.
 */
@FeignClient(value = "iam-service", fallback = UserFeignClientFallback.class)
public interface UserFeignClient {

    @PostMapping(value = "/v1/users/ids")
    ResponseEntity<List<UserDO>> listUsersByIds(@RequestBody Long[] ids,
                                                @RequestParam(name = "only_enabled") Boolean onlyEnabled);

    @PostMapping(value = "/v1/users/login_names")
    ResponseEntity<List<UserDO>> listUsersByLogins(@RequestBody String[] loginNames,
                                                   @RequestParam(name = "only_enabled") Boolean onlyEnabled);

    @GetMapping(value = "/v1/users/{id}/projects")
    ResponseEntity<List<ProjectDO>> queryProjects(@PathVariable(name = "id") Long id,
                                                  @RequestParam(name = "included_disabled")
                                                          Boolean includedDisabled);

    @GetMapping(value = "/v1/organizations/{organization_id}")
    ResponseEntity<OrganizationDO> queryOrgById(@PathVariable(name = "organization_id") Long organizationId);

    @GetMapping(value = "/v1/projects/{projectId}")
    ResponseEntity<ProjectDO> queryIamProject(@PathVariable("projectId") Long projectId);

    @GetMapping(value = "/v1/organizations/{organization_id}/projects")
    ResponseEntity<PageInfo<ProjectDO>> pageByProject(@PathVariable(name = "organization_id") Long organizationId,
                                                      @RequestParam("page") int page, @RequestParam("size") int size);

    @PostMapping(value = "/v1/organizations/{organization_id}/role_members/users")
    ResponseEntity<PageInfo<UserDO>> pagingQueryUsersByRoleIdOnOrganizationLevel(
            @RequestParam(name = "role_id") Long roleId,
            @PathVariable(name = "organization_id") Long sourceId,
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestBody RoleAssignmentSearchVO roleAssignmentSearchVO);

    @PostMapping(value = "/v1/roles/search")
    ResponseEntity<PageInfo<RoleDO>> roleList(@RequestBody(required = false) RoleSearchDO role);

    @GetMapping(value = "/v1/organizations")
    ResponseEntity<PageInfo<OrganizationDO>> pageByOrganization(@RequestParam("page") int page, @RequestParam("size") int size);
}

