package io.choerodon.kb.infra.feign;

import java.util.Collection;
import java.util.Set;
import javax.validation.Valid;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.kb.api.vo.RoleAssignmentSearchVO;
import io.choerodon.kb.infra.feign.fallback.IamFeignClientFallbackFactory;

/**
 * Created by Zenger on 2019/4/30.
 */
@FeignClient(value = "choerodon-iam", fallbackFactory = IamFeignClientFallbackFactory.class)
public interface IamFeignClient {

    /**
     * 根据用户ID查询用户信息
     *
     * @param ids         用户ID
     * @param onlyEnabled 是否只查询启用的
     * @return List<UserDO>
     */
    @PostMapping(value = "/choerodon/v1/users/ids")
    ResponseEntity<String> listUsersByIds(@RequestBody Long[] ids,
                                          @RequestParam(name = "only_enabled") boolean onlyEnabled);

    /**
     * 查询project层角色,附带该角色下分配的用户数
     *
     * @param projectId              项目id
     * @param roleAssignmentSearchVO 角色查询vo
     */
    @PostMapping(value = "/choerodon/v1/projects/{project_id}/role_members/users/count")
    ResponseEntity<String> listRolesWithUserCountOnProjectLevel(
            @PathVariable(name = "project_id") Long projectId,
            @RequestBody(required = false) @Valid RoleAssignmentSearchVO roleAssignmentSearchVO);

    @GetMapping(value = "/choerodon/v1/organizations/{organization_id}/roles")
    ResponseEntity<String> listRolesOnOrganizationLevel(
            @PathVariable(name = "organization_id") Long organizationId,
            @RequestParam(name = "label_name", required = false) String labelName,
            @RequestParam(name = "only_select_enable") Boolean onlySelectEnable);

    /**
     * 查询租户层角色,附带该角色下分配的用户数
     *
     * @param organizationId         租户id
     * @param roleAssignmentSearchVO 角色查询vo
     */
    @PostMapping(value = "/choerodon/v1/organizations/{organizationId}/role_members/users/count")
    ResponseEntity<String> listRolesWithUserCountOnOrganizationLevel(
            @PathVariable Long organizationId,
            @RequestBody(required = false) @Valid RoleAssignmentSearchVO roleAssignmentSearchVO);

    /**
     * 根据id查询角色信息
     *
     * @param tenantId 租户id
     * @param roleIds  角色id集合
     */
    @PostMapping(value = "/choerodon/v1/list_roles")
    ResponseEntity<String> listRolesByIds(@RequestParam("tenantId") Long tenantId,
                                          @RequestBody Collection<Long> roleIds);

    /**
     * @param organizationId
     * @return
     */
    @GetMapping("/choerodon/v1/organizations/{organization_id}/work_group/list")
    ResponseEntity<String> listWorkGroups(@PathVariable(name = "organization_id") Long organizationId);

    /**
     * 查询用户所在组织列表，根据into字段判断能否进入
     *
     * @param userId 用户ID
     * @return List<OrganizationDTO>
     */
    @GetMapping(value = "/choerodon/v1/users/{user_id}/organizations")
    ResponseEntity<String> listOrganizationByUserId(@PathVariable(name = "user_id") Long userId);

    /**
     * listProjectsByOrgId
     *
     * @param organizationId 租户ID
     * @return List<ProjectDO>
     */
    @GetMapping(value = "/choerodon/v1/organizations/{organization_id}/projects/all")
    ResponseEntity<String> listProjectsByOrgId(@PathVariable(name = "organization_id") Long organizationId);

    /**
     * 分页查询组织下的项目(不包含本项目)
     *
     * @param organizationId    组织ID
     * @param page              当前页面
     * @param size              页面大小
     * @param param             查询条件
     * @param topProjectIds     置顶项目id
     * @param ignoredProjectIds 需要排除的项目id
     * @return Page<ProjectDO>
     */
    @GetMapping(value = "/choerodon/v1/organizations/{organization_id}/projects/list_and_top")
    ResponseEntity<String> pageProjectInfo(@PathVariable(name = "organization_id") Long organizationId,
                                           @RequestParam Integer page,
                                           @RequestParam Integer size,
                                           @RequestParam String param,
                                           @RequestParam Set<Long> topProjectIds,
                                           @RequestParam Set<Long> ignoredProjectIds);

    /**
     * queryOrganizationById
     *
     * @param id 租户ID
     * @return OrganizationDTO
     */
    @GetMapping(value = "/choerodon/v1/organizations/{organization_id}")
    ResponseEntity<String> queryOrganizationById(@PathVariable(name = "organization_id") Long id);

//    @GetMapping(value = "/choerodon/v1/organizations/ids")
//    ResponseEntity<List<OrganizationDTO>> queryByIds(@RequestBody Set<Long> ids);

