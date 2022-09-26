package io.choerodon.kb.infra.feign.fallback;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import io.choerodon.core.utils.FeignFallbackUtil;
import io.choerodon.kb.infra.feign.AgileFeignClient;

@Component
public class AgileFeignClientFallbackFactory implements FallbackFactory<AgileFeignClient> {

    @Override
    public AgileFeignClient create(Throwable cause) {
        return FeignFallbackUtil.get(cause, AgileFeignClient.class);
    }
}
