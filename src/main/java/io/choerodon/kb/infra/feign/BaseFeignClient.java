package io.choerodon.kb.infra.feign;

import java.util.List;
import java.util.Set;

import io.choerodon.core.domain.Page;
import org.hzero.common.HZeroService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.kb.api.vo.ProjectDTO;
import io.choerodon.kb.infra.feign.fallback.BaseFeignClientFallback;
import io.choerodon.kb.infra.feign.vo.OrganizationDTO;
import io.choerodon.kb.infra.feign.vo.OrganizationSimplifyDTO;
import io.choerodon.kb.infra.feign.vo.ProjectDO;
import io.choerodon.kb.infra.feign.vo.UserDO;

/**
 * Created by Zenger on 2019/4/30.
 */
@FeignClient(value = "choerodon-iam", fallback = BaseFeignClientFallback.class)
public interface BaseFeignClient {

    @PostMapping(value = "/choerodon/v1/users/ids")
    ResponseEntity<List<UserDO>> listUsersByIds(@RequestBody Long[] ids,
                                                @RequestParam(name = "only_enabled") Boolean onlyEnabled);

    /**
     * 查询用户所在组织列表，根据into字段判断能否进入
     *
     * @param userId
     * @return
     */
    @GetMapping(value = "/choerodon/v1/users/{user_id}/organizations")
    ResponseEntity<List<OrganizationDTO>> listOrganizationByUserId(@PathVariable(name = "user_id") Long userId);

    @GetMapping(value = "/choerodon/v1/organizations/{organization_id}/projects/all")
    ResponseEntity<List<ProjectDO>> listProjectsByOrgId(@PathVariable(name = "organization_id") Long organizationId);

    @GetMapping(value = "/choerodon/v1/organizations/{organization_id}")
    ResponseEntity<OrganizationDTO> query(@PathVariable(name = "organization_id") Long id);

//    @GetMapping(value = "/choerodon/v1/organizations/ids")
//    ResponseEntity<List<OrganizationDTO>> queryByIds(@RequestBody Set<Long> ids);

    @GetMapping(value = "/choerodon/v1/projects/ids")
    ResponseEntity<List<ProjectDTO>> queryProjectByIds(@RequestBody Set<Long> ids);

    @GetMapping(value = "/choerodon/v1/projects/{project_id}")
    ResponseEntity<ProjectDTO> queryProject(@PathVariable(name = "project_id") Long id);

    @GetMapping(value = "/choerodon/v1/organizations/all")
    ResponseEntity<Page<OrganizationSimplifyDTO>> getAllOrgsList(@RequestParam int page,
                                                                 @RequestParam int size);

    @GetMapping(value = "/choerodon/v1/fix/projects/all")
    ResponseEntity<List<ProjectDTO>> getAllProList();

    @GetMapping("/choerodon/v1/organizations/{organization_id}/users/{user_id}/projects")
    ResponseEntity<List<ProjectDTO>> queryOrgProjects(@PathVariable("organization_id") Long organizationId,
                                                      @PathVariable("user_id") Long userId);

    @GetMapping("/choerodon/v1/organizations/{tenant_id}/org_level")
    ResponseEntity<String> orgLevel( @PathVariable("tenant_id") Long tenantId);
}

