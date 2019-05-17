package io.choerodon.kb.infra.persistence.impl;

import org.springframework.stereotype.Service;

import io.choerodon.kb.domain.kb.repository.PageTagRepository;
import io.choerodon.kb.infra.dataobject.PageTagDO;
import io.choerodon.kb.infra.mapper.PageTagMapper;

/**
 * Created by Zenger on 2019/4/29.
 */
@Service
public class PageTagRepositoryImpl implements PageTagRepository {

    private PageTagMapper pageTagMapper;

    public PageTagRepositoryImpl(PageTagMapper pageTagMapper) {
        this.pageTagMapper = pageTagMapper;
    }

    @Override
    public void deleteByPageId(Long pageId) {
        PageTagDO pageTagDO = new PageTagDO();
        pageTagDO.setPageId(pageId);
        pageTagMapper.delete(pageTagDO);
    }
}
