package io.choerodon.kb.infra.feign.fallback;

import org.springframework.http.ResponseEntity;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.infra.feign.FileFeignClient;
import io.choerodon.kb.infra.feign.vo.*;

/**
 * @author superlee
 * @since 12/27/21
 */
public class FileFeignClientFallback implements FileFeignClient {


    @Override
    public ResponseEntity<Void> updateFile(Long organizationId, FileVO fileVO) {
        throw new CommonException("error.update.file");
    }
}
