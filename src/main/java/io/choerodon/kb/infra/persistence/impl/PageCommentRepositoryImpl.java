package io.choerodon.kb.infra.persistence.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.kb.repository.PageCommentRepository;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.annotation.DataLog;
import io.choerodon.kb.infra.dto.PageCommentDTO;
import io.choerodon.kb.infra.mapper.PageCommentMapper;
import org.springframework.stereotype.Service;

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
    public PageCommentDTO baseCreate(PageCommentDTO pageCommentDTO) {
        if (pageCommentMapper.insert(pageCommentDTO) != 1) {
            throw new CommonException(ERROR_PAGE_COMMENT_INSERT);
        }
        return pageCommentMapper.selectByPrimaryKey(pageCommentDTO.getId());
    }

    @Override
    @DataLog(type = BaseStage.COMMENT_UPDATE)
    public PageCommentDTO baseUpdate(PageCommentDTO pageCommentDTO) {
        if (pageCommentMapper.updateByPrimaryKey(pageCommentDTO) != 1) {
            throw new CommonException(ERROR_PAGE_COMMENT_UPDATE);
        }
        return pageCommentMapper.selectByPrimaryKey(pageCommentDTO.getId());
    }

    @Override
    public PageCommentDTO baseQueryById(Long id) {
        PageCommentDTO pageCommentDTO = pageCommentMapper.selectByPrimaryKey(id);
        if (pageCommentDTO == null) {
            throw new CommonException(ERROR_PAGE_COMMENT_SELECT);
        }
        return pageCommentDTO;
    }

    @Override
    @DataLog(type = BaseStage.COMMENT_DELETE)
    public void baseDelete(Long id) {
        if (pageCommentMapper.deleteByPrimaryKey(id) != 1) {
            throw new CommonException(ERROR_PAGE_COMMENT_DELETE);
        }
    }

    @Override
    public void deleteByPageId(Long pageId) {
        PageCommentDTO pageCommentDTO = new PageCommentDTO();
        pageCommentDTO.setPageId(pageId);
        pageCommentMapper.delete(pageCommentDTO);
    }
}
