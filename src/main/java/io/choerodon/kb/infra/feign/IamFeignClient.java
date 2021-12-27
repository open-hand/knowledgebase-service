package io.choerodon.kb.infra.feign;

import io.choerodon.kb.api.vo.WatermarkVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Zenger on 2019/4/30.
 */
@FeignClient(value = "choerodon-iam", fallbackFactory = IamFallbackFactory.class)
public interface IamFeignClient {

    @GetMapping("/choerodon/v1/organizations/{organization_id}/water_mark")
    ResponseEntity<WatermarkVO> getWaterMarkConfig(@PathVariable("organization_id") Long organizationId);
}

