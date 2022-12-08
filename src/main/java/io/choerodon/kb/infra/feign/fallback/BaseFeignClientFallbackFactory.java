package io.choerodon.kb.infra.feign.fallback;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import io.choerodon.core.utils.FeignFallbackUtil;
import io.choerodon.kb.infra.feign.BaseFeignClient;
import io.choerodon.kb.infra.feign.IamFeignClient;

/**
 * Created by Zenger on 2019/4/30.
 */
@Component
public class BaseFeignClientFallbackFactory implements FallbackFactory<BaseFeignClient> {

    @Override
    public BaseFeignClient create(Throwable cause) {
        return FeignFallbackUtil.get(cause, BaseFeignClient.class);

    }
}
