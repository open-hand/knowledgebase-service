package io.choerodon.kb.infra.mapper;

import io.choerodon.kb.infra.dto.PageAttachmentDTO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageAttachmentMapper extends BaseMapper<PageAttachmentDTO> {

    List<PageAttachmentDTO> selectByPageId(@Param("pageId") Long pageId);

    List<PageAttachmentDTO> selectIn(@Param("ids") List<Long> ids);

    List<PageAttachmentDTO> queryByFileName(@Param("organizationId") Long organizationId,
                                            @Param("projectId") Long projectId,
                                            @Param("fileName") String fileName,
                                            @Param("attachmentUrl") String attachmentUrl);

    void batchInsert(@Param("list") List<PageAttachmentDTO> list);
}
