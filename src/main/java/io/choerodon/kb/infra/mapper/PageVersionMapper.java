package io.choerodon.kb.infra.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

import io.choerodon.kb.infra.dto.PageVersionDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageVersionMapper extends BaseMapper<PageVersionDTO> {

    void deleteByPageId(@Param("pageId") Long pageId);

    List<PageVersionDTO> queryByPageId(@Param("pageId") Long pageId);

    String selectMaxVersionByPageId(@Param("pageId") Long pageId);
}
