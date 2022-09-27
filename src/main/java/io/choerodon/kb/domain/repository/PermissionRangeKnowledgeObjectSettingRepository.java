package io.choerodon.kb.domain.repository;

import java.util.List;

import io.choerodon.kb.domain.entity.PermissionRange;

public interface PermissionRangeKnowledgeObjectSettingRepository extends PermissionRangeBaseRepository {
    List<PermissionRange> queryFolderOrFileCollaborator(Long organizationId, Long projectId, String targetType, Long targetValue);
}
