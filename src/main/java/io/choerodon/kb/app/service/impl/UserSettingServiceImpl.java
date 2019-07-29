package io.choerodon.kb.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.api.validator.UserSettingValidator;
import io.choerodon.kb.api.vo.UserSettingVO;
import io.choerodon.kb.app.service.UserSettingService;
import io.choerodon.kb.infra.dto.UserSettingDTO;
import io.choerodon.kb.infra.mapper.UserSettingMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by HuangFuqiang@choerodon.io on 2019/07/02.
 * Email: fuqianghuang01@gmail.com
 */
@Service
public class UserSettingServiceImpl implements UserSettingService {

    private static final String ERROR_USERSETTING_INSERT = "error.userSetting.insert";
    private static final String ERROR_USERSETTING_UPDATE = "error.userSetting.update";
    @Autowired
    private UserSettingValidator userSettingValidator;
    @Autowired
    private UserSettingMapper userSettingMapper;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public void baseCreate(UserSettingVO userSettingVO) {
        UserSettingDTO userSettingDTO = modelMapper.map(userSettingVO, UserSettingDTO.class);
        if (userSettingMapper.insert(userSettingDTO) != 1) {
            throw new CommonException(ERROR_USERSETTING_INSERT);
        }
    }

    @Override
    public void baseUpdateBySelective(UserSettingVO userSettingVO) {
        UserSettingDTO userSettingDTO = modelMapper.map(userSettingVO, UserSettingDTO.class);
        if (userSettingMapper.updateByPrimaryKeySelective(userSettingDTO) != 1) {
            throw new CommonException(ERROR_USERSETTING_UPDATE);
        }
    }

    @Override
    public void createOrUpdate(Long organizationId, Long projectId, UserSettingVO userSettingVO) {
        userSettingValidator.checkUserSettingCreateOrUpdate(organizationId, projectId, userSettingVO);
        CustomUserDetails customUserDetails = DetailsHelper.getUserDetails();
        if (customUserDetails != null) {
            userSettingVO.setUserId(customUserDetails.getUserId());
            if (userSettingVO.getId() == null) {
                userSettingVO.setProjectId(projectId);
                userSettingVO.setOrganizationId(organizationId);
                userSettingValidator.checkUniqueRecode(userSettingVO);
                this.baseCreate(userSettingVO);
            } else {
                this.baseUpdateBySelective(userSettingVO);
            }
        }
    }

}
