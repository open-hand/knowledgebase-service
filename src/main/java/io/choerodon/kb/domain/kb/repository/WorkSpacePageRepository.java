package io.choerodon.kb.domain.kb.repository;

import io.choerodon.kb.infra.dto.WorkSpacePageDTO;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface WorkSpacePageRepository {

    WorkSpacePageDTO insert(WorkSpacePageDTO workSpacePageDTO);

    WorkSpacePageDTO update(WorkSpacePageDTO workSpacePageDTO);

    WorkSpacePageDTO selectByWorkSpaceId(Long workSpaceId);

    void delete(Long id);
}
