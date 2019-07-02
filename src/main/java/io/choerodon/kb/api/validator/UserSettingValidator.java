package io.choerodon.kb.api.validator;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.dao.UserSettingDTO;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Created by HuangFuqiang@choerodon.io on 2019/07/02.
 * Email: fuqianghuang01@gmail.com
 */
@Component
public class UserSettingValidator {

    private static final String SETTING_TYPE_EDIT_MODE = "edit_mode";

    public void checkUserSettingCreateOrUpdate(UserSettingDTO userSettingDTO) {
        if (userSettingDTO.getOrganizationId() == null && userSettingDTO.getProjectId() == null) {
            throw new CommonException("error.orgAndPro.isNull");
        }
        if (userSettingDTO.getType() == null) {
            throw new CommonException("error.type.isNull");
        }
        if (userSettingDTO.getUserId() == null) {
            throw new CommonException("error.userId.isNull");
        }
        if (Objects.equals(userSettingDTO.getType(), SETTING_TYPE_EDIT_MODE) && userSettingDTO.getEditMode() == null) {
            throw new CommonException("error.editMode.isNull");
        }
        if (userSettingDTO.getId() != null && userSettingDTO.getObjectVeresionNumber() == null) {
            throw new CommonException("error.objectVersionNumber.isNull");
        }
    }
}
