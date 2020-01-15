package io.choerodon.kb.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.api.vo.PageCommentVO;
import io.choerodon.kb.api.vo.PageCreateCommentVO;
import io.choerodon.kb.api.vo.PageUpdateCommentVO;
import io.choerodon.kb.app.service.PageCommentService;
import io.choerodon.kb.infra.dto.PageCommentDTO;
import io.choerodon.kb.infra.feign.BaseFeignClient;
import io.choerodon.kb.infra.feign.vo.UserDO;
import io.choerodon.kb.infra.mapper.PageCommentMapper;
import io.choerodon.kb.infra.repository.PageCommentRepository;
import io.choerodon.kb.infra.repository.PageRepository;
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

    private static final String ERROR_ILLEGAL = "error.delete.illegal";
    private BaseFeignClient baseFeignClient;
    private PageRepository pageRepository;
    private PageCommentRepository pageCommentRepository;
    private PageCommentMapper pageCommentMapper;

    public PageCommentServiceImpl(BaseFeignClient baseFeignClient,
                                  PageRepository pageRepository,
                                  PageCommentRepository pageCommentRepository,
                                  PageCommentMapper pageCommentMapper) {
        this.baseFeignClient = baseFeignClient;
        this.pageRepository = pageRepository;
        this.pageCommentRepository = pageCommentRepository;
        this.pageCommentMapper = pageCommentMapper;
    }

    public void setBaseFeignClient(BaseFeignClient baseFeignClient) {
        this.baseFeignClient = baseFeignClient;
    }

    @Override
    public PageCommentVO create(Long organizationId, Long projectId, PageCreateCommentVO pageCreateCommentVO) {
        pageRepository.checkById(organizationId, projectId, pageCreateCommentVO.getPageId());
        PageCommentDTO pageCommentDTO = new PageCommentDTO();
        pageCommentDTO.setPageId(pageCreateCommentVO.getPageId());
        pageCommentDTO.setComment(pageCreateCommentVO.getComment());
        pageCommentDTO = pageCommentRepository.baseCreate(pageCommentDTO);
        return getCommentInfo(pageCommentDTO);
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
        return getCommentInfo(pageCommentDTO);
    }

    @Override
    public List<PageCommentVO> queryByPageId(Long organizationId, Long projectId, Long pageId) {
        pageRepository.checkById(organizationId, projectId, pageId);
        List<PageCommentVO> pageCommentVOList = new ArrayList<>();
        List<PageCommentDTO> pageComments = pageCommentMapper.selectByPageId(pageId);
        if (pageComments != null && !pageComments.isEmpty()) {
            List<Long> userIds = pageComments.stream().map(PageCommentDTO::getCreatedBy).distinct()
                    .collect(Collectors.toList());
            Long[] ids = new Long[userIds.size()];
            userIds.toArray(ids);
            List<UserDO> userDOList = baseFeignClient.listUsersByIds(ids, false).getBody();
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
                pageCommentVO.setCreateUser(userMap.get(p.getCreatedBy()));
                pageCommentVOList.add(pageCommentVO);
            });
        }
        return pageCommentVOList;
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
        List<UserDO> userDOList = baseFeignClient.listUsersByIds(ids, false).getBody();
        pageCommentVO.setCreateUser(userDOList.get(0));
        return pageCommentVO;
    }
}
