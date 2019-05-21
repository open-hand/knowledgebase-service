package io.choerodon.kb.infra.persistence.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.kb.repository.PageLogRepository;
import io.choerodon.kb.infra.dataobject.PageLogDO;
import io.choerodon.kb.infra.mapper.PageLogMapper;

/**
 * Created by Zenger on 2019/4/29.
 */
@Service
public class PageLogRepositoryImpl implements PageLogRepository {

    private static final String ERROR_PAGE_LOG_INSERT = "error.page.log.insert";

    private PageLogMapper pageLogMapper;

    public PageLogRepositoryImpl(PageLogMapper pageLogMapper) {
        this.pageLogMapper = pageLogMapper;
    }

    @Override
    public PageLogDO insert(PageLogDO pageLogDO) {
        if (pageLogMapper.insert(pageLogDO) != 1) {
            throw new CommonException(ERROR_PAGE_LOG_INSERT);
        }
        return pageLogMapper.selectByPrimaryKey(pageLogDO.getId());
    }

    @Override
    public List<PageLogDO> selectByPageId(Long pageId) {
        return pageLogMapper.selectByPageId(pageId);
    }

    @Override
    public void deleteByPageId(Long pageId) {
        PageLogDO pageLogDO = new PageLogDO();
        pageLogDO.setPageId(pageId);
        pageLogMapper.delete(pageLogDO);
    }
}
