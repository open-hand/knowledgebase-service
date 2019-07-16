package io.choerodon.kb.domain.kb.repository;

import io.choerodon.kb.infra.dto.WorkSpaceShareDTO;

/**
 * Created by Zenger on 2019/6/10.
 */
public interface WorkSpaceShareRepository {

    WorkSpaceShareDTO insert(WorkSpaceShareDTO workSpaceShareDTO);

    WorkSpaceShareDTO update(WorkSpaceShareDTO workSpaceShareDTO);

    void deleteByWorkSpaceId(Long workSpaceId);

    WorkSpaceShareDTO selectById(Long id);

    WorkSpaceShareDTO selectByWorkSpaceId(Long workSpaceId);

    WorkSpaceShareDTO selectOne(WorkSpaceShareDTO workSpaceShareDTO);
}
