package io.choerodon.kb.app.service;

import io.choerodon.kb.api.dao.*;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;

import java.util.List;
import java.util.Map;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface WorkSpaceService {

    PageVO create(Long resourceId, PageCreateVO pageCreateVO, String type);

    PageVO queryDetail(Long organizationId, Long projectId, Long workSpaceId, String searchStr);

    PageVO update(Long resourceId, Long id, PageUpdateVO pageUpdateVO, String type);

    void delete(Long resourceId, Long id, String type, Boolean isAdmin);

    void moveWorkSpace(Long resourceId, Long id, MoveWorkSpaceVO moveWorkSpaceVO, String type);

    Map<String, Object> queryAllChildTreeByWorkSpaceId(Long workSpaceId, Boolean isNeedChild);

    Map<String, Object> queryAllTree(Long resourceId, Long expandWorkSpaceId, String type);

    List<WorkSpaceDTO> queryAllSpaceByProject();

    List<WorkSpaceVO> queryAllSpaceByOptions(Long resourceId, String type);

    List<WorkSpaceVO> querySpaceByIds(Long projectId, List<Long> spaceIds);
}
