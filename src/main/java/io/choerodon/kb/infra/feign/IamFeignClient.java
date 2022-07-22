package io.choerodon.kb.infra.feign;

import io.choerodon.kb.infra.feign.fallback.IamFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by Zenger on 2019/4/30.
 */
@FeignClient(value = "choerodon-iam", fallbackFactory = IamFeignClientFallbackFactory.class)
public interface IamFeignClient {

    @GetMapping("/choerodon/v1/organizations/{organization_id}/water_mark")
    ResponseEntity<String> getWaterMarkConfig(@PathVariable("organization_id") Long organizationId);

    @GetMapping(value = "/choerodon/v1/organizations/{organization_id}/users/{user_id}/projects_simple")
    ResponseEntity<String> listProjectsByUserIdForSimple(@PathVariable("organization_id") Long organizationId,
                                                                   @PathVariable("user_id") Long userId,
                                                                   @RequestParam(required = false) String category,
                                                                   @RequestParam(required = false) Boolean enabled);

    @GetMapping(value = "/choerodon/v1/site/tenant/wps/config")
    ResponseEntity<String> queryTenantWpsConfig(@RequestParam("tenant_id") Long tenantId);
}

