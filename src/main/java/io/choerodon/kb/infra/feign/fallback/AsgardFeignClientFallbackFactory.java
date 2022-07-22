package io.choerodon.kb.infra.feign.fallback;

import io.choerodon.kb.infra.feign.AsgardFeignClient;
import io.choerodon.kb.infra.utils.FeignFallbackUtil;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * @author dengyouquan
 **/
@Component
public class AsgardFeignClientFallbackFactory implements FallbackFactory<AsgardFeignClient> {

    @Override
    public AsgardFeignClient create(Throwable cause) {
        return FeignFallbackUtil.get(cause, AsgardFeignClient.class);
    }
}
