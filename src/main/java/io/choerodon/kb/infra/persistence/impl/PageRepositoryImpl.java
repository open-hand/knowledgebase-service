package io.choerodon.kb.infra.persistence.impl;

import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.kb.entity.PageE;
import io.choerodon.kb.domain.kb.repository.PageRepository;
import io.choerodon.kb.infra.dataobject.PageDO;
import io.choerodon.kb.infra.mapper.PageMapper;

/**
 * Created by Zenger on 2019/4/29.
 */
@Service
public class PageRepositoryImpl implements PageRepository {

    private PageMapper pageMapper;

    public PageRepositoryImpl(PageMapper pageMapper) {
        this.pageMapper = pageMapper;
    }

    @Override
    public PageE selectById(Long id) {
        return ConvertHelper.convert(pageMapper.selectByPrimaryKey(id), PageE.class);
    }

    @Override
    public PageE insert(PageE pageE) {
        PageDO pageDO = ConvertHelper.convert(pageE, PageDO.class);
        if (pageMapper.insert(pageDO) != 1) {
            throw new CommonException("error.page.insert");
        }
        return ConvertHelper.convert(pageDO, PageE.class);
    }

    @Override
    public PageE update(PageE pageE) {
        PageDO pageDO = ConvertHelper.convert(pageE, PageDO.class);
        if (pageMapper.updateByPrimaryKey(pageDO) != 1) {
            throw new CommonException("error.page.update");
        }
        return ConvertHelper.convert(pageDO, PageE.class);
    }

    @Override
    public void delete(Long id) {
        if (pageMapper.deleteByPrimaryKey(id) != 1) {
            throw new CommonException("error.page.delete");
        }
    }
}
