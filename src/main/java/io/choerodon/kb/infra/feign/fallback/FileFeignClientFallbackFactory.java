package io.choerodon.kb.infra.feign.fallback;

import io.choerodon.core.utils.FeignFallbackUtil;
import io.choerodon.kb.infra.feign.FileFeignClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * @author superlee
 * @since 12/27/21
 */
@Component
public class FileFeignClientFallbackFactory implements FallbackFactory<FileFeignClient> {

    @Override
    public FileFeignClient create(Throwable cause) {
        return FeignFallbackUtil.get(cause, FileFeignClient.class);
    }
}