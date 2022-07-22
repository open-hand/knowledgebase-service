package io.choerodon.kb.infra.feign.fallback;

import io.choerodon.core.utils.FeignFallbackUtil;
import io.choerodon.kb.infra.feign.IamFeignClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * @author superlee
 * @author archibald
 * @since 12/27/21
 */
@Component
public class IamFeignClientFallbackFactory implements FallbackFactory<IamFeignClient> {

    @Override
    public IamFeignClient create(Throwable cause) {
        return FeignFallbackUtil.get(cause, IamFeignClient.class);
    }
}
