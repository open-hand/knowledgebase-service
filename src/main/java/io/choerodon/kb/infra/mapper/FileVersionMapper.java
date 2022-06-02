package io.choerodon.kb.infra.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

import io.choerodon.kb.infra.dto.FileVersionDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by wangxiang on 2022/5/24
 */
public interface FileVersionMapper extends BaseMapper<FileVersionDTO> {
    List<FileVersionDTO> findAllByFileId(@Param("fileId") String fileId, @Param("count") Integer count, @Param("offset") Integer offset);

    FileVersionDTO findMaxVersion(@Param("fileId") String fileId);
}
