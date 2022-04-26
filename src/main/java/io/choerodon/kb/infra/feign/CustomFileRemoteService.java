package io.choerodon.kb.infra.feign;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.choerodon.kb.infra.feign.fallback.CustomFileRemoteServiceFallback;

/**
 * @author superlee
 * @since 2020-05-22
 */
@FeignClient(
        value = "choerodon-file",
        fallback = CustomFileRemoteServiceFallback.class
)
public interface CustomFileRemoteService {

    @PostMapping({"/choerodon/v1/{organizationId}/delete-by-url"})
    ResponseEntity deleteFileByUrl(@PathVariable("organizationId") Long organizationId,
                                   @RequestParam("bucketName") String bucketName,
                                   @RequestBody List<String> urls);
}
