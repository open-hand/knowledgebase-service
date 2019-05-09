package io.choerodon.kb.infra.feign.fallback;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.infra.feign.FileFeignClient;

/**
 * Created by Zenger on 2019/4/30.
 */
@Component
public class FileFeignClientFallback implements FileFeignClient {

    @Override
    public ResponseEntity<String> uploadFile(String bucketName, String fileName, MultipartFile multipartFile) {
        throw new CommonException("error.file.upload");
    }

    @Override
    public ResponseEntity deleteFile(String bucketName, String url) {
        throw new CommonException("error.file.delete");
    }
}
