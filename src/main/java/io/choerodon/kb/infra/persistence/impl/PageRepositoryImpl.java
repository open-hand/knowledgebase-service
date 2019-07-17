package io.choerodon.kb.infra.persistence.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.vo.PageInfoVO;
import io.choerodon.kb.api.vo.PageSyncVO;
import io.choerodon.kb.domain.kb.repository.PageRepository;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.annotation.DataLog;
import io.choerodon.kb.infra.dto.PageDTO;
import io.choerodon.kb.infra.mapper.PageMapper;
import io.choerodon.kb.infra.utils.EsRestUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Zenger on 2019/4/29.
 */
@Service
public class PageRepositoryImpl implements PageRepository {

    private static final String ERROR_PAGE_ILLEGAL = "error.page.illegal";
    private static final String ERROR_PAGE_CREATE = "error.page.create";
    private static final String ERROR_PAGE_DELETE = "error.page.delete";
    private static final String ERROR_PAGE_NOTFOUND = "error.page.notFound";
    private static final String ERROR_PAGE_UPDATE = "error.page.update";
    private static final String ERROR_PAGE_SELECT = "error.page.select";
    @Autowired
    private PageMapper pageMapper;
    @Autowired
    private EsRestUtil esRestUtil;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public PageDTO selectById(Long id) {
        PageDTO pageDTO = pageMapper.selectByPrimaryKey(id);
        if (pageDTO == null) {
            throw new CommonException(ERROR_PAGE_SELECT);
        }
        return pageDTO;
    }

    @Override
    @DataLog(type = BaseStage.PAGE_CREATE)
    public PageDTO baseCreate(PageDTO create) {
        create.setIsSyncEs(true);
        if (pageMapper.insert(create) != 1) {
            throw new CommonException(ERROR_PAGE_CREATE);
        }
        return pageMapper.selectByPrimaryKey(create.getId());
    }

    @Override
    @DataLog(type = BaseStage.PAGE_UPDATE)
    public PageDTO baseUpdate(PageDTO pageDTO, Boolean flag) {
        if (pageMapper.updateByPrimaryKey(pageDTO) != 1) {
            throw new CommonException(ERROR_PAGE_UPDATE);
        }
        //同步page到es
        PageInfoVO pageInfoVO = pageMapper.queryInfoById(pageDTO.getId());
        PageSyncVO pageSync = modelMapper.map(pageInfoVO, PageSyncVO.class);
        esRestUtil.createOrUpdatePage(BaseStage.ES_PAGE_INDEX, pageDTO.getId(), pageSync);
        return pageMapper.selectByPrimaryKey(pageDTO.getId());
    }

    @Override
    public void baseDelete(Long id) {
        if (pageMapper.deleteByPrimaryKey(id) != 1) {
            throw new CommonException(ERROR_PAGE_DELETE);
        }
    }

    @Override
    public PageDTO baseQueryById(Long organizationId, Long projectId, Long pageId) {
        PageDTO page = pageMapper.selectByPrimaryKey(pageId);
        if (page == null) {
            throw new CommonException(ERROR_PAGE_NOTFOUND);
        }
        if (page.getOrganizationId() != null && !page.getOrganizationId().equals(organizationId)) {
            throw new CommonException(ERROR_PAGE_ILLEGAL);
        }
        if (page.getProjectId() != null && !page.getProjectId().equals(projectId)) {
            throw new CommonException(ERROR_PAGE_ILLEGAL);
        }
        return page;
    }

    @Override
    public PageInfoVO queryInfoById(Long organizationId, Long projectId, Long pageId) {
        PageInfoVO pageInfoVO = pageMapper.queryInfoById(pageId);
        if (pageInfoVO == null) {
            throw new CommonException(ERROR_PAGE_NOTFOUND);
        }
        if (pageInfoVO.getOrganizationId() != null && !pageInfoVO.getOrganizationId().equals(organizationId)) {
            throw new CommonException(ERROR_PAGE_ILLEGAL);
        }
        if (pageInfoVO.getProjectId() != null && !pageInfoVO.getProjectId().equals(projectId)) {
            throw new CommonException(ERROR_PAGE_ILLEGAL);
        }
        return pageInfoVO;
    }

    @Override
    public PageInfoVO queryShareInfoById(Long pageId) {
        PageInfoVO pageInfoVO = pageMapper.queryInfoById(pageId);
        if (pageInfoVO == null) {
            throw new CommonException(ERROR_PAGE_NOTFOUND);
        }
        return pageInfoVO;
    }

    @Override
    public void checkById(Long organizationId, Long projectId, Long pageId) {
        baseQueryById(organizationId, projectId, pageId);
    }
}
