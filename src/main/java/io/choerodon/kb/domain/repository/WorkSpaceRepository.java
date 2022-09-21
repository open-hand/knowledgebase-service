package io.choerodon.kb.domain.repository;

import java.util.List;

import io.choerodon.kb.infra.dto.WorkSpaceDTO;

import org.hzero.mybatis.base.BaseRepository;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface WorkSpaceRepository extends BaseRepository<WorkSpaceDTO> {

    List<WorkSpaceDTO> selectErrorRoute();
}
