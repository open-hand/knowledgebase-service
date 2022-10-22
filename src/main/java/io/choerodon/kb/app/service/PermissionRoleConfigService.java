package io.choerodon.kb.app.service;

import java.util.Collection;
import java.util.List;

import io.choerodon.kb.domain.entity.PermissionRoleConfig;

/**
 * 知识库权限矩阵应用服务
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
public interface PermissionRoleConfigService {

    /**
     * 批量创建或更新知识库权限矩阵
     *
     * @param tenantId          租户ID
     * @param projectId         项目ID
     * @param permissionRoleConfigs   知识库权限矩阵
     * @return 知识库权限矩阵
     */
    List<PermissionRoleConfig> batchCreateOrUpdate(Long tenantId, Long projectId, Collection<PermissionRoleConfig> permissionRoleConfigs);
    
}
