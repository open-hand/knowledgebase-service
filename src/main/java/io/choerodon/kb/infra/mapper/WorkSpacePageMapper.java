package io.choerodon.kb.infra.mapper;

import io.choerodon.kb.infra.dto.WorkSpacePageDTO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface WorkSpacePageMapper extends BaseMapper<WorkSpacePageDTO> {
    List<WorkSpacePageDTO> queryByPageIds(@Param("pageIds") List<Long> pageIds);
}
