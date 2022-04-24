package io.choerodon.kb.infra.mapper;

import io.choerodon.kb.api.vo.*;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface WorkSpaceProMapper extends BaseMapper<WorkSpaceDTO> {

    List<WorkSpaceRecentVO> selectRecent(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("baseId") Long baseId);

}
