package io.choerodon.kb.infra.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

import io.choerodon.kb.infra.dto.WorkSpacePageDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface WorkSpacePageMapper extends BaseMapper<WorkSpacePageDTO> {
    List<WorkSpacePageDTO> queryByPageIds(@Param("pageIds") List<Long> pageIds);
}
