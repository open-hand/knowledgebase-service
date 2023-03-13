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
    public void rename(WorkSpaceDTO workSpaceDTO, String newName, boolean checkPermission) {
        if(checkPermission) {
            // 鉴权
            checkPermission(workSpaceDTO, PermissionConstants.ActionPermission.FOLDER_RENAME.getCode());
        }
        checkFolderNameLength(newName);
        workSpaceDTO.setName(newName);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void move(WorkSpaceDTO sourceWorkSpace, WorkSpaceDTO targetWorkSpace, boolean checkPermission) {
        if(checkPermission) {
            // 鉴权源space的移动权限
            checkPermission(sourceWorkSpace, PermissionConstants.ActionPermission.FOLDER_MOVE.getCode());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restore(WorkSpaceDTO workSpaceDTO, boolean checkPermission) {
        if(checkPermission) {
            checkPermission(workSpaceDTO, PermissionConstants.ActionPermission.FOLDER_RECOVER.getCode());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(WorkSpaceDTO workSpaceDTO, boolean checkPermission) {

    }

    private void checkPermission(WorkSpaceDTO workSpaceDTO, String action) {
        Assert.isTrue(permissionCheckDomainService.checkPermission(workSpaceDTO.getOrganizationId(),
                workSpaceDTO.getProjectId(),
                FOLDER.toString(),
                null,
                workSpaceDTO.getId(),
                action), FORBIDDEN);
    }

}
