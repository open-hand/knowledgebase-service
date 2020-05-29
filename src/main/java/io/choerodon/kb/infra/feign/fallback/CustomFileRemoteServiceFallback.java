package io.choerodon.kb.infra.feign.fallback;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.infra.feign.CustomFileRemoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author superlee
 * @since 2020-05-22
 */
@Component
public class CustomFileRemoteServiceFallback implements CustomFileRemoteService {

    private static final Logger logger = LoggerFactory.getLogger(CustomFileRemoteServiceFallback.class);

    @Override
    public ResponseEntity deleteFileByUrl(Long organizationId, String bucketName, List<String> urls) {
        logger.error("Delete file failed,organizationId = {}, bucketName = {}.", organizationId, bucketName);
        throw new CommonException("File service is not available, please check", new Object[0]);
    }
}
