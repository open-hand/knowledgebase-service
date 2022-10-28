package io.choerodon.kb.domain.service;

import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.choerodon.kb.infra.enums.PermissionConstants;

/**
 * 权限范围知识对象设置 领域Service
 *
 * @author gaokuo.dai@zknow.com 2022-09-27
 */
public interface PermissionRangeKnowledgeObjectSettingService {

    /**
     * 权限范围和安全设置保存接口
     *
     * @param organizationId     组织ID
     * @param projectId          项目ID
     * @param permissionDetailVO permissionDetailVO
     * @return permissionDetailVO
     */
    default PermissionDetailVO saveRangeAndSecurity(Long organizationId, Long projectId, PermissionDetailVO permissionDetailVO) {
        return this.saveRangeAndSecurity(organizationId, projectId, permissionDetailVO, true);
    }
    /**
     * 权限范围和安全设置保存接口
     *
     * @param organizationId     组织ID
     * @param projectId          项目ID
     * @param permissionDetailVO permissionDetailVO
     * @param checkPermission    是否校验更新权限
     * @return permissionDetailVO
     */
    PermissionDetailVO saveRangeAndSecurity(Long organizationId, Long projectId, PermissionDetailVO permissionDetailVO, boolean checkPermission);

    /**
     * 权限范围保存接口
     *
     * @param organizationId     组织ID
     * @param projectId          项目ID
     * @param permissionDetail   permissionDetail
     * @return permissionDetailVO
     */
    default PermissionDetailVO saveRange(Long organizationId, Long projectId, PermissionDetailVO permissionDetail) {
        return this.saveRange(organizationId, projectId, permissionDetail, true);
    }

    /**
     * 权限范围保存接口
     *
     * @param organizationId     组织ID
     * @param projectId          项目ID
     * @param permissionDetail   permissionDetail
     * @param checkPermission    是否校验更新权限
     * @return permissionDetailVO
     */
    PermissionDetailVO saveRange(Long organizationId, Long projectId, PermissionDetailVO permissionDetail, boolean checkPermission);

    /**
     * 删除相关权限，不提供接口，供内部调用
     *
     * @param organizationId 组织id
     * @param projectId      项目id
     * @param baseTargetType 基础指向类型
     * @param targetValue    知识库id/文件夹id/文件id
     */
    void removePermissionRange(Long organizationId, Long projectId, PermissionConstants.PermissionTargetBaseType baseTargetType, Long targetValue);

}
