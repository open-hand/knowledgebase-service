package io.choerodon.kb.infra.feign;

import io.choerodon.kb.api.vo.ProjectDTO;
import io.choerodon.kb.api.vo.WatermarkVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by Zenger on 2019/4/30.
 */
@FeignClient(value = "choerodon-iam", fallbackFactory = IamFallbackFactory.class)
public interface IamFeignClient {

    @GetMapping("/choerodon/v1/organizations/{organization_id}/water_mark")
    ResponseEntity<WatermarkVO> getWaterMarkConfig(@PathVariable("organization_id") Long organizationId);

    @GetMapping(value = "/choerodon/v1/organizations/{organization_id}/users/{user_id}/projects_simple")
    ResponseEntity<List<ProjectDTO>> listProjectsByUserIdForSimple(@PathVariable("organization_id") Long organizationId,
                                                                   @PathVariable("user_id") Long userId,
                                                                   @RequestParam(required = false) String category,
                                                                   @RequestParam(required = false) Boolean enabled);
}

