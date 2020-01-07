package io.choerodon.kb.infra.feign;

import com.github.pagehelper.PageInfo;

import io.choerodon.kb.api.vo.ProjectDTO;
import io.choerodon.kb.infra.feign.fallback.BaseFeignClientFallback;
import io.choerodon.kb.infra.feign.vo.OrganizationDTO;
import io.choerodon.kb.infra.feign.vo.ProjectDO;
import io.choerodon.kb.infra.feign.vo.UserDO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Set;
import javax.validation.Valid;

/**
 * Created by Zenger on 2019/4/30.
 */
@FeignClient(value = "base-service", fallback = BaseFeignClientFallback.class)
public interface BaseFeignClient {

    @PostMapping(value = "/v1/users/ids")
    ResponseEntity<List<UserDO>> listUsersByIds(@RequestBody Long[] ids,
                                                @RequestParam(name = "only_enabled") Boolean onlyEnabled);

    /**
     * 查询用户所在组织列表，根据into字段判断能否进入
     *
     * @param userId
     * @return
     */
    @GetMapping(value = "/v1/users/{user_id}/organizations")
    ResponseEntity<List<OrganizationDTO>> listOrganizationByUserId(@PathVariable(name = "user_id") Long userId);

    @GetMapping(value = "/v1/organizations/{organization_id}/projects/all")
    ResponseEntity<List<ProjectDO>> listProjectsByOrgId(@PathVariable(name = "organization_id") Long organizationId);

    @GetMapping(value = "/v1/organizations/{organization_id}")
    ResponseEntity<OrganizationDTO> query(@PathVariable(name = "organization_id") Long id);

    @GetMapping(value = "/v1/organizations/ids")
    ResponseEntity<List<OrganizationDTO>> queryByIds(@RequestBody Set<Long> ids);

    @GetMapping(value = "/v1/projects/ids")
    ResponseEntity<List<ProjectDTO>> queryProjectByIds(@RequestBody Set<Long> ids);
}

