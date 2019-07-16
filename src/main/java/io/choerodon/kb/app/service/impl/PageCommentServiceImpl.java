package io.choerodon.kb.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.api.dao.PageCommentVO;
import io.choerodon.kb.api.dao.PageCreateCommentVO;
import io.choerodon.kb.api.dao.PageUpdateCommentVO;
import io.choerodon.kb.app.service.PageCommentService;
import io.choerodon.kb.domain.kb.repository.IamRepository;
import io.choerodon.kb.domain.kb.repository.PageCommentRepository;
import io.choerodon.kb.domain.kb.repository.PageRepository;
import io.choerodon.kb.infra.dto.PageCommentDTO;
import io.choerodon.kb.infra.dto.PageDTO;
import io.choerodon.kb.infra.dto.iam.UserDO;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Zenger on 2019/4/30.
 */
@Service
public class PageCommentServiceImpl implements PageCommentService {

    private static final String ILLEGAL_ERROR = "error.delete.illegal";
    private IamRepository iamRepository;
    private PageRepository pageRepository;
    private PageCommentRepository pageCommentRepository;
    private ModelMapper modelMapper;

    public PageCommentServiceImpl(IamRepository iamRepository,
                                  PageRepository pageRepository,
                                  PageCommentRepository pageCommentRepository,
                                  ModelMapper modelMapper) {
        this.iamRepository = iamRepository;
        this.pageRepository = pageRepository;
        this.pageCommentRepository = pageCommentRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public PageCommentVO create(PageCreateCommentVO pageCreateCommentVO) {
        PageDTO pageDTO = pageRepository.selectById(pageCreateCommentVO.getPageId());
        PageCommentDTO pageCommentDTO = new PageCommentDTO();
        pageCommentDTO.setPageId(pageDTO.getId());
        pageCommentDTO.setComment(pageCreateCommentVO.getComment());
        pageCommentDTO = pageCommentRepository.insert(pageCommentDTO);
        return getCommentInfo(pageCommentDTO);
    }

    @Override
    public PageCommentVO update(Long id, PageUpdateCommentVO pageUpdateCommentVO) {
        PageCommentDTO pageCommentDTO = pageCommentRepository.selectById(id);
        if (!pageCommentDTO.getPageId().equals(pageUpdateCommentVO.getPageId())) {
            throw new CommonException("error.pageId.not.equal");
        }
        pageCommentDTO.setObjectVersionNumber(pageUpdateCommentVO.getObjectVersionNumber());
        pageCommentDTO.setComment(pageUpdateCommentVO.getComment());
        pageCommentDTO = pageCommentRepository.update(pageCommentDTO);
        return getCommentInfo(pageCommentDTO);
    }

    @Override
    public List<PageCommentVO> queryByList(Long pageId) {
        List<PageCommentVO> pageCommentVOList = new ArrayList<>();
        List<PageCommentDTO> pageComments = pageCommentRepository.selectByPageId(pageId);
        if (pageComments != null && !pageComments.isEmpty()) {
            List<Long> userIds = pageComments.stream().map(PageCommentDTO::getCreatedBy).distinct()
                    .collect(Collectors.toList());
            Long[] ids = new Long[userIds.size()];
            userIds.toArray(ids);
            List<UserDO> userDOList = iamRepository.userDOList(ids);
            Map<Long, UserDO> userMap = new HashMap<>();
            userDOList.forEach(userDO -> userMap.put(userDO.getId(), userDO));
            pageComments.forEach(p -> {
                PageCommentVO pageCommentVO = new PageCommentVO();
                pageCommentVO.setId(p.getId());
                pageCommentVO.setPageId(p.getPageId());
                pageCommentVO.setComment(p.getComment());
                pageCommentVO.setObjectVersionNumber(p.getObjectVersionNumber());
                pageCommentVO.setUserId(p.getCreatedBy());
                pageCommentVO.setLastUpdateDate(p.getLastUpdateDate());
                UserDO userDO = userMap.getOrDefault(p.getCreatedBy(), new UserDO());
                pageCommentVO.setLoginName(userDO.getLoginName());
                pageCommentVO.setRealName(userDO.getRealName());
                pageCommentVO.setUserImageUrl(userDO.getImageUrl());
                pageCommentVOList.add(pageCommentVO);
            });
        }
        return pageCommentVOList;
    }

    @Override
    public void delete(Long id, Boolean isAdmin) {
        PageCommentDTO comment = pageCommentRepository.selectById(id);
        if (!isAdmin) {
            Long currentUserId = DetailsHelper.getUserDetails().getUserId();
            if (!comment.getCreatedBy().equals(currentUserId)) {
                throw new CommonException(ILLEGAL_ERROR);
            }
        }
        pageCommentRepository.delete(id);
    }

    private PageCommentVO getCommentInfo(PageCommentDTO pageCommentDTO) {
        PageCommentVO pageCommentVO = new PageCommentVO();
        pageCommentVO.setId(pageCommentDTO.getId());
        pageCommentVO.setPageId(pageCommentDTO.getPageId());
        pageCommentVO.setComment(pageCommentDTO.getComment());
        pageCommentVO.setObjectVersionNumber(pageCommentDTO.getObjectVersionNumber());
        pageCommentVO.setUserId(pageCommentDTO.getCreatedBy());
        pageCommentVO.setLastUpdateDate(pageCommentDTO.getLastUpdateDate());
        Long[] ids = new Long[1];
        ids[0] = pageCommentDTO.getCreatedBy();
        List<UserDO> userDOList = iamRepository.userDOList(ids);
        pageCommentVO.setLoginName(userDOList.get(0).getLoginName());
        pageCommentVO.setRealName(userDOList.get(0).getRealName());
        pageCommentVO.setUserImageUrl(userDOList.get(0).getImageUrl());
        return pageCommentVO;
    }
}
