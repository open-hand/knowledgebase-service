package io.choerodon.kb.infra.repository.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import io.choerodon.kb.domain.repository.UserSettingRepository;
import io.choerodon.kb.infra.dto.UserSettingDTO;
import io.choerodon.kb.infra.mapper.UserSettingMapper;

import org.hzero.mybatis.base.impl.BaseRepositoryImpl;

@Repository
public class UserSettingRepositoryImpl extends BaseRepositoryImpl<UserSettingDTO> implements UserSettingRepository {

    @Autowired
    private UserSettingMapper userSettingMapper;

    @Override
    public List<UserSettingDTO> selectByOption(Long organizationId, Long projectId, String type, Long userId) {
        return this.userSettingMapper.selectByOption(organizationId, projectId, type, userId);
    }
}
