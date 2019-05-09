package io.choerodon.kb.domain.kb.repository;

import io.choerodon.kb.domain.kb.entity.WorkSpacePageE;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface WorkSpacePageRepository {

    WorkSpacePageE insert(WorkSpacePageE workSpacePageE);

    WorkSpacePageE update(WorkSpacePageE workSpacePageE);

    WorkSpacePageE selectByWorkSpaceId(Long workSpaceId);

    void delete(Long id);
}
