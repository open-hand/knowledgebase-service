package io.choerodon.kb.infra.feign.fallback;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.infra.feign.AgileFeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class AgileFeignClientFallback implements AgileFeignClient {

    @Override
    public ResponseEntity moveWikiRelation() {
        throw new CommonException("error.moveWikiRelation.post");
    }
}
