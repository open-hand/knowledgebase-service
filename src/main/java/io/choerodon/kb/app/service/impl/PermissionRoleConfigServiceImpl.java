package io.choerodon.kb.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.kb.app.service.PermissionRoleConfigService;
import io.choerodon.kb.domain.entity.PermissionRoleConfig;
import io.choerodon.kb.domain.repository.PermissionRoleConfigRepository;

import org.hzero.core.base.BaseAppService;
import org.hzero.mybatis.helper.SecurityTokenHelper;

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
    public PermissionRoleConfig create(Long tenantId, PermissionRoleConfig permissionRoleConfig) {
        validObject(permissionRoleConfig);
        permissionRoleConfigRepository.insertSelective(permissionRoleConfig);
        return permissionRoleConfig;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionRoleConfig update(Long tenantId, PermissionRoleConfig permissionRoleConfig) {
        SecurityTokenHelper.validToken(permissionRoleConfig);
        permissionRoleConfigRepository.updateByPrimaryKeySelective(permissionRoleConfig);
        return permissionRoleConfig;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(PermissionRoleConfig permissionRoleConfig) {
        SecurityTokenHelper.validToken(permissionRoleConfig);
        permissionRoleConfigRepository.deleteByPrimaryKey(permissionRoleConfig);
    }
}
