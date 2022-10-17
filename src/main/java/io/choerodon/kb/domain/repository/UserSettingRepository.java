package io.choerodon.kb.domain.repository;

import java.util.List;

import io.choerodon.kb.infra.dto.UserSettingDTO;

import org.hzero.mybatis.base.BaseRepository;

public interface UserSettingRepository extends BaseRepository<UserSettingDTO> {

    List<UserSettingDTO> selectByOption(Long organizationId, Long projectId, String type, Long userId);

}
