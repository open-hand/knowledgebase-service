package io.choerodon.kb.domain.service.impl;

import static org.hzero.core.base.BaseConstants.ErrorCode.FORBIDDEN;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import io.choerodon.kb.domain.service.IWorkSpaceService;
import io.choerodon.kb.domain.service.PermissionCheckDomainService;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.enums.PermissionConstants;
import io.choerodon.kb.infra.enums.WorkSpaceType;

/**
 * Copyright (c) 2022. Zknow Enterprise Solution. All right reserved.
 *
 * @author zongqi.hao@zknow.com
 * @since 2022/10/14
 */
@Component
public class FolderWorkSpaceServiceImpl implements IWorkSpaceService {

    private final PermissionCheckDomainService permissionCheckDomainService;

    public FolderWorkSpaceServiceImpl(PermissionCheckDomainService permissionCheckDomainService) {
        this.permissionCheckDomainService = permissionCheckDomainService;
    }

    @Override
    public WorkSpaceType handleSpaceType() {
        return WorkSpaceType.FOLDER;
    }

    @Override
    public void renameWorkSpace(WorkSpaceDTO workSpaceDTO, String newName) {
        // 鉴权
        Assert.isTrue(permissionCheckDomainService.checkPermission(workSpaceDTO.getOrganizationId(),
                workSpaceDTO.getProjectId(),
                PermissionConstants.PermissionTargetBaseType.FOLDER.toString(),
                null,
                workSpaceDTO.getId(),
                PermissionConstants.ActionPermission.FOLDER_RENAME.getCode()), FORBIDDEN);
        checkFolderNameLength(newName);
    }
}
