package io.choerodon.kb.infra.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import io.choerodon.kb.infra.feign.vo.*;


/**
 * Created by Zenger on 2019/4/30.
 */
@FeignClient(value = "choerodon-file", fallbackFactory = IamFallbackFactory.class)
public interface FileFeignClient {

    @PutMapping("/choerodon/v1/{organization_id}/update_file")
    ResponseEntity<Void> updateFile(@PathVariable("organization_id") Long organizationId,
                                    @RequestBody FileVO fileVO);



}

