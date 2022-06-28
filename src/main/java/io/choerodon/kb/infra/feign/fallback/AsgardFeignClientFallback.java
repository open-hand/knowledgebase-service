package io.choerodon.kb.infra.feign.fallback;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.infra.feign.AsgardFeignClient;
import io.choerodon.kb.infra.feign.vo.SagaInstanceDetails;

/**
 * @author dengyouquan
 **/
@Component
public class AsgardFeignClientFallback implements AsgardFeignClient {

    @Override
    public ResponseEntity<List<SagaInstanceDetails>> queryByRefTypeAndRefIds(String refType, List<String> refIds, String sagaCode) {
        throw new CommonException("error.query.instance.detail");
    }
}