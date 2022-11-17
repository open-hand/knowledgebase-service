package io.choerodon.kb.infra.repository.impl;

import org.springframework.stereotype.Repository;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.repository.WorkSpacePageRepository;
import io.choerodon.kb.infra.dto.WorkSpacePageDTO;
import io.choerodon.kb.infra.enums.ReferenceType;

import org.hzero.mybatis.base.impl.BaseRepositoryImpl;

/**
 * Copyright (c) 2022. Zknow Enterprise Solution. All right reserved.
 *
 * @author zongqi.hao@zknow.com
 * @since 2022/10/14
 */
@Repository
public class WorkSpacePageRepositoryImpl extends BaseRepositoryImpl<WorkSpacePageDTO> implements WorkSpacePageRepository {

    private static final String ERROR_WORK_SPACE_PAGE_INSERT = "error.workSpacePage.insert";

    @Override
    public WorkSpacePageDTO baseCreate(Long pageId, Long workSpaceId) {
        WorkSpacePageDTO workSpacePageDTO = new WorkSpacePageDTO();
        workSpacePageDTO.setReferenceType(ReferenceType.SELF);
        workSpacePageDTO.setPageId(pageId);
        workSpacePageDTO.setWorkspaceId(workSpaceId);
        if (insert(workSpacePageDTO) != 1) {
            throw new CommonException(ERROR_WORK_SPACE_PAGE_INSERT);
        }
        return selectByPrimaryKey(workSpacePageDTO.getId());
    }
}
