package io.choerodon.kb.infra.persistence.impl;

import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.kb.entity.PageContentE;
import io.choerodon.kb.domain.kb.repository.PageContentRepository;
import io.choerodon.kb.infra.dataobject.PageContentDO;
import io.choerodon.kb.infra.mapper.PageContentMapper;

/**
 * Created by Zenger on 2019/4/29.
 */
@Service
public class PageContentRepositoryImpl implements PageContentRepository {

    private PageContentMapper pageContentMapper;

    public PageContentRepositoryImpl(PageContentMapper pageContentMapper) {
        this.pageContentMapper = pageContentMapper;
    }

    @Override
    public PageContentE insert(PageContentE pageContentE) {
        PageContentDO pageContentDO = ConvertHelper.convert(pageContentE, PageContentDO.class);
        if (pageContentMapper.insert(pageContentDO) != 1) {
            throw new CommonException("error.page.content.insert");
        }
        return ConvertHelper.convert(pageContentDO, PageContentE.class);
    }

    @Override
    public void deleteByPageId(Long pageId) {
        pageContentMapper.deleteByPageId(pageId);
    }
}
