package io.choerodon.kb.domain.kb.repository;

import io.choerodon.kb.infra.dto.WorkSpacePageDTO;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface WorkSpacePageRepository {

    WorkSpacePageDTO baseCreate(WorkSpacePageDTO workSpacePageDTO);

    WorkSpacePageDTO baseUpdate(WorkSpacePageDTO workSpacePageDTO);

    WorkSpacePageDTO selectByWorkSpaceId(Long workSpaceId);

    void baseDelete(Long id);
}
