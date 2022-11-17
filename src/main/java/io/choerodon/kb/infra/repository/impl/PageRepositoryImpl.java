package io.choerodon.kb.infra.repository.impl;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.api.vo.PageInfoVO;
import io.choerodon.kb.domain.repository.PageContentRepository;
import io.choerodon.kb.domain.repository.PageRepository;
import io.choerodon.kb.infra.annotation.DataLog;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.dto.PageContentDTO;
import io.choerodon.kb.infra.dto.PageDTO;
import io.choerodon.kb.infra.mapper.PageMapper;
import io.choerodon.kb.infra.utils.EsRestUtil;

import org.hzero.core.base.BaseConstants;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;

/**
 * Created by Zenger on 2019/4/29.
 */
@Service
public class PageRepositoryImpl extends BaseRepositoryImpl<PageDTO> implements PageRepository {

    private static final String ERROR_PAGE_ILLEGAL = "error.page.illegal";
    private static final String ERROR_PAGE_CREATE = "error.page.create";
    private static final String ERROR_PAGE_DELETE = "error.page.delete";
    private static final String ERROR_PAGE_NOTFOUND = "error.page.notFound";
    private static final String ERROR_PAGE_UPDATE = "error.page.update";
    private static final String ERROR_PAGE_SELECT = "error.page.select";
    @Autowired
    private PageMapper pageMapper;
    @Autowired
    private PageContentRepository pageContentRepository;
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
    public PageDTO baseUpdate(PageDTO pageDTO, boolean logUpdateAction) {
        Assert.notNull(pageDTO, BaseConstants.ErrorCode.NOT_NULL);
        if (pageMapper.updateByPrimaryKey(pageDTO) != 1) {
            throw new CommonException(ERROR_PAGE_UPDATE);
        }
        this.createOrUpdateEs(pageDTO.getId());
        return pageMapper.selectByPrimaryKey(pageDTO.getId());
    }

    @Override
    public void createOrUpdateEs(Long pageId) {
        //同步page到es
        PageInfoVO pageInfoVO = pageMapper.queryInfoById(pageId);
        esRestUtil.createOrUpdatePage(BaseStage.ES_PAGE_INDEX, pageId, pageInfoVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEs(Long pageId) {
        esRestUtil.deletePage(BaseStage.ES_PAGE_INDEX, pageId);
        PageDTO pageDTO = selectByPrimaryKey(pageId);
        pageDTO.setIsSyncEs(false);
        super.updateOptional(pageDTO, PageDTO.FIELD_IS_SYNC_ES);
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
        if (page.getOrganizationId() == 0L || (page.getProjectId() != null && page.getProjectId() == 0L)) {
            return page;
        }
        if (page.getOrganizationId() != null && !page.getOrganizationId().equals(organizationId)) {
            throw new CommonException(ERROR_PAGE_ILLEGAL);
        }
        return page;
    }

    @Override
    public PageDTO baseQueryByIdWithOrg(Long organizationId, Long projectId, Long pageId) {
        PageDTO page = pageMapper.selectByPrimaryKey(pageId);
        if (page == null) {
            throw new CommonException(ERROR_PAGE_NOTFOUND);
        }
        if (page.getOrganizationId() == 0L || (page.getProjectId() != null && page.getProjectId() == 0L)) {
            return page;
        }
        if (page.getOrganizationId() != null && !page.getOrganizationId().equals(organizationId)) {
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

    @Override
    public PageContentDTO queryDraftContent(Long organizationId, Long projectId, Long pageId) {
        this.checkById(organizationId, projectId, pageId);
        CustomUserDetails userDetails = DetailsHelper.getUserDetails();
        if (userDetails == null) {
            return null;
        }
        Long userId = userDetails.getUserId();
        PageContentDTO pageContent = new PageContentDTO();
        pageContent.setPageId(pageId);
        pageContent.setVersionId(0L);
        pageContent.setCreatedBy(userId);
        List<PageContentDTO> contents = pageContentRepository.select(pageContent);
        return CollectionUtils.isEmpty(contents) ? null : contents.get(0);
    }

    @Override
    @DataLog(type = BaseStage.PAGE_UPDATE)
    public void updatePageTitle(PageDTO page, boolean logUpdateAction) {
        // 基础校验
        Assert.notNull(page, BaseConstants.ErrorCode.NOT_NULL);
        Assert.notNull(page.getId(), BaseConstants.ErrorCode.NOT_NULL);
        Assert.hasText(page.getTitle(), BaseConstants.ErrorCode.NOT_NULL);
        // 更新title
        page.setIsSyncEs(false);
        String[] updateFields = new String[] {PageDTO.FIELD_TITLE, PageDTO.FIELD_IS_SYNC_ES};
        if(page.getLatestVersionId() != null) {
            updateFields = ArrayUtils.add(updateFields, PageDTO.FIELD_LATEST_VERSION_ID);
        }
        pageMapper.updateOptional(page, updateFields);
        // 同步ES
        this.createOrUpdateEs(page.getId());
    }

}
