package io.choerodon.kb.app.service;

import io.choerodon.kb.domain.entity.PermissionRoleConfig;

/**
 * 知识库权限矩阵应用服务
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
public interface PermissionRoleConfigService {

    /**
     * 创建知识库权限矩阵
     *
     * @param tenantId             租户ID
     * @param permissionRoleConfig 知识库权限矩阵
     * @return 知识库权限矩阵
     */
    PermissionRoleConfig create(Long tenantId, PermissionRoleConfig permissionRoleConfig);

    /**
     * 更新知识库权限矩阵
     *
     * @param tenantId             租户ID
     * @param permissionRoleConfig 知识库权限矩阵
     * @return 知识库权限矩阵
     */
    PermissionRoleConfig update(Long tenantId, PermissionRoleConfig permissionRoleConfig);

    /**
     * 删除知识库权限矩阵
     *
     * @param permissionRoleConfig 知识库权限矩阵
     */
    void remove(PermissionRoleConfig permissionRoleConfig);
}
