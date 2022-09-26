package io.choerodon.kb.app.service.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.kb.app.service.PermissionRoleConfigService;
import io.choerodon.kb.domain.entity.PermissionRoleConfig;
import io.choerodon.kb.domain.repository.PermissionRoleConfigRepository;

import org.hzero.core.base.BaseAppService;
import org.hzero.core.base.BaseConstants;

/**
 * 知识库权限矩阵应用服务默认实现
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
@Service
public class PermissionRoleConfigServiceImpl extends BaseAppService implements PermissionRoleConfigService {

    @Autowired
    private PermissionRoleConfigRepository permissionRoleConfigRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PermissionRoleConfig> batchCreateOrUpdate(Long tenantId, Long projectId, Collection<PermissionRoleConfig> permissionRoleConfigs) {
        Assert.notNull(tenantId, BaseConstants.ErrorCode.NOT_NULL);
        Assert.notNull(projectId, BaseConstants.ErrorCode.NOT_NULL);
        if(CollectionUtils.isEmpty(permissionRoleConfigs)) {
            return Collections.emptyList();
        }
        return permissionRoleConfigs.stream().map(permissionRoleConfig -> {
            permissionRoleConfig.setOrganizationId(tenantId).setProjectId(projectId);
            final PermissionRoleConfig entityInDb = this.permissionRoleConfigRepository.findByUniqueKey(permissionRoleConfig);
            if(entityInDb == null) {
                permissionRoleConfig.setId(null);
                permissionRoleConfig = permissionRoleConfig.validateAndProcessBeforeCreate();
                this.permissionRoleConfigRepository.insertSelective(permissionRoleConfig);
            } else {
                permissionRoleConfig.setId(entityInDb.getId());
                permissionRoleConfig.setObjectVersionNumber(entityInDb.getObjectVersionNumber());
                permissionRoleConfig = permissionRoleConfig.validateAndProcessBeforeUpdate();
                this.permissionRoleConfigRepository.updateOptional(permissionRoleConfig, PermissionRoleConfig.FIELD_AUTHORIZE_FLAG);
            }
            return permissionRoleConfig;
        }).collect(Collectors.toList());
    }
}
