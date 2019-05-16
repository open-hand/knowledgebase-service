package io.choerodon.kb.app.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.dao.PageCommentDTO;
import io.choerodon.kb.api.dao.PageCommentUpdateDTO;
import io.choerodon.kb.app.service.PageCommentService;
import io.choerodon.kb.domain.kb.repository.IamRepository;
import io.choerodon.kb.domain.kb.repository.PageCommentRepository;
import io.choerodon.kb.domain.kb.repository.PageRepository;
import io.choerodon.kb.infra.dataobject.PageCommentDO;
import io.choerodon.kb.infra.dataobject.PageDO;
import io.choerodon.kb.infra.dataobject.iam.UserDO;

/**
 * Created by Zenger on 2019/4/30.
 */
@Service
public class PageCommentServiceImpl implements PageCommentService {

    private IamRepository iamRepository;
    private PageRepository pageRepository;
    private PageCommentRepository pageCommentRepository;

    public PageCommentServiceImpl(IamRepository iamRepository,
                                  PageRepository pageRepository,
                                  PageCommentRepository pageCommentRepository) {
        this.iamRepository = iamRepository;
        this.pageRepository = pageRepository;
        this.pageCommentRepository = pageCommentRepository;
    }

    @Override
    public PageCommentDTO create(PageCommentUpdateDTO pageCommentUpdateDTO) {
        PageDO pageDO = pageRepository.selectById(pageCommentUpdateDTO.getPageId());
        if (pageDO == null) {
            throw new CommonException("error.page.select");
        }
        PageCommentDO pageCommentDO = new PageCommentDO();
        pageCommentDO.setPageId(pageDO.getId());
        pageCommentDO.setComment(pageCommentUpdateDTO.getComment());
        pageCommentDO = pageCommentRepository.insert(pageCommentDO);
        return getCommentInfo(pageCommentDO);
    }

    @Override
    public PageCommentDTO update(Long id, PageCommentUpdateDTO pageCommentUpdateDTO) {
        PageCommentDO pageCommentDO = pageCommentRepository.selectById(id);
        if (pageCommentDO == null) {
            throw new CommonException("error.page.comment.select");
        }
        if (!pageCommentDO.getPageId().equals(pageCommentUpdateDTO.getPageId())) {
            throw new CommonException("error.pageId.not.equal");
        }
        pageCommentDO.setComment(pageCommentUpdateDTO.getComment());
        pageCommentDO = pageCommentRepository.update(pageCommentDO);
        return getCommentInfo(pageCommentDO);
    }

    @Override
    public void delete(Long id) {
        pageCommentRepository.delete(id);
    }

    private PageCommentDTO getCommentInfo(PageCommentDO pageCommentDO) {
        PageCommentDTO pageCommentDTO = new PageCommentDTO();
        pageCommentDTO.setId(pageCommentDO.getId());
        pageCommentDTO.setPageId(pageCommentDO.getPageId());
        pageCommentDTO.setComment(pageCommentDO.getComment());
        pageCommentDTO.setObjectVersionNumber(pageCommentDO.getObjectVersionNumber());
        pageCommentDTO.setUserId(pageCommentDO.getCreatedBy());
        pageCommentDTO.setLastUpdateDate(pageCommentDO.getLastUpdateDate());
        Long[] ids = new Long[1];
        ids[0] = pageCommentDO.getCreatedBy();
        List<UserDO> userDOList = iamRepository.userDOList(ids);
        pageCommentDTO.setUserName(userDOList.get(0).getLoginName() + userDOList.get(0).getRealName());
        pageCommentDTO.setUserImageUrl(userDOList.get(0).getImageUrl());
        return pageCommentDTO;
    }
}
