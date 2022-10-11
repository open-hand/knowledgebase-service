package io.choerodon.kb.domain.repository;

import java.util.Collection;
import java.util.List;

import io.choerodon.kb.infra.dto.WorkSpaceDTO;

import org.hzero.mybatis.base.BaseRepository;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface WorkSpaceRepository extends BaseRepository<WorkSpaceDTO> {

    List<WorkSpaceDTO> selectErrorRoute();

    /**
     * 根据id集合查询名称
     * @param workSpaceIds  id集合
     * @return              名称集合
     */
    List<WorkSpaceDTO> selectWorkSpaceNameByIds(Collection<Long> workSpaceIds);
}
