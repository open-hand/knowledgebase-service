package io.choerodon.kb.infra.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.choerodon.kb.infra.feign.fallback.AsgardFeignClientFallbackFactory;


/**
 * @author dengyouquan
 **/
@FeignClient(value = "choerodon-asgard", fallbackFactory = AsgardFeignClientFallbackFactory.class)
public interface AsgardFeignClient {

    /**
     * queryByRefTypeAndRefIds
     * @param refType refType
     * @param refIds refIds
     * @param sagaCode sagaCode
     * @return List<SagaInstanceDetails>
     */
    @GetMapping("/v1/sagas/instances/ref/business/instance")
    ResponseEntity<String> queryByRefTypeAndRefIds(@RequestParam(value = "refType") String refType,
                                                                      @RequestParam(value = "refIds") List<String> refIds,
                                                                      @RequestParam(value = "sagaCode") String sagaCode);
}
