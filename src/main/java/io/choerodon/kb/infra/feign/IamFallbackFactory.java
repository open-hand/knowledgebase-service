package io.choerodon.kb.infra.feign;

import io.choerodon.kb.api.vo.WatermarkVO;
import io.choerodon.kb.infra.feign.fallback.IamFeignClientFallback;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * @author superlee
 * @since 12/27/21
 */
@Component
public class IamFallbackFactory implements FallbackFactory<IamFeignClient> {

    /**
     * FallbackFactory 获取feign 源异常
     *
     * @param cause
     * @return
     */
    @Override
    public IamFeignClient create(Throwable cause) {
        IamFeignClientFallback iamFeignClientFallback = new IamFeignClientFallback(cause);
        return new IamFeignClient() {
            @Override
            public ResponseEntity<WatermarkVO> getWaterMarkConfig(Long organizationId) {
                return iamFeignClientFallback.getWaterMarkConfig(organizationId);
            }
        };
    }
}