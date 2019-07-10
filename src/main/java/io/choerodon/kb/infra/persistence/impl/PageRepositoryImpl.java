package io.choerodon.kb.infra.persistence.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.dao.PageInfo;
import io.choerodon.kb.api.dao.PageSyncDTO;
import io.choerodon.kb.domain.kb.repository.PageRepository;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.common.annotation.DataLog;
import io.choerodon.kb.infra.common.utils.EsRestUtil;
import io.choerodon.kb.infra.dataobject.PageDO;
import io.choerodon.kb.infra.mapper.PageMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created by Zenger on 2019/4/29.
 */
@Service
public class PageRepositoryImpl implements PageRepository {

    @Autowired
    private PageMapper pageMapper;
    @Autowired
    private EsRestUtil esRestUtil;

    private static final String ERROR_PAGE_ILLEGAL = "error.page.illegal";
    private static final String ERROR_PAGE_CREATE = "error.page.create";
    private static final String ERROR_PAGE_DELETE = "error.page.delete";
    private static final String ERROR_PAGE_NOTFOUND = "error.page.notFound";
    private static final String ERROR_PAGE_UPDATE = "error.page.update";
    private static final String ERROR_PAGE_SELECT = "error.page.select";

    private ModelMapper modelMapper = new ModelMapper();

    @PostConstruct
    public void init() {
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    @Override
    public PageDO selectById(Long id) {
        PageDO pageDO = pageMapper.selectByPrimaryKey(id);
        if (pageDO == null) {
            throw new CommonException(ERROR_PAGE_SELECT);
        }
        return pageDO;
    }

    @Override
    @DataLog(type = BaseStage.PAGE_CREATE)
    public PageDO create(PageDO create) {
        create.setIsSyncEs(true);
        if (pageMapper.insert(create) != 1) {
            throw new CommonException(ERROR_PAGE_CREATE);
        }
        return pageMapper.selectByPrimaryKey(create.getId());
    }

    @Override
    @DataLog(type = BaseStage.PAGE_UPDATE)
    public PageDO update(PageDO pageDO, Boolean flag) {
        if (pageMapper.updateByPrimaryKey(pageDO) != 1) {
            throw new CommonException(ERROR_PAGE_UPDATE);
        }
        //同步page到es
        PageInfo pageInfo = pageMapper.queryInfoById(pageDO.getId());
        PageSyncDTO pageSync = modelMapper.map(pageInfo, PageSyncDTO.class);
        esRestUtil.createOrUpdatePage(BaseStage.ES_PAGE_INDEX, pageDO.getId(), pageSync);
        return pageMapper.selectByPrimaryKey(pageDO.getId());
    }

    @Override
    public void delete(Long id) {
        if (pageMapper.deleteByPrimaryKey(id) != 1) {
            throw new CommonException(ERROR_PAGE_DELETE);
        }
    }

    @Override
    public PageDO queryById(Long organizationId, Long projectId, Long pageId) {
        PageDO page = pageMapper.selectByPrimaryKey(pageId);
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
    public PageInfo queryInfoById(Long organizationId, Long projectId, Long pageId) {
        PageInfo pageInfo = pageMapper.queryInfoById(pageId);
        if (pageInfo == null) {
            throw new CommonException(ERROR_PAGE_NOTFOUND);
        }
        if (pageInfo.getOrganizationId() != null && !pageInfo.getOrganizationId().equals(organizationId)) {
            throw new CommonException(ERROR_PAGE_ILLEGAL);
        }
        if (pageInfo.getProjectId() != null && !pageInfo.getProjectId().equals(projectId)) {
            throw new CommonException(ERROR_PAGE_ILLEGAL);
        }
        return pageInfo;
    }

    @Override
    public PageInfo queryShareInfoById(Long pageId) {
        PageInfo pageInfo = pageMapper.queryInfoById(pageId);
        if (pageInfo == null) {
            throw new CommonException(ERROR_PAGE_NOTFOUND);
        }
        return pageInfo;
    }

    @Override
    public void checkById(Long organizationId, Long projectId, Long pageId) {
        queryById(organizationId, projectId, pageId);
    }
}
