package io.choerodon.kb.app.service;

import java.util.List;
import java.util.Map;

import io.choerodon.kb.api.dao.*;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface WorkSpaceService {

    PageDTO create(Long resourceId, PageCreateDTO pageCreateDTO, String type);

    PageDTO queryDetail(Long resourceId, Long id, String type);

    PageDTO update(Long resourceId, Long id, PageUpdateDTO pageUpdateDTO, String type);

    void delete(Long resourceId, Long id, String type, Boolean isAdmin);

    Map<Long, WorkSpaceTreeDTO> queryTree(Long resourceId, List<Long> parentIds, String type);

    Map<Long, WorkSpaceTreeDTO> queryParentTree(Long resourceId, Long id, String type);

    WorkSpaceFirstTreeDTO queryFirstTree(Long resourceId, String type);

    void moveWorkSpace(Long resourceId, Long id, MoveWorkSpaceDTO moveWorkSpaceDTO, String type);

    WorkSpaceOrganizationTreeDTO queryOrganizationTree(Long projectId);
}
