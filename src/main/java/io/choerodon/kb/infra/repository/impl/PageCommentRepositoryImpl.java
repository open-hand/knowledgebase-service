package io.choerodon.kb.infra.repository.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.vo.PageCommentVO;
import io.choerodon.kb.domain.repository.IamRemoteRepository;
import io.choerodon.kb.domain.repository.PageCommentRepository;
import io.choerodon.kb.domain.repository.PageRepository;
import io.choerodon.kb.infra.annotation.DataLog;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.dto.PageCommentDTO;
import io.choerodon.kb.infra.feign.vo.UserDO;
import io.choerodon.kb.infra.mapper.PageCommentMapper;

import org.hzero.mybatis.base.impl.BaseRepositoryImpl;

/**
 * Created by Zenger on 2019/4/29.
 */
@Service
public class PageCommentRepositoryImpl extends BaseRepositoryImpl<PageCommentDTO> implements PageCommentRepository {

    private static final String ERROR_PAGE_COMMENT_INSERT = "error.page.comment.insert";
    private static final String ERROR_PAGE_COMMENT_UPDATE = "error.page.comment.update";
    private static final String ERROR_PAGE_COMMENT_SELECT = "error.page.comment.select";
    private static final String ERROR_PAGE_COMMENT_DELETE = "error.page.comment.delete";

    @Autowired
    private PageCommentMapper pageCommentMapper;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private IamRemoteRepository iamRemoteRepository;


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

    @Override
    public List<PageCommentVO> queryByPageId(Long organizationId, Long projectId, Long pageId) {
        pageRepository.checkById(organizationId, projectId, pageId);
        List<PageCommentVO> pageCommentVOList = new ArrayList<>();
        List<PageCommentDTO> pageComments = pageCommentMapper.selectByPageId(pageId);
        if (pageComments != null && !pageComments.isEmpty()) {
            List<Long> userIds = pageComments.stream()
                    .map(PageCommentDTO::getCreatedBy)
                    .collect(Collectors.toList());
            List<UserDO> userDOList = iamRemoteRepository.listUsersByIds(userIds, false);
            Map<Long, UserDO> userMap = new HashMap<>();
            for (UserDO userDO : userDOList) {
                userMap.put(userDO.getId(), userDO);
            }
            for (PageCommentDTO p : pageComments) {
                PageCommentVO pageCommentVO = new PageCommentVO();
                pageCommentVO.setId(p.getId());
                pageCommentVO.setPageId(p.getPageId());
                pageCommentVO.setComment(p.getComment());
                pageCommentVO.setObjectVersionNumber(p.getObjectVersionNumber());
                pageCommentVO.setUserId(p.getCreatedBy());
                pageCommentVO.setLastUpdateDate(p.getLastUpdateDate());
                pageCommentVO.setCreateUser(userMap.get(p.getCreatedBy()));
                pageCommentVOList.add(pageCommentVO);
            }
        }
        return pageCommentVOList;
    }

    @Override
    public PageCommentVO getCommentInfo(PageCommentDTO pageCommentDTO) {
        PageCommentVO pageCommentVO = new PageCommentVO();
        pageCommentVO.setId(pageCommentDTO.getId());
        pageCommentVO.setPageId(pageCommentDTO.getPageId());
        pageCommentVO.setComment(pageCommentDTO.getComment());
        pageCommentVO.setObjectVersionNumber(pageCommentDTO.getObjectVersionNumber());
        pageCommentVO.setUserId(pageCommentDTO.getCreatedBy());
        pageCommentVO.setLastUpdateDate(pageCommentDTO.getLastUpdateDate());
        final List<Long> ids = Collections.singletonList(pageCommentDTO.getCreatedBy());
        List<UserDO> userDOList = iamRemoteRepository.listUsersByIds(ids, false);
        if(CollectionUtils.isNotEmpty(userDOList)) {
            pageCommentVO.setCreateUser(userDOList.get(0));
        }
        return pageCommentVO;
    }

}
