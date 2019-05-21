package io.choerodon.kb.infra.persistence.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.kb.repository.PageRepository;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.common.annotation.DataLog;
import io.choerodon.kb.infra.dataobject.PageDO;
import io.choerodon.kb.infra.mapper.PageMapper;

/**
 * Created by Zenger on 2019/4/29.
 */
@Service
public class PageRepositoryImpl implements PageRepository {

    @Autowired
    private PageMapper pageMapper;

    private static final String ERROR_PAGE_ILLEGAL = "error.page.illegal";
    private static final String ERROR_PAGE_CREATE = "error.page.create";
    private static final String ERROR_PAGE_DELETE = "error.page.delete";
    private static final String ERROR_PAGE_NOTFOUND = "error.page.notFound";
    private static final String ERROR_PAGE_UPDATE = "error.page.update";
    private static final String ERROR_PAGE_SELECT = "error.page.select";

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
        if (!page.getOrganizationId().equals(organizationId)) {
            throw new CommonException(ERROR_PAGE_ILLEGAL);
        }
        if (page.getProjectId() != null && !page.getProjectId().equals(projectId)) {
            throw new CommonException(ERROR_PAGE_ILLEGAL);
        }
        return page;
    }

    @Override
    public void checkById(Long organizationId, Long projectId, Long pageId) {
        queryById(organizationId, projectId, pageId);
    }
}
