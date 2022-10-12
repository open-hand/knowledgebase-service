package io.choerodon.kb.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import io.choerodon.kb.app.service.PermissionRefreshCacheService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.domain.repository.PermissionRoleConfigRepository;
import io.choerodon.kb.infra.enums.PermissionConstants;

/**
 * @author superlee
 * @since 2022-10-11
 */
@Service
public class PermissionRefreshCacheServiceImpl implements PermissionRefreshCacheService {

    @Autowired
    private PermissionRoleConfigRepository permissionRoleConfigRepository;
    @Autowired
    private WorkSpaceService workSpaceService;

    @Override
    public void refreshCache(PermissionConstants.PermissionRefreshType refreshType) {
        Assert.notNull(refreshType, "error.illegal.permission.refresh.type");
        switch (refreshType) {
            case ROLE_CONFIG:
                this.permissionRoleConfigRepository.reloadCache();
                break;
            case RANGE:
                // TODO
                break;
            case SECURITY_CONFIG:
                // TODO
                break;
            case TARGET_PARENT:
                this.workSpaceService.reloadTargetParentMappingToRedis();
                break;
            default:
                break;
        }
    }

    @Override
    public void refreshCache(String type) {
        this.refreshCache(PermissionConstants.PermissionRefreshType.ofKebabCaseName(type));
    }
}
