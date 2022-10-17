package io.choerodon.kb.app.service.impl;

import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.api.vo.PageCommentVO;
import io.choerodon.kb.api.vo.PageCreateCommentVO;
import io.choerodon.kb.api.vo.PageUpdateCommentVO;
import io.choerodon.kb.app.service.PageCommentService;
import io.choerodon.kb.domain.repository.PageCommentRepository;
import io.choerodon.kb.domain.repository.PageRepository;
import io.choerodon.kb.infra.dto.PageCommentDTO;

/**
 * Created by Zenger on 2019/4/30.
 */
@Service
public class PageCommentServiceImpl implements PageCommentService {

    private static final String ERROR_ILLEGAL = "error.delete.illegal";
    private PageRepository pageRepository;
    private PageCommentRepository pageCommentRepository;

    public PageCommentServiceImpl(PageRepository pageRepository,
                                  PageCommentRepository pageCommentRepository) {
        this.pageRepository = pageRepository;
        this.pageCommentRepository = pageCommentRepository;
    }

    @Override
    public PageCommentVO create(Long organizationId, Long projectId, PageCreateCommentVO pageCreateCommentVO) {
        pageRepository.checkById(organizationId, projectId, pageCreateCommentVO.getPageId());
        PageCommentDTO pageCommentDTO = new PageCommentDTO();
        pageCommentDTO.setPageId(pageCreateCommentVO.getPageId());
        pageCommentDTO.setComment(pageCreateCommentVO.getComment());
        pageCommentDTO = pageCommentRepository.baseCreate(pageCommentDTO);
        return this.pageCommentRepository.getCommentInfo(pageCommentDTO);
    }

    @Override
    public PageCommentVO update(Long organizationId, Long projectId, Long id, PageUpdateCommentVO pageUpdateCommentVO) {
        pageRepository.checkById(organizationId, projectId, pageUpdateCommentVO.getPageId());
        PageCommentDTO pageCommentDTO = pageCommentRepository.baseQueryById(id);
        if (!pageCommentDTO.getPageId().equals(pageUpdateCommentVO.getPageId())) {
            throw new CommonException(ERROR_ILLEGAL);
        }
        pageCommentDTO.setObjectVersionNumber(pageUpdateCommentVO.getObjectVersionNumber());
        pageCommentDTO.setComment(pageUpdateCommentVO.getComment());
        pageCommentDTO = pageCommentRepository.baseUpdate(pageCommentDTO);
        return this.pageCommentRepository.getCommentInfo(pageCommentDTO);
    }

    @Override
    public void delete(Long organizationId, Long projectId, Long id, Boolean isAdmin) {
        PageCommentDTO comment = pageCommentRepository.baseQueryById(id);
        pageRepository.checkById(organizationId, projectId, comment.getPageId());
        if (Boolean.FALSE.equals(isAdmin)) {
            Long currentUserId = DetailsHelper.getUserDetails().getUserId();
            if (!comment.getCreatedBy().equals(currentUserId)) {
                throw new CommonException(ERROR_ILLEGAL);
            }
        }
        pageCommentRepository.baseDelete(id);
    }

}
