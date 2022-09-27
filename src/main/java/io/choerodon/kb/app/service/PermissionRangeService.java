package io.choerodon.kb.app.service;

import io.choerodon.kb.api.vo.permission.PermissionDetailVO;

/**
 * 知识库权限应用范围应用服务
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
public interface PermissionRangeService {

    /**
     * 组织创建时初始化组织默认知识库设置
     * @param organizationId 组织ID
     */
    void initPermissionRangeOnOrganizationCreate(Long organizationId);

    /**
     * 权限范围保存接口
     *
     * @param organizationId 组织ID
     * @param projectId 项目ID
     * @param permissionDetailVO permissionDetailVO
     * @return permissionDetailVO
     */
    PermissionDetailVO save(Long organizationId, Long projectId, PermissionDetailVO permissionDetailVO);
}
