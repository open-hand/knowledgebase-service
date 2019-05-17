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

    private PageLogMapper pageLogMapper;

    public PageLogRepositoryImpl(PageLogMapper pageLogMapper) {
        this.pageLogMapper = pageLogMapper;
    }

    @Override
    public PageLogDO insert(PageLogDO pageLogDO) {
        if (pageLogMapper.insert(pageLogDO) != 1) {
            throw new CommonException("error.page.log.insert");
        }
        return pageLogMapper.selectByPrimaryKey(pageLogDO.getId());
    }

    @Override
    public List<PageLogDO> selectByPageId(Long pageId) {
        return pageLogMapper.selectByPageId(pageId);
    }
}
