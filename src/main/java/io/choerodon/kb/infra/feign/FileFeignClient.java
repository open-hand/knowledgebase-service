package io.choerodon.kb.infra.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.kb.infra.config.FeignMultipartSupportConfig;
import io.choerodon.kb.infra.feign.fallback.FileFeignClientFallback;

/**
 * Created by Zenger on 2019/4/30.
 */
@FeignClient(value = "file-service", fallback = FileFeignClientFallback.class, configuration = FeignMultipartSupportConfig.class)
public interface FileFeignClient {

    @PostMapping(value = "/v1/files",
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<String> uploadFile(@RequestParam("bucket_name") String bucketName,
                                      @RequestParam("file_name") String fileName,
                                      @RequestPart("file") MultipartFile multipartFile);

    @DeleteMapping(value = "/v1/files")
    ResponseEntity deleteFile(@RequestParam("bucket_name") String bucketName,
                              @RequestParam("url") String url);
}
