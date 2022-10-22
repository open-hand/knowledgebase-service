package io.choerodon.kb.infra.repository.impl;

import org.springframework.stereotype.Repository;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.repository.PageRepository;
import io.choerodon.kb.domain.repository.WorkSpacePageRepository;
import io.choerodon.kb.infra.dto.PageDTO;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
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

    private static final String ERROR_WORKSPACEPAGE_INSERT = "error.workSpacePage.insert";

    private final PageRepository pageRepository;

    public WorkSpacePageRepositoryImpl(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    @Override
    public void updatePageTitle(WorkSpaceDTO spaceDTO) {
        WorkSpacePageDTO spacePageDTO = new WorkSpacePageDTO();
        spacePageDTO.setWorkspaceId(spaceDTO.getId());
        WorkSpacePageDTO workSpacePageDTO = this.selectOne(spacePageDTO);
        if (workSpacePageDTO != null) {
            PageDTO pageDTO = pageRepository.selectByPrimaryKey(workSpacePageDTO.getPageId());
            if (pageDTO != null) {
                pageDTO.setTitle(spaceDTO.getName());
                pageRepository.updateByPrimaryKey(pageDTO);
            }
        }
    }

    @Override
    public WorkSpacePageDTO baseCreate(Long pageId, Long workSpaceId) {
        WorkSpacePageDTO workSpacePageDTO = new WorkSpacePageDTO();
        workSpacePageDTO.setReferenceType(ReferenceType.SELF);
        workSpacePageDTO.setPageId(pageId);
        workSpacePageDTO.setWorkspaceId(workSpaceId);
        if (insert(workSpacePageDTO) != 1) {
            throw new CommonException(ERROR_WORKSPACEPAGE_INSERT);
        }
        return selectByPrimaryKey(workSpacePageDTO.getId());
    }
}
