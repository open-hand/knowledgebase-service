package io.choerodon.kb.infra.feign.fallback;

import io.choerodon.kb.infra.feign.AgileFeignClient;
import io.choerodon.kb.infra.utils.FeignFallbackUtil;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class AgileFeignClientFallbackFactrory implements FallbackFactory<AgileFeignClient> {

    @Override
    public AgileFeignClient create(Throwable cause) {
        return FeignFallbackUtil.get(cause, AgileFeignClient.class);
    }
}
