package io.choerodon.kb.domain.repository;

import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.dto.WorkSpacePageDTO;

import org.hzero.mybatis.base.BaseRepository;

public interface WorkSpacePageRepository extends BaseRepository<WorkSpacePageDTO> {

    void updatePageTitle(WorkSpaceDTO spaceDTO);
}
