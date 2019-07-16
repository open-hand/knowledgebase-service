package io.choerodon.kb.infra.persistence.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.dao.UserSettingVO;
import io.choerodon.kb.domain.kb.repository.UserSettingrepository;
import io.choerodon.kb.infra.dto.UserSettingDTO;
import io.choerodon.kb.infra.mapper.UserSettingMapper;
import org.modelmapper.ModelMapper;
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
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public void insert(UserSettingVO userSettingVO) {
        UserSettingDTO userSettingDTO = modelMapper.map(userSettingVO, UserSettingDTO.class);
        if (userSettingMapper.insert(userSettingDTO) != 1) {
            throw new CommonException("error.userSetting.insert");
        }
    }

    @Override
    public void updateBySelective(UserSettingVO userSettingVO) {
        UserSettingDTO userSettingDTO = modelMapper.map(userSettingVO, UserSettingDTO.class);
        if (userSettingMapper.updateByPrimaryKeySelective(userSettingDTO) != 1) {
            throw new CommonException("error.userSetting.update");
        }
    }
}
