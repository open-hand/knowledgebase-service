package io.choerodon.kb.infra.feign;

import org.hzero.common.HZeroService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.kb.infra.feign.fallback.IamFeignClientFallbackFactory;

/**
 * Created by Zenger on 2019/4/30.
 */
@FeignClient(value = HZeroService.Iam.NAME, fallbackFactory = IamFeignClientFallbackFactory.class)
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
     * 查询用户所在组织列表，根据into字段判断能否进入
     *
     * @param userId 用户ID
     * @return List<OrganizationDTO>
     */
    @GetMapping(value = "/choerodon/v1/users/{user_id}/organizations")
    ResponseEntity<String> listOrganizationByUserId(@PathVariable(name = "user_id") Long userId);

    /**
     * queryOrganizationById
     *
     * @param id 租户ID
     * @return OrganizationDTO
     */
    @GetMapping(value = "/choerodon/v1/organizations/{organization_id}")
    ResponseEntity<String> queryOrganizationById(@PathVariable(name = "organization_id") Long id);

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
     * 检查用户是否租户ROOT用户
     *
     * @param organizationId 租户ID
     * @param userId         用户ID
     * @return Boolean
     */
    @GetMapping(value = "/choerodon/v1/organizations/{organization_id}/users/{user_id}/check_is_root")
    ResponseEntity<String> checkIsOrgRoot(@PathVariable(name = "organization_id") Long organizationId,
                                          @PathVariable(name = "user_id") Long userId);
}

