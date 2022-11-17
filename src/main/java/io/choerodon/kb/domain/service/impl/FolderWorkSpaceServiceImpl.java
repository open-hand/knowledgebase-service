package io.choerodon.kb.domain.service.impl;

import static io.choerodon.kb.infra.enums.PermissionConstants.PermissionTargetBaseType.FOLDER;
import static org.hzero.core.base.BaseConstants.ErrorCode.FORBIDDEN;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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

    @Autowired
    private PermissionCheckDomainService permissionCheckDomainService;


    @Override
    public WorkSpaceType handleSpaceType() {
        return WorkSpaceType.FOLDER;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rename(WorkSpaceDTO workSpaceDTO, String newName) {
        // 鉴权
        Assert.isTrue(permissionCheckDomainService.checkPermission(workSpaceDTO.getOrganizationId(),
                workSpaceDTO.getProjectId(),
                FOLDER.toString(),
                null,
                workSpaceDTO.getId(),
                PermissionConstants.ActionPermission.FOLDER_RENAME.getCode()), FORBIDDEN);
        checkFolderNameLength(newName);
        workSpaceDTO.setName(newName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void move(WorkSpaceDTO sourceWorkSpace, WorkSpaceDTO targetWorkSpace) {
        // 鉴权源space的移动权限
        Assert.isTrue(permissionCheckDomainService.checkPermission(sourceWorkSpace.getOrganizationId(),
                sourceWorkSpace.getProjectId(),
                FOLDER.toString(),
                null,
                sourceWorkSpace.getId(),
                PermissionConstants.ActionPermission.FOLDER_MOVE.getCode()), FORBIDDEN);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restore(WorkSpaceDTO workSpaceDTO) {
        Assert.isTrue(permissionCheckDomainService.checkPermission(workSpaceDTO.getOrganizationId(),
                workSpaceDTO.getProjectId(),
                FOLDER.toString(),
                null,
                workSpaceDTO.getId(),
                PermissionConstants.ActionPermission.FOLDER_RECOVER.getCode()), FORBIDDEN);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(WorkSpaceDTO workSpaceDTO) {

    }
}
