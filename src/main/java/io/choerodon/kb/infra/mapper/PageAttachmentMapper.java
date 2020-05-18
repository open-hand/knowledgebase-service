package io.choerodon.kb.infra.mapper;

import io.choerodon.kb.infra.dto.PageAttachmentDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageAttachmentMapper extends Mapper<PageAttachmentDTO> {

    List<PageAttachmentDTO> selectByPageId(@Param("pageId") Long pageId);

    List<PageAttachmentDTO> selectByIds(@Param("ids") List<Long> ids);

    List<PageAttachmentDTO> queryByFileName(@Param("organizationId") Long organizationId,
                                            @Param("projectId") Long projectId,
                                            @Param("fileName") String fileName,
                                            @Param("attachmentUrl") String attachmentUrl);

    void batchInsert(@Param("list") List<PageAttachmentDTO> list);
}
