package io.choerodon.kb.infra.feign.fallback;

import io.choerodon.kb.infra.feign.CustomFileRemoteService;
import io.choerodon.kb.infra.utils.FeignFallbackUtil;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * @author superlee
 * @since 2020-05-22
 */
@Component
public class CustomFileRemoteServiceFallbackFactory implements FallbackFactory<CustomFileRemoteService> {

    @Override
    public CustomFileRemoteService create(Throwable cause) {
        return FeignFallbackUtil.get(cause, CustomFileRemoteService.class);
    }
}
