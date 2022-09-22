package io.choerodon.kb.infra.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.kb.domain.entity.PermissionRoleConfig;
import io.choerodon.kb.domain.repository.PermissionRoleConfigRepository;
import io.choerodon.kb.infra.mapper.PermissionRoleConfigMapper;

import org.hzero.mybatis.base.impl.BaseRepositoryImpl;

/**
 * 知识库权限矩阵 资源库实现
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
@Component
public class PermissionRoleConfigRepositoryImpl extends BaseRepositoryImpl<PermissionRoleConfig> implements PermissionRoleConfigRepository {

    @Autowired
    private PermissionRoleConfigMapper permissionRoleConfigMapper;

}
