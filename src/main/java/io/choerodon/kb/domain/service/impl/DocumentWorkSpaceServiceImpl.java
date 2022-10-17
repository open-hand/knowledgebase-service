package io.choerodon.kb.domain.service.impl;

import static org.hzero.core.base.BaseConstants.ErrorCode.FORBIDDEN;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import io.choerodon.kb.domain.repository.WorkSpaceRepository;
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

    private final PermissionCheckDomainService permissionCheckDomainService;
    private final WorkSpaceRepository workSpaceRepository;

    public DocumentWorkSpaceServiceImpl(PermissionCheckDomainService permissionCheckDomainService,
                                        WorkSpaceRepository workSpaceRepository) {
        this.permissionCheckDomainService = permissionCheckDomainService;
        this.workSpaceRepository = workSpaceRepository;
    }

    @Override
    public WorkSpaceType handleSpaceType() {
        return WorkSpaceType.DOCUMENT;
    }

    @Override
    public void rename(WorkSpaceDTO workSpaceDTO, String newName) {
        // 鉴权
        Assert.isTrue(permissionCheckDomainService.checkPermission(workSpaceDTO.getOrganizationId(),
                workSpaceDTO.getProjectId(),
                PermissionTargetBaseType.FILE.toString(),
                null,
                workSpaceDTO.getId(),
                PermissionConstants.ActionPermission.DOCUMENT_RENAME.getCode()), FORBIDDEN);
        workSpaceDTO.setName(newName);

    }

    @Override
    public void move(WorkSpaceDTO sourceWorkSpace, WorkSpaceDTO targetWorkSpace) {
        // 鉴权源space的移动权限
        Assert.isTrue(permissionCheckDomainService.checkPermission(sourceWorkSpace.getOrganizationId(),
                sourceWorkSpace.getProjectId(),
                PermissionTargetBaseType.FILE.toString(),
                null,
                sourceWorkSpace.getId(),
                PermissionConstants.ActionPermission.DOCUMENT_MOVE.getCode()), FORBIDDEN);
        // 鉴定目标space的编辑权限
        //        Assert.isTrue(permissionCheckDomainService.checkPermission(sourceWorkSpace.getOrganizationId(),
        //                sourceWorkSpace.getProjectId(),
        //                PermissionTargetBaseType.ofWorkSpaceType(WorkSpaceType.of(targetWorkSpace.getType())).toString(),
        //                null,
        //                targetWorkSpace.getId(),
        //                PermissionConstants.ActionPermission.), FORBIDDEN);
    }
}
