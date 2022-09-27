package io.choerodon.kb.domain.service;

import java.util.List;

import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.choerodon.kb.domain.entity.PermissionRange;

/**
 * 权限范围知识对象设置 领域Service
 * @author gaokuo.dai@zknow.com 2022-09-27
 */
public interface PermissionRangeKnowledgeObjectSettingService {

    /**
     * 权限范围和安全设置保存接口
     *
     * @param organizationId 组织ID
     * @param projectId 项目ID
     * @param permissionDetailVO permissionDetailVO
     * @return permissionDetailVO
     */
    PermissionDetailVO saveRangeAndSecurity(Long organizationId, Long projectId, PermissionDetailVO permissionDetailVO);

    /**
     * 权限范围保存接口
     *
     * @param organizationId 组织ID
     * @param projectId 项目ID
     * @param permissionDetailVO permissionDetailVO
     * @return permissionDetailVO
     */
    PermissionDetailVO saveRange(Long organizationId, Long projectId, PermissionDetailVO permissionDetailVO);

    /**
     *
     * @param organizationId
     * @param projectId
     * @param targetType
     * @param targetValue
     * @return
     */
    List<PermissionRange> queryFolderOrFileCollaborator(Long organizationId, Long projectId, String targetType, Long targetValue);
}
