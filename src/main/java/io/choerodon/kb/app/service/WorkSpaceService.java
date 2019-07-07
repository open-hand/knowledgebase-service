package io.choerodon.kb.app.service;

import io.choerodon.kb.api.dao.*;
import io.choerodon.kb.infra.dataobject.WorkSpaceDO;

import java.util.List;
import java.util.Map;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface WorkSpaceService {

    PageDTO create(Long resourceId, PageCreateDTO pageCreateDTO, String type);

    PageDTO queryDetail(Long organizationId, Long projectId, Long workSpaceId, String searchStr);

    PageDTO update(Long resourceId, Long id, PageUpdateDTO pageUpdateDTO, String type);

    void delete(Long resourceId, Long id, String type, Boolean isAdmin);

    void moveWorkSpace(Long resourceId, Long id, MoveWorkSpaceDTO moveWorkSpaceDTO, String type);

    Map<String, Object> queryAllChildTreeByWorkSpaceId(Long workSpaceId, Boolean isNeedChild);

    Map<String, Object> queryAllTree(Long resourceId, Long expandWorkSpaceId, String type);

    List<WorkSpaceDO> queryAllSpaceByProject();

    List<WorkSpaceDTO> queryAllSpaceByOptions(Long resourceId, String type);

    List<WorkSpaceDTO> querySpaceByIds(Long projectId, List<Long> spaceIds);
}
