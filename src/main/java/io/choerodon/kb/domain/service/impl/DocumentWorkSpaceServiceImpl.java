package io.choerodon.kb.domain.service.impl;

import org.springframework.stereotype.Component;

import io.choerodon.kb.domain.service.IWorkSpaceService;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.enums.WorkSpaceType;

/**
 * Copyright (c) 2022. Zknow Enterprise Solution. All right reserved.
 *
 * @author zongqi.hao@zknow.com
 * @since 2022/10/14
 */
@Component
public class DocumentWorkSpaceServiceImpl implements IWorkSpaceService {

    @Override
    public WorkSpaceType handleSpaceType() {
        return WorkSpaceType.DOCUMENT;
    }

    @Override
    public void renameWorkSpace(WorkSpaceDTO workSpaceDTO, String newName) {
        workSpaceDTO.setName(newName);

    }
}
