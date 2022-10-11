package io.choerodon.kb.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import io.choerodon.kb.app.service.PermissionRefreshCacheService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.infra.enums.PermissionRefreshType;

/**
 * @author superlee
 * @since 2022-10-11
 */
@Service
public class PermissionRefreshCacheServiceImpl implements PermissionRefreshCacheService {

    @Autowired
    private WorkSpaceService workSpaceService;

    @Override
    public void refreshCache(String type) {
        PermissionRefreshType permissionRefreshType = PermissionRefreshType.ofKebabCaseName(type);
        Assert.notNull(permissionRefreshType, "error.illegal.permission.refresh.type");
        switch (permissionRefreshType) {
            case ROLE_CONFIG:
                break;
            case RANGE:
                break;
            case SECURITY_CONFIG:
                break;
            case TARGET_PARENT:
                workSpaceService.reloadTargetParentMappingToRedis();
                break;
            default:
                break;
        }
    }
}
