package io.choerodon.kb.app.service.impl;

import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.app.service.PageService;
import io.choerodon.kb.domain.kb.entity.PageE;
import io.choerodon.kb.domain.kb.repository.PageRepository;

/**
 * Created by Zenger on 2019/4/30.
 */
@Service
public class PageServiceImpl implements PageService {

    private PageRepository pageRepository;

    public PageServiceImpl(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    @Override
    public Boolean checkPageCreate(Long id) {
        PageE pageE = pageRepository.selectById(id);
        if (pageE == null) {
            throw new CommonException("error.page.select");
        }
        CustomUserDetails customUserDetails = DetailsHelper.getUserDetails();
        return pageE.getCreatedBy().equals(customUserDetails.getUserId()) ? true : false;
    }
}
