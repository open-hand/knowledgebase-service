package io.choerodon.kb.infra.repository.impl;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.repository.PermissionRangeKnowledgeObjectSettingRepository;
import io.choerodon.kb.infra.enums.PermissionConstants;

/**
 * 权限范围知识对象设置 领域资源库实现
 * @author zongqi.hao@zknow.com 2022-09-23
 */
@Repository
public class PermissionRangeKnowledgeObjectSettingRepositoryImpl extends PermissionRangeBaseRepositoryImpl implements PermissionRangeKnowledgeObjectSettingRepository {
    @Override
    public List<PermissionRange> queryFolderOrFileCollaborator(Long organizationId, Long projectId, String targetType, Long targetValue) {
        Assert.isTrue(PermissionConstants.PermissionTargetType.FOLDER_OR_FILE_TYPES.contains(targetType), "error.kb.permission.target.type");
        PermissionRange permissionRange = new PermissionRange();
        permissionRange.setOrganizationId(organizationId);
        permissionRange.setProjectId(projectId);
        permissionRange.setTargetType(targetType);
        permissionRange.setTargetValue(targetValue);
        List<PermissionRange> select = select(permissionRange);
        assemblyRangeData(organizationId, select);
        return select;
    }
}
