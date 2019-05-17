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

    private PageCommentMapper pageCommentMapper;

    public PageCommentRepositoryImpl(PageCommentMapper pageCommentMapper) {
        this.pageCommentMapper = pageCommentMapper;
    }

    @Override
    @DataLog(type = BaseStage.COMMENT_CREATE)
    public PageCommentDO insert(PageCommentDO pageCommentDO) {
        if (pageCommentMapper.insert(pageCommentDO) != 1) {
            throw new CommonException("error.page.comment.insert");
        }
        return pageCommentMapper.selectByPrimaryKey(pageCommentDO.getId());
    }

    @Override
    @DataLog(type = BaseStage.COMMENT_UPDATE)
    public PageCommentDO update(PageCommentDO pageCommentDO) {
        if (pageCommentMapper.updateByPrimaryKey(pageCommentDO) != 1) {
            throw new CommonException("error.page.comment.update");
        }
        return pageCommentMapper.selectByPrimaryKey(pageCommentDO.getId());
    }

    @Override
    public PageCommentDO selectById(Long id) {
        return pageCommentMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<PageCommentDO> selectByPageId(Long pageId) {
        return pageCommentMapper.selectByPageId(pageId);
    }

    @Override
    @DataLog(type = BaseStage.COMMENT_DELETE)
    public void delete(Long id) {
        if (pageCommentMapper.deleteByPrimaryKey(id) != 1) {
            throw new CommonException("error.page.comment.delete");
        }
    }

    @Override
    public void deleteByPageId(Long pageId) {
        PageCommentDO pageCommentDO = new PageCommentDO();
        pageCommentDO.setPageId(pageId);
        pageCommentMapper.delete(pageCommentDO);
    }
}
