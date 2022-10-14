package io.choerodon.kb.domain.service.impl;

import static org.hzero.core.base.BaseConstants.ErrorCode.FORBIDDEN;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import io.choerodon.kb.domain.repository.WorkSpacePageRepository;
import io.choerodon.kb.domain.service.IWorkSpaceService;
import io.choerodon.kb.domain.service.PermissionCheckDomainService;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.enums.PermissionConstants;
import io.choerodon.kb.infra.enums.WorkSpaceType;
import io.choerodon.kb.infra.utils.CommonUtil;

/**
 * Copyright (c) 2022. Zknow Enterprise Solution. All right reserved.
 *
 * @author zongqi.hao@zknow.com
 * @since 2022/10/14
 */
@Component
public class FileWorkSpaceServiceImpl implements IWorkSpaceService {

    private final PermissionCheckDomainService permissionCheckDomainService;
    private final WorkSpacePageRepository workSpacePageRepository;

    public FileWorkSpaceServiceImpl(PermissionCheckDomainService permissionCheckDomainService,
                                    WorkSpacePageRepository workSpacePageRepository) {
        this.permissionCheckDomainService = permissionCheckDomainService;
        this.workSpacePageRepository = workSpacePageRepository;
    }

    @Override
    public WorkSpaceType handleSpaceType() {
        return WorkSpaceType.FILE;
    }

    @Override
    public void renameWorkSpace(WorkSpaceDTO workSpaceDTO, String newName) {
        // 鉴权
        Assert.isTrue(permissionCheckDomainService.checkPermission(workSpaceDTO.getOrganizationId(),
                workSpaceDTO.getProjectId(),
                PermissionConstants.PermissionTargetBaseType.FILE.toString(),
                null,
                workSpaceDTO.getId(),
                PermissionConstants.ActionPermission.FILE_RENAME.getCode()), FORBIDDEN);
        String fileType = CommonUtil.getFileType(workSpaceDTO.getFileKey());
        workSpaceDTO.setName(newName + "." + fileType);
        //同步修改page表
        workSpacePageRepository.updatePageTitle(workSpaceDTO);
    }
}
