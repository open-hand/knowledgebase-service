package io.choerodon.kb.infra.persistence.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.dao.UserSettingDTO;
import io.choerodon.kb.domain.kb.repository.UserSettingrepository;
import io.choerodon.kb.infra.dataobject.UserSettingDO;
import io.choerodon.kb.infra.mapper.UserSettingMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by HuangFuqiang@choerodon.io on 2019/07/02.
 * Email: fuqianghuang01@gmail.com
 */
@Component
public class UserSettingrepositoryImpl implements UserSettingrepository {

    @Autowired
    private UserSettingMapper userSettingMapper;

    @Override
    public void insert(UserSettingDTO userSettingDTO) {
        UserSettingDO userSettingDO = new UserSettingDO();
        BeanUtils.copyProperties(userSettingDTO, userSettingDO);
        if (userSettingMapper.insert(userSettingDO) != 1) {
            throw new CommonException("error.userSetting.insert");
        }
    }

    @Override
    public void updateBySelective(UserSettingDTO userSettingDTO) {
        UserSettingDO userSettingDO = new UserSettingDO();
        BeanUtils.copyProperties(userSettingDTO, userSettingDO);
        if (userSettingMapper.updateByPrimaryKeySelective(userSettingDO) != 1) {
            throw new CommonException("error.userSetting.update");
        }
    }
}
