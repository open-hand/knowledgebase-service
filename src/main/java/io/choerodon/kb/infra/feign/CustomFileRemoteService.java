package io.choerodon.kb.infra.feign;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.kb.infra.feign.fallback.CustomFileRemoteServiceFallback;
import io.choerodon.kb.infra.feign.vo.FileVO;

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


    /**
     * 根据文件的fileKeys集合 查询文件数据
     *
     * @param organizationId
     * @param fileKeys
     * @return
     */
    @PostMapping({"/choerodon/v1/{organization_id}/file/list"})
    ResponseEntity<List<FileVO>> queryFileDTOByFileKeys(@PathVariable("organization_id") Long organizationId,
                                                   @RequestBody List<String> fileKeys);
}
