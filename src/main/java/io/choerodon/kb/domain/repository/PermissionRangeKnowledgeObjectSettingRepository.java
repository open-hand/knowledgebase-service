package io.choerodon.kb.domain.repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.choerodon.kb.api.vo.permission.PermissionSearchVO;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.infra.enums.PermissionConstants;

/**
 * 权限范围知识对象设置 领域资源库
 * @author zongqi.hao@zknow.com 2022-09-23
 */
public interface PermissionRangeKnowledgeObjectSettingRepository extends PermissionRangeBaseRepository {
    List<PermissionRange> queryObjectSettingCollaborator(Long organizationId, Long projectId, PermissionSearchVO searchVO);

    void clear(Long organizationId, Long projectId, Long targetValue);

    List<PermissionRange> selectFolderAndFileByTargetValues(Long organizationId, Long projectId, HashSet<PermissionConstants.PermissionTargetType> resourceTargetTypes, Set<String> workspaceIds);
}
