package io.choerodon.kb.infra.mapper;

import io.choerodon.kb.infra.dto.PageLogDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageLogMapper extends Mapper<PageLogDTO> {

    List<PageLogDTO> selectByPageId(@Param("pageId") Long pageId);
}
