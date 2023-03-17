package io.choerodon.kb.domain.service.impl;

import static org.hzero.core.base.BaseConstants.ErrorCode.FORBIDDEN;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.kb.app.service.WorkSpacePageService;
import io.choerodon.kb.domain.service.IWorkSpaceService;
import io.choerodon.kb.domain.service.PermissionCheckDomainService;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.enums.PermissionConstants;
import io.choerodon.kb.infra.enums.PermissionConstants.PermissionTargetBaseType;
import io.choerodon.kb.infra.enums.WorkSpaceType;

/**
 * Copyright (c) 2022. Zknow Enterprise Solution. All right reserved.
 *
 * @author zongqi.hao@zknow.com
 * @since 2022/10/14
 */
@Component
public class DocumentWorkSpaceServiceImpl implements IWorkSpaceService {


    @Autowired
    private PermissionCheckDomainService permissionCheckDomainService;
    @Autowired
    private WorkSpacePageService workSpacePageService;


    @Override
    public WorkSpaceType handleSpaceType() {
        return WorkSpaceType.DOCUMENT;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rename(WorkSpaceDTO workSpaceDTO, String newName, boolean checkPermission) {
        if(checkPermission) {
            // 鉴权
            checkPermission(workSpaceDTO, PermissionConstants.ActionPermission.DOCUMENT_RENAME.getCode());
        }
        workSpaceDTO.setName(newName);
        //同步修改page表
        workSpacePageService.updatePageTitle(workSpaceDTO);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void move(WorkSpaceDTO sourceWorkSpace, WorkSpaceDTO targetWorkSpace, boolean checkPermission) {
        if(checkPermission) {
            // 鉴权源space的移动权限
            checkPermission(sourceWorkSpace, PermissionConstants.ActionPermission.DOCUMENT_MOVE.getCode());
        }
        // 鉴定目标space的编辑权限
        //        Assert.isTrue(permissionCheckDomainService.checkPermission(sourceWorkSpace.getOrganizationId(),
        //                sourceWorkSpace.getProjectId(),
        //                PermissionTargetBaseType.ofWorkSpaceType(WorkSpaceType.of(targetWorkSpace.getType())).toString(),
        //                null,
        //                targetWorkSpace.getId(),
        //                PermissionConstants.ActionPermission.), FORBIDDEN);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restore(WorkSpaceDTO workSpaceDTO, boolean checkPermission) {
        if(checkPermission) {
            checkPermission(workSpaceDTO, PermissionConstants.ActionPermission.DOCUMENT_RECOVER.getCode());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(WorkSpaceDTO workSpaceDTO, boolean checkPermission) {
        if(checkPermission) {
            checkPermission(workSpaceDTO, PermissionConstants.ActionPermission.DOCUMENT_EDIT.getCode());
        }
    }

    private void checkPermission(WorkSpaceDTO workSpaceDTO, String action) {
        Assert.isTrue(permissionCheckDomainService.checkPermission(workSpaceDTO.getOrganizationId(),
                workSpaceDTO.getProjectId(),
                PermissionTargetBaseType.FILE.toString(),
                null,
                workSpaceDTO.getId(),
                action), FORBIDDEN);
    }
}
