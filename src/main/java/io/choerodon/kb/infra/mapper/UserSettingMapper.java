package io.choerodon.kb.infra.mapper;

import io.choerodon.kb.infra.dto.UserSettingDTO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserSettingMapper extends BaseMapper<UserSettingDTO> {
    List<UserSettingDTO> selectByOption(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("type") String type, @Param("userId") Long userId);
}
