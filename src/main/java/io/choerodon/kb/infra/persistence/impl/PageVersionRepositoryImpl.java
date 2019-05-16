package io.choerodon.kb.infra.persistence.impl;

import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.kb.repository.PageVersionRepository;
import io.choerodon.kb.infra.dataobject.PageVersionDO;
import io.choerodon.kb.infra.mapper.PageVersionMapper;

/**
 * Created by Zenger on 2019/4/29.
 */
@Service
public class PageVersionRepositoryImpl implements PageVersionRepository {

    private PageVersionMapper pageVersionMapper;

    public PageVersionRepositoryImpl(PageVersionMapper pageVersionMapper) {
        this.pageVersionMapper = pageVersionMapper;
    }

    @Override
    public PageVersionDO insert(PageVersionDO pageVersionDO) {
        if (pageVersionMapper.insert(pageVersionDO) != 1) {
            throw new CommonException("error.page.version.insert");
        }
        return pageVersionMapper.selectByPrimaryKey(pageVersionDO.getId());
    }

    @Override
    public PageVersionDO selectById(Long id) {
        return pageVersionMapper.selectByPrimaryKey(id);
    }

    @Override
    public PageVersionDO selectOne(PageVersionDO pageVersionDO) {
        return pageVersionMapper.selectOne(pageVersionDO);
    }

    @Override
    public void deleteByPageId(Long pageId) {
        pageVersionMapper.deleteByPageId(pageId);
    }
}
