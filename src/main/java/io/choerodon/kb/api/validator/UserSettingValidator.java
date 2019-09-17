package io.choerodon.kb.api.validator;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.vo.UserSettingVO;
import io.choerodon.kb.infra.dto.UserSettingDTO;
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

    public void checkUserSettingCreateOrUpdate(Long organizationId, Long projectId, UserSettingVO userSettingVO) {
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

    public void checkUniqueRecode(UserSettingVO userSettingVO) {
        List<UserSettingDTO> userSettingDTOList = userSettingMapper.selectByOption(userSettingVO.getOrganizationId(), userSettingVO.getProjectId(), userSettingVO.getType(), userSettingVO.getUserId());
        if (userSettingDTOList != null && !userSettingDTOList.isEmpty()) {
            throw new CommonException("error.userSetting.exist");
        }
    }
}
