package io.choerodon.kb.infra.feign.fallback;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.infra.feign.CustomFileRemoteService;
import io.choerodon.kb.infra.feign.vo.FileVO;

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

    @Override
    public ResponseEntity<List<FileVO>> queryFileDTOByFileKeys(Long organizationId, List<String> fileKeys) {
        throw new CommonException("error.query.FileDTO.by.fileKeys");
    }

    @Override
    public ResponseEntity<FileVO> getFileDTOByFileKey(Long organizationId, String fileKey) {
        throw new CommonException("error.query.FileDTO.by.fileKey");
    }
}
