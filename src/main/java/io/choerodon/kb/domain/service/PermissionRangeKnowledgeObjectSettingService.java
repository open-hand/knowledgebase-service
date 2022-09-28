package io.choerodon.kb.domain.service;

import java.util.List;

import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.choerodon.kb.api.vo.permission.PermissionSearchVO;
import io.choerodon.kb.domain.entity.PermissionRange;

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
    PermissionDetailVO saveRangeAndSecurity(Long organizationId, Long projectId, PermissionDetailVO permissionDetailVO);

    /**
     * 权限范围保存接口
     *
     * @param organizationId     组织ID
     * @param projectId          项目ID
     * @param permissionDetailVO permissionDetailVO
     * @return permissionDetailVO
     */
    PermissionDetailVO saveRange(Long organizationId, Long projectId, PermissionDetailVO permissionDetailVO);

    /**
     * 查询已有协作者接口
     *
     * @param organizationId 租户id
     * @param projectId      项目id
     * @param searchVO       查询实体
     * @return List
     */
    List<PermissionRange> queryCollaboratorAndSecuritySetting(Long organizationId, Long projectId, PermissionSearchVO searchVO);

    /**
     * 删除相关权限，不提供接口，供内部调用
     *
     * @param organizationId 组织id
     * @param projectId      项目id
     * @param targetValue    知识库id/文件夹id/文件id
     */
    void clear(Long organizationId, Long projectId, Long targetValue);
}
