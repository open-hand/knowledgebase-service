package io.choerodon.kb.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.kb.infra.dto.PageCommentDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageCommentMapper extends BaseMapper<PageCommentDTO> {

    List<PageCommentDTO> selectByPageId(@Param("pageId") Long pageId);
}
