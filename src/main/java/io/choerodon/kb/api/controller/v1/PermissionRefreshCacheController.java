package io.choerodon.kb.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.domain.service.PermissionRefreshCacheDomainService;
import io.choerodon.swagger.annotation.Permission;

import org.hzero.core.util.Results;

/**
 * @author superlee
 * @since 2022-10-11
 */
@RestController("permissionRefreshCacheController.v1")
@RequestMapping("/v1/permission/{type}/cache/refresh")
public class PermissionRefreshCacheController {

    @Autowired
    private PermissionRefreshCacheDomainService permissionRefreshCacheService;

    @ApiOperation(value = "根据type刷新知识库redis缓存")
    @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
    @PostMapping
    public ResponseEntity<Void> refreshTargetParentCache(@PathVariable String type) {
        permissionRefreshCacheService.refreshCache(type);
        return Results.success();
    }
}
