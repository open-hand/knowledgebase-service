package io.choerodon.kb.domain.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import io.choerodon.kb.domain.repository.PermissionRangeKnowledgeObjectSettingRepository;
import io.choerodon.kb.domain.repository.PermissionRoleConfigRepository;
import io.choerodon.kb.domain.repository.WorkSpaceRepository;
import io.choerodon.kb.domain.service.PermissionRefreshCacheDomainService;
import io.choerodon.kb.infra.enums.PermissionConstants;

/**
 * @author superlee
 * @since 2022-10-11
 */
@Service
public class PermissionRefreshCacheDomainServiceImpl implements PermissionRefreshCacheDomainService {

    @Autowired
    private PermissionRoleConfigRepository permissionRoleConfigRepository;
    // 这里只要是PermissionRangeBaseRepository的实现类就行, 所以随意注入了一个子类
    @Autowired
    private PermissionRangeKnowledgeObjectSettingRepository permissionRangeRepository;
    @Autowired
    private WorkSpaceRepository workSpaceRepository;

    @Override
    public void refreshCache(PermissionConstants.PermissionRefreshType refreshType) {
        Assert.notNull(refreshType, "error.illegal.permission.refresh.type");
        switch (refreshType) {
            case ROLE_CONFIG:
                this.permissionRoleConfigRepository.reloadCache();
                break;
            case RANGE:
                this.permissionRangeRepository.clearCache();
                break;
            case SECURITY_CONFIG:
                // TODO
                break;
            case TARGET_PARENT:
                this.workSpaceRepository.reloadTargetParentMappingToRedis();
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
