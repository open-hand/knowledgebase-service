package io.choerodon.kb.infra.persistence.impl;

import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.kb.entity.PageVersionE;
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
    public PageVersionE insert(PageVersionE pageVersionE) {
        PageVersionDO pageVersionDO = ConvertHelper.convert(pageVersionE, PageVersionDO.class);
        if (pageVersionMapper.insert(pageVersionDO) != 1) {
            throw new CommonException("error.page.version.insert");
        }
        return ConvertHelper.convert(pageVersionDO, PageVersionE.class);
    }

    @Override
    public PageVersionE selectById(Long id) {
        return ConvertHelper.convert(pageVersionMapper.selectByPrimaryKey(id), PageVersionE.class);
    }

    @Override
    public PageVersionE selectOne(PageVersionE pageVersionE) {
        PageVersionDO pageVersionDO = ConvertHelper.convert(pageVersionE, PageVersionDO.class);
        return ConvertHelper.convert(pageVersionMapper.selectOne(pageVersionDO), PageVersionE.class);
    }

    @Override
    public void deleteByPageId(Long pageId) {
        pageVersionMapper.deleteByPageId(pageId);
    }
}
