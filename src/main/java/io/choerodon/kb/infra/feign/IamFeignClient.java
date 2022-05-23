package io.choerodon.kb.infra.feign;

import io.choerodon.core.domain.Page;
import io.choerodon.kb.api.vo.ProjectDTO;
import io.choerodon.kb.infra.feign.fallback.IamFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * Created by Zenger on 2019/4/30.
 */
@FeignClient(value = "choerodon-iam", fallback = IamFeignClientFallback.class)
public interface IamFeignClient {


    @GetMapping(value = "/choerodon/v1/organizations/{organization_id}/projects")
    ResponseEntity<Page<ProjectDTO>> pagingQuery(@PathVariable(name = "organization_id") Long organizationId,
                                                 @RequestParam Integer page,
                                                 @RequestParam Integer size,
                                                 @RequestParam(required = false) String name,
                                                 @RequestParam(required = false) String code,
                                                 @RequestParam(required = false) Boolean enabled,
                                                 @RequestParam(required = false, defaultValue = "false") Boolean withAdditionInfo,
                                                 @RequestParam(required = false) String params);

}

