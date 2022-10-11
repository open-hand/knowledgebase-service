package io.choerodon.kb.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.app.service.PermissionRefreshCacheService;
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
    private PermissionRefreshCacheService permissionRefreshCacheService;

    @ApiOperation(value = "根据type刷新知识库redis缓存")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity refreshTargetParentCache(@PathVariable String type) {
        permissionRefreshCacheService.refreshCache(type);
        return Results.success();
    }
}
