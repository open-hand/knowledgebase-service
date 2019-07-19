package io.choerodon.kb.app.service;

import io.choerodon.kb.infra.dto.WorkSpacePageDTO;

/**
 * @author shinan.chen
 * @since 2019/7/17
 */
public interface WorkSpacePageService {

    WorkSpacePageDTO baseCreate(WorkSpacePageDTO workSpacePageDTO);

    WorkSpacePageDTO selectByWorkSpaceId(Long workSpaceId);

    void baseDelete(Long id);
}
