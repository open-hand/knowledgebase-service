package io.choerodon.kb.infra.persistence.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.kb.repository.PageCommentRepository;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.common.annotation.DataLog;
import io.choerodon.kb.infra.dataobject.PageCommentDO;
import io.choerodon.kb.infra.mapper.PageCommentMapper;

/**
 * Created by Zenger on 2019/4/29.
 */
@Service
public class PageCommentRepositoryImpl implements PageCommentRepository {

    private static final String ERROR_PAGE_COMMENT_INSERT = "error.page.comment.insert";
    private static final String ERROR_PAGE_COMMENT_UPDATE = "error.page.comment.update";
    private static final String ERROR_PAGE_COMMENT_SELECT = "error.page.comment.select";
    private static final String ERROR_PAGE_COMMENT_DELETE = "error.page.comment.delete";

    private PageCommentMapper pageCommentMapper;

    public PageCommentRepositoryImpl(PageCommentMapper pageCommentMapper) {
        this.pageCommentMapper = pageCommentMapper;
    }

    @Override
    @DataLog(type = BaseStage.COMMENT_CREATE)
    public PageCommentDO insert(PageCommentDO pageCommentDO) {
        if (pageCommentMapper.insert(pageCommentDO) != 1) {
            throw new CommonException(ERROR_PAGE_COMMENT_INSERT);
        }
        return pageCommentMapper.selectByPrimaryKey(pageCommentDO.getId());
    }

    @Override
    @DataLog(type = BaseStage.COMMENT_UPDATE)
    public PageCommentDO update(PageCommentDO pageCommentDO) {
        if (pageCommentMapper.updateByPrimaryKey(pageCommentDO) != 1) {
            throw new CommonException(ERROR_PAGE_COMMENT_UPDATE);
        }
        return pageCommentMapper.selectByPrimaryKey(pageCommentDO.getId());
    }

    @Override
    public PageCommentDO selectById(Long id) {
        PageCommentDO pageCommentDO = pageCommentMapper.selectByPrimaryKey(id);
        if (pageCommentDO == null) {
            throw new CommonException(ERROR_PAGE_COMMENT_SELECT);
        }
        return pageCommentDO;
    }

    @Override
    public List<PageCommentDO> selectByPageId(Long pageId) {
        return pageCommentMapper.selectByPageId(pageId);
    }

    @Override
    @DataLog(type = BaseStage.COMMENT_DELETE)
    public void delete(Long id) {
        if (pageCommentMapper.deleteByPrimaryKey(id) != 1) {
            throw new CommonException(ERROR_PAGE_COMMENT_DELETE);
        }
    }

    @Override
    public void deleteByPageId(Long pageId) {
        PageCommentDO pageCommentDO = new PageCommentDO();
        pageCommentDO.setPageId(pageId);
        pageCommentMapper.delete(pageCommentDO);
    }
}
