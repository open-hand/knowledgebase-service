package io.choerodon.kb.domain.repository;

import java.util.List;
import java.util.Set;

import io.choerodon.kb.domain.entity.PermissionRange;

/**
 * 权限范围知识对象设置 领域资源库
 * @author zongqi.hao@zknow.com 2022-09-23
 */
public interface PermissionRangeKnowledgeObjectSettingRepository extends PermissionRangeBaseRepository {
    List<PermissionRange> queryFolderOrFileCollaborator(Long organizationId, Long projectId, Set<String> targetTypes, Long targetValue);
}
