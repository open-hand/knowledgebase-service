package io.choerodon.kb.infra.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

import io.choerodon.kb.infra.dto.PageAttachmentDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageAttachmentMapper extends BaseMapper<PageAttachmentDTO> {

    List<PageAttachmentDTO> selectByPageId(@Param("pageId") Long pageId);

    List<PageAttachmentDTO> selectIn(@Param("ids") List<Long> ids);

    List<PageAttachmentDTO> queryByFileName(@Param("organizationId") Long organizationId,
                                            @Param("projectId") Long projectId,
                                            @Param("fileName") String fileName);

    void batchInsert(@Param("list") List<PageAttachmentDTO> list);
}
