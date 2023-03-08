package io.choerodon.kb.domain.service.impl;

import static org.hzero.core.base.BaseConstants.ErrorCode.FORBIDDEN;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.kb.app.service.WorkSpacePageService;
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

    private final static Logger LOGGER = LoggerFactory.getLogger(FileWorkSpaceServiceImpl.class);

    @Autowired
    private PermissionCheckDomainService permissionCheckDomainService;
    @Autowired
    private WorkSpacePageService workSpacePageService;


    @Override
    public WorkSpaceType handleSpaceType() {
        return WorkSpaceType.FILE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rename(WorkSpaceDTO workSpaceDTO, String newName) {
        // 鉴权
        checkPermission(workSpaceDTO, PermissionConstants.ActionPermission.FILE_RENAME.getCode());
        String fileType = CommonUtil.getFileType(workSpaceDTO.getFileKey());
        workSpaceDTO.setName(newName + "." + fileType);
        //同步修改page表
        workSpacePageService.updatePageTitle(workSpaceDTO);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void move(WorkSpaceDTO sourceWorkSpace, WorkSpaceDTO targetWorkSpace) {
        // 鉴权源space的移动权限
        checkPermission(sourceWorkSpace, PermissionConstants.ActionPermission.FILE_MOVE.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restore(WorkSpaceDTO workSpaceDTO) {
        checkPermission(workSpaceDTO, PermissionConstants.ActionPermission.FILE_RECOVER.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(WorkSpaceDTO workSpaceDTO) {
        checkPermission(workSpaceDTO, PermissionConstants.ActionPermission.FILE_EDIT.getCode());
    }

    private void checkPermission(WorkSpaceDTO workSpaceDTO, String action) {
        if (workSpaceDTO.getTemplateFlag()) {
            return;
        }
        Assert.isTrue(permissionCheckDomainService.checkPermission(workSpaceDTO.getOrganizationId(),
                workSpaceDTO.getProjectId(),
                PermissionConstants.PermissionTargetBaseType.FILE.toString(),
                null,
                workSpaceDTO.getId(),
                action), FORBIDDEN);
    }

}
