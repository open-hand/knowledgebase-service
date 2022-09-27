package io.choerodon.kb.infra.repository.impl;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.repository.PermissionRangeFolderOrFileRepository;
import io.choerodon.kb.infra.enums.PermissionConstants;

/**
 * Copyright (c) 2022. Zknow Enterprise Solution. All right reserved.
 *
 * @author zongqi.hao@zknow.com
 * @since 2022/9/27
 */
@Repository
public class PermissionRangeFolderOrFileRepositoryImpl extends PermissionRangeBaseRepositoryImpl implements PermissionRangeFolderOrFileRepository {

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
