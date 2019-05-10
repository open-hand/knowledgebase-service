package io.choerodon.kb.infra.persistence.impl;

import org.springframework.stereotype.Service;

import io.choerodon.kb.domain.kb.repository.PageCommentRepository;
import io.choerodon.kb.infra.dataobject.PageCommentDO;
import io.choerodon.kb.infra.mapper.PageCommentMapper;

/**
 * Created by Zenger on 2019/4/29.
 */
@Service
public class PageCommentRepositoryImpl implements PageCommentRepository {

    private PageCommentMapper pageCommentMapper;

    public PageCommentRepositoryImpl(PageCommentMapper pageCommentMapper) {
        this.pageCommentMapper = pageCommentMapper;
    }

    @Override
    public void deleteByPageId(Long pageId) {
        PageCommentDO pageCommentDO = new PageCommentDO();
        pageCommentDO.setPageId(pageId);
        pageCommentMapper.delete(pageCommentDO);
    }
}
