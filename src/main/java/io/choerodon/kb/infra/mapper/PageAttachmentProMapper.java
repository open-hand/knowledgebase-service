package io.choerodon.kb.infra.mapper;

import io.choerodon.kb.infra.dto.PageAttachmentDTO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author superlee
 * @since 2022-03-16
 */
public interface PageAttachmentProMapper extends BaseMapper<PageAttachmentDTO> {

    List<PageAttachmentDTO> queryByFileName(@Param("organizationId") Long organizationId,
                                            @Param("projectId") Long projectId,
                                            @Param("fileName") String fileName);
}
