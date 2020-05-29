package io.choerodon.kb.infra.mapper;

import io.choerodon.kb.infra.dto.PageVersionDTO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageVersionMapper extends BaseMapper<PageVersionDTO> {

    void deleteByPageId(@Param("pageId") Long pageId);

    List<PageVersionDTO> queryByPageId(@Param("pageId") Long pageId);

    String selectMaxVersionByPageId(@Param("pageId") Long pageId);
}
