package io.choerodon.kb.infra.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.choerodon.kb.infra.feign.fallback.CustomFileRemoteServiceFallbackFactory;

import org.hzero.common.HZeroService;

/**
 * @author superlee
 * @since 2020-05-22
 */
@FeignClient( value = HZeroService.File.NAME, fallbackFactory = CustomFileRemoteServiceFallbackFactory.class )
public interface CustomFileRemoteService {

    /**
     * deleteFileByUrl
     * @param organizationId organizationId
     * @param bucketName bucketName
     * @param urls urls
     * @return Void
     */
    @PostMapping({"/choerodon/v1/{organizationId}/delete-by-url"})
    ResponseEntity<String> deleteFileByUrl(@PathVariable("organizationId") Long organizationId,
                                   @RequestParam("bucketName") String bucketName,
                                   @RequestBody List<String> urls);


    /**
     * 根据文件的fileKeys集合 查询文件数据
     *
     * @param organizationId
     * @param fileKeys
     * @return List&lt;FileVO&gt;
     */
    @PostMapping({"/choerodon/v1/{organization_id}/file/list"})
    ResponseEntity<String> queryFileDTOByFileKeys(@PathVariable("organization_id") Long organizationId,
                                                   @RequestBody List<String> fileKeys);
}
