package io.choerodon.kb.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.kb.app.service.PermissionRangeService;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.repository.PermissionRangeRepository;

import org.hzero.core.base.BaseAppService;
import org.hzero.mybatis.helper.SecurityTokenHelper;

/**
 * 知识库权限应用范围应用服务默认实现
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
@Service
public class PermissionRangeServiceImpl extends BaseAppService implements PermissionRangeService {

    @Autowired
    private PermissionRangeRepository permissionRangeRepository;

    @Override
    public PermissionRange create(Long tenantId, PermissionRange permissionRange) {
        validObject(permissionRange);
        permissionRangeRepository.insertSelective(permissionRange);
        return permissionRange;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionRange update(Long tenantId, PermissionRange permissionRange) {
        SecurityTokenHelper.validToken(permissionRange);
        permissionRangeRepository.updateByPrimaryKeySelective(permissionRange);
        return permissionRange;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(PermissionRange permissionRange) {
        SecurityTokenHelper.validToken(permissionRange);
        permissionRangeRepository.deleteByPrimaryKey(permissionRange);
    }
}
