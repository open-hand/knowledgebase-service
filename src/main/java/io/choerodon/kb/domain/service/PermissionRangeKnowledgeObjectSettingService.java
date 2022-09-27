package io.choerodon.kb.domain.service;

import java.util.List;

import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.choerodon.kb.domain.entity.PermissionRange;

public interface PermissionRangeKnowledgeObjectSettingService {

    /**
     * 权限范围保存接口
     *
     * @param organizationId 组织ID
     * @param projectId 项目ID
     * @param permissionDetailVO permissionDetailVO
     * @return permissionDetailVO
     */
    PermissionDetailVO save(Long organizationId, Long projectId, PermissionDetailVO permissionDetailVO);

    List<PermissionRange> queryFolderOrFileCollaborator(Long organizationId, Long projectId, String targetType, Long targetValue);
}
