package io.choerodon.kb.app.service.impl;

import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.api.dao.UserSettingVO;
import io.choerodon.kb.api.validator.UserSettingValidator;
import io.choerodon.kb.app.service.UserSettingService;
import io.choerodon.kb.domain.kb.repository.UserSettingrepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by HuangFuqiang@choerodon.io on 2019/07/02.
 * Email: fuqianghuang01@gmail.com
 */
@Service
public class UserSettingServiceImpl implements UserSettingService {

    @Autowired
    private UserSettingValidator userSettingValidator;

    @Autowired
    private UserSettingrepository userSettingrepository;

    @Override
    public void createOrUpdate(Long organizationId, Long projectId, UserSettingVO userSettingVO) {
        userSettingValidator.checkUserSettingCreateOrUpdate(organizationId, projectId, userSettingVO);
        CustomUserDetails customUserDetails = DetailsHelper.getUserDetails();
        userSettingVO.setUserId(customUserDetails.getUserId());
        if (userSettingVO.getId() == null) {
            userSettingValidator.checkUniqueRecode(userSettingVO);
            userSettingrepository.insert(userSettingVO);
        } else {
            userSettingrepository.updateBySelective(userSettingVO);
        }
    }

}
