package io.choerodon.kb.app.service.impl;

import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.api.vo.UserSettingVO;
import io.choerodon.kb.app.service.UserSettingService;
import io.choerodon.kb.infra.dto.UserSettingDTO;
import io.choerodon.kb.infra.mapper.UserSettingMapper;

/**
 * Created by HuangFuqiang@choerodon.io on 2019/07/02.
 * Email: fuqianghuang01@gmail.com
 */
@Service
public class UserSettingServiceImpl implements UserSettingService {

    private static final String SETTING_TYPE_EDIT_MODE = "edit_mode";
    private static final String ERROR_USER_SETTING_INSERT = "error.userSetting.insert";
    private static final String ERROR_USER_SETTING_UPDATE = "error.userSetting.update";
    @Autowired
    private UserSettingMapper userSettingMapper;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public UserSettingVO createOrUpdate(Long organizationId, Long projectId, UserSettingVO userSettingVO) {
        this.checkUserSettingCreateOrUpdate(organizationId, projectId, userSettingVO);
        CustomUserDetails customUserDetails = DetailsHelper.getUserDetails();
        if (customUserDetails == null) {
            return userSettingVO;
        }
        final Long userId = customUserDetails.getUserId();
        userSettingVO.setOrganizationId(organizationId);
        userSettingVO.setProjectId(projectId);
        userSettingVO.setUserId(userId);

        List<UserSettingDTO> userSettingsInDb = userSettingMapper.selectByOption(
                organizationId,
                projectId,
                userSettingVO.getType(),
                userId
        );
        if (CollectionUtils.isEmpty(userSettingsInDb)) {
            userSettingVO = this.baseCreate(userSettingVO);
        } else {
            final UserSettingDTO userSettingInDb = userSettingsInDb.get(0);
            userSettingInDb.setEditMode(userSettingVO.getEditMode());
            if (userSettingMapper.updateByPrimaryKeySelective(userSettingInDb) != 1) {
                throw new CommonException(ERROR_USER_SETTING_UPDATE);
            }
            userSettingVO = modelMapper.map(userSettingInDb, UserSettingVO.class);
        }
        return userSettingVO;
    }

    private void checkUserSettingCreateOrUpdate(Long organizationId, Long projectId, UserSettingVO userSettingVO) {
        if (userSettingVO.getType() == null) {
            throw new CommonException("error.type.isNull");
        }
        if (Objects.equals(userSettingVO.getType(), SETTING_TYPE_EDIT_MODE) && userSettingVO.getEditMode() == null) {
            throw new CommonException("error.editMode.isNull");
        }
        if (userSettingVO.getId() != null && userSettingVO.getObjectVersionNumber() == null) {
            throw new CommonException("error.objectVersionNumber.isNull");
        }
        if (organizationId != null) {
            userSettingVO.setOrganizationId(organizationId);
        }
        if (projectId != null) {
            userSettingVO.setProjectId(projectId);
        }
    }

    private UserSettingVO baseCreate(UserSettingVO userSettingVO) {
        UserSettingDTO userSettingDTO = modelMapper.map(userSettingVO, UserSettingDTO.class);
        if (userSettingMapper.insert(userSettingDTO) != 1) {
            throw new CommonException(ERROR_USER_SETTING_INSERT);
        }
        return modelMapper.map(userSettingDTO, UserSettingVO.class);
    }

}
