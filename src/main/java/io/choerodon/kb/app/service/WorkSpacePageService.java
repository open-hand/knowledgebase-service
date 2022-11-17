package io.choerodon.kb.app.service;

import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.dto.WorkSpacePageDTO;

/**
 * @author shinan.chen
 * @since 2019/7/17
 */
public interface WorkSpacePageService {

    WorkSpacePageDTO selectByWorkSpaceId(Long workSpaceId);

    WorkSpacePageDTO selectByPageId(Long pageId);

    void baseDelete(Long id);

    void createOrUpdateEs(Long workSpaceId);

    void deleteEs(Long workSpaceId);

    void updatePageTitle(WorkSpaceDTO spaceDTO);
}
