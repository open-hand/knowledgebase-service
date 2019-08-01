package io.choerodon.kb.infra.feign;

import io.choerodon.kb.infra.feign.fallback.IamFeignClientFallback;
import io.choerodon.kb.infra.feign.vo.UserDO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Created by Zenger on 2019/4/30.
 */
@FeignClient(value = "iam-service", fallback = IamFeignClientFallback.class)
public interface IamFeignClient {

    @PostMapping(value = "/v1/users/ids")
    ResponseEntity<List<UserDO>> listUsersByIds(@RequestBody Long[] ids,
                                                @RequestParam(name = "only_enabled") Boolean onlyEnabled);
}

