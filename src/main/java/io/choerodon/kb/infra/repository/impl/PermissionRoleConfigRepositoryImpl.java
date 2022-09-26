package io.choerodon.kb.infra.repository.impl;

import org.springframework.stereotype.Repository;

import io.choerodon.kb.domain.entity.PermissionRoleConfig;
import io.choerodon.kb.domain.repository.PermissionRoleConfigRepository;

import org.hzero.mybatis.base.impl.BaseRepositoryImpl;

/**
 * 知识库权限矩阵 资源库实现
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
@Repository
public class PermissionRoleConfigRepositoryImpl extends BaseRepositoryImpl<PermissionRoleConfig> implements PermissionRoleConfigRepository {

    @Override
    public PermissionRoleConfig findByUniqueKey(PermissionRoleConfig permissionRoleConfig) {
        if(permissionRoleConfig == null) {
            return null;
        }
        final PermissionRoleConfig queryParam = permissionRoleConfig.generateUniqueQueryParam();
        if(queryParam == null) {
            return null;
        }
        return this.selectOne(queryParam);
    }

}