    /**
     * queryProjectByIds
     *
     * @param ids ids
     * @return List<ProjectDTO>
     */
    @PostMapping(value = "/choerodon/v1/projects/ids")
    ResponseEntity<String> queryProjectByIds(@RequestBody Set<Long> ids);

    /**
     * 跟项目ID查询项目
     *
     * @param id 项目ID
     * @return ProjectDTO
     */
    @GetMapping(value = "/choerodon/v1/projects/{project_id}")
    ResponseEntity<String> queryProjectById(@PathVariable(name = "project_id") Long id);

    /**
     * 分页查询项目信息
     *
     * @param page 当前页码
     * @param size 每页大小
     * @return Page<OrganizationSimplifyDTO>
     */
    @GetMapping(value = "/choerodon/v1/organizations/all")
    ResponseEntity<String> pageOrganizations(@RequestParam int page, @RequestParam int size);

    /**
     * 获取所有项目信息
     *
     * @return List<ProjectDTO>
     */
    @GetMapping(value = "/choerodon/v1/fix/projects/all")
    ResponseEntity<String> getAllProjects();

    /**
     * 获取用户在当前组织的项目信息
     * @param organizationId    组织ID
     * @param userId            用户ID
     * @return                  List&lt;ProjectDTO&gt;
     */
    @GetMapping("/choerodon/v1/organizations/{organization_id}/users/{user_id}/projects")
    ResponseEntity<String> queryOrgProjects(@PathVariable("organization_id") Long organizationId,
                                                      @PathVariable("user_id") Long userId);

    /**
     * 根据租户ID查询租户层级
     *
     * @param tenantId 租户ID
     * @return String
     */
    @GetMapping("/choerodon/v1/organizations/{tenant_id}/org_level")
    ResponseEntity<String> orgLevel(@PathVariable("tenant_id") Long tenantId);

    /**
     * 检查用户是否租户ROOT用户
     *
     * @param organizationId 租户ID
     * @param userId         用户ID
     * @return Boolean
     */
    @GetMapping(value = "/choerodon/v1/organizations/{organization_id}/users/{user_id}/check_is_root")
    ResponseEntity<String> checkIsOrgRoot(@PathVariable(name = "organization_id") Long organizationId,
                                          @PathVariable(name = "user_id") Long userId);

    /**
     * checkAdminPermission
     *
     * @param projectId projectId
     * @return Boolean
     */
    @GetMapping(value = "/choerodon/v1/projects/{project_id}/check_admin_permission")
    ResponseEntity<String> checkAdminPermission(@PathVariable(name = "project_id") Long projectId);

    /**
     * queryOrgProjectsOptional
     *
     * @param organizationId organizationId
     * @param userId         userId
     * @return List<ProjectDTO>
     */
    @GetMapping("/choerodon/v1/organizations/{organization_id}/users/{user_id}/projects/optional")
    ResponseEntity<String> queryOrgProjectsOptional(@PathVariable("organization_id") Long organizationId,
                                                    @PathVariable("user_id") Long userId);

    /**
     * getWaterMarkConfig
     *
     * @param organizationId organizationId
     * @return WatermarkVO
     */
    @GetMapping("/choerodon/v1/organizations/{organization_id}/water_mark")
    ResponseEntity<String> getWaterMarkConfig(@PathVariable("organization_id") Long organizationId);

    /**
     * listProjectsByUserIdForSimple
     *
     * @param organizationId organizationId
     * @param userId         userId
     * @param category       category
     * @param enabled        enabled
     * @return List<ProjectDTO>
     */
    @GetMapping(value = "/choerodon/v1/organizations/{organization_id}/users/{user_id}/projects_simple")
    ResponseEntity<String> listProjectsByUserIdForSimple(@PathVariable("organization_id") Long organizationId,
                                                         @PathVariable("user_id") Long userId,
                                                         @RequestParam(required = false) String category,
                                                         @RequestParam(required = false) Boolean enabled);

    /**
     * queryTenantWpsConfig
     *
     * @param tenantId tenantId
     * @return TenantWpsConfigVO
     */
    @GetMapping(value = "/choerodon/v1/site/tenant/wps/config")
    ResponseEntity<String> queryTenantWpsConfig(@RequestParam("tenant_id") Long tenantId);

    @GetMapping(value = "/choerodon/v1/organizations/{organizationId}/knowledge/user-info")
    ResponseEntity<String> queryUserInfo(@PathVariable("organizationId") Long organizationId,
                                         @RequestParam("userId") Long userId,
                                         @RequestParam("projectId") Long projectId);
}

