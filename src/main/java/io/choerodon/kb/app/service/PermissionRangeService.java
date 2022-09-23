package io.choerodon.kb.app.service;

import io.choerodon.kb.api.vo.permission.OrganizationPermissionSettingVO;
import io.choerodon.kb.domain.entity.PermissionRange;

/**
 * 知识库权限应用范围应用服务
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
public interface PermissionRangeService {

    /**
     * 创建知识库权限应用范围
     *
     * @param tenantId        租户ID
     * @param permissionRange 知识库权限应用范围
     * @return 知识库权限应用范围
     */
    PermissionRange create(Long tenantId, PermissionRange permissionRange);

    /**
     * 更新知识库权限应用范围
     *
     * @param tenantId        租户ID
     * @param permissionRange 知识库权限应用范围
     * @return 知识库权限应用范围
     */
    PermissionRange update(Long tenantId, PermissionRange permissionRange);

    /**
     * 删除知识库权限应用范围
     *
     * @param permissionRange 知识库权限应用范围
     */
    void remove(PermissionRange permissionRange);

    /**
     * 查询组织知识库权限设置
     *
     * @param organizationId 组织id
     * @return
     */
    OrganizationPermissionSettingVO queryOrgPermissionSetting(Long organizationId);
}
