package io.choerodon.kb.infra.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import io.choerodon.kb.domain.repository.PageContentRepository;
import io.choerodon.kb.infra.dto.PageContentDTO;
import io.choerodon.kb.infra.mapper.PageContentMapper;

import org.hzero.mybatis.base.impl.BaseRepositoryImpl;

@Repository
public class PageContentRepositoryImpl extends BaseRepositoryImpl<PageContentDTO> implements PageContentRepository {

    @Autowired
    private PageContentMapper pageContentMapper;
    
    @Override
    public PageContentDTO selectLatestByPageId(Long pageId) {
        if(pageId == null) {
            return null;
        }
        return this.pageContentMapper.selectLatestByPageId(pageId);
    }
}
