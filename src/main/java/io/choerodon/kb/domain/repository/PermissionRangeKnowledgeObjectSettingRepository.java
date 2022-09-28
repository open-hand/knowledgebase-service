package io.choerodon.kb.domain.repository;

import java.util.List;

import io.choerodon.kb.api.vo.permission.CollaboratorSearchVO;
import io.choerodon.kb.domain.entity.PermissionRange;

/**
 * 权限范围知识对象设置 领域资源库
 * @author zongqi.hao@zknow.com 2022-09-23
 */
public interface PermissionRangeKnowledgeObjectSettingRepository extends PermissionRangeBaseRepository {
    List<PermissionRange> queryObjectSettingCollaborator(Long organizationId, Long projectId, CollaboratorSearchVO searchVO);
}
