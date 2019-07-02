package io.choerodon.kb.api.validator;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.dao.UserSettingDTO;
import io.choerodon.kb.infra.dataobject.UserSettingDO;
import io.choerodon.kb.infra.mapper.UserSettingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Created by HuangFuqiang@choerodon.io on 2019/07/02.
 * Email: fuqianghuang01@gmail.com
 */
@Component
public class UserSettingValidator {

    private static final String SETTING_TYPE_EDIT_MODE = "edit_mode";

    @Autowired
    private UserSettingMapper userSettingMapper;

    public void checkUserSettingCreateOrUpdate(Long organizationId, Long projectId, UserSettingDTO userSettingDTO) {
        if (userSettingDTO.getType() == null) {
            throw new CommonException("error.type.isNull");
        }
        if (Objects.equals(userSettingDTO.getType(), SETTING_TYPE_EDIT_MODE) && userSettingDTO.getEditMode() == null) {
            throw new CommonException("error.editMode.isNull");
        }
        if (userSettingDTO.getId() != null && userSettingDTO.getObjectVeresionNumber() == null) {
            throw new CommonException("error.objectVersionNumber.isNull");
        }
        if (organizationId != null) {
            userSettingDTO.setOrganizationId(organizationId);
        }
        if (projectId != null) {
            userSettingDTO.setProjectId(projectId);
        }
    }

    public void checkUniqueRecode(UserSettingDTO userSettingDTO) {
        UserSettingDO userSettingDO = null;
        if (userSettingDTO.getProjectId() == null) {
            userSettingDO = new UserSettingDO(userSettingDTO.getOrganizationId(), userSettingDTO.getType(), userSettingDTO.getUserId());
        } else {
            userSettingDO = new UserSettingDO(userSettingDTO.getOrganizationId(), userSettingDTO.getProjectId(), userSettingDTO.getType(), userSettingDTO.getUserId());
        }
        List<UserSettingDO> userSettingDOList = userSettingMapper.select(userSettingDO);
        if (userSettingDOList != null && !userSettingDOList.isEmpty()) {
            throw new CommonException("error.userSetting.exist");
        }
    }
}
