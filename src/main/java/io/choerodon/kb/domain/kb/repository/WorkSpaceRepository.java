package io.choerodon.kb.domain.kb.repository;

import io.choerodon.kb.infra.dto.PageDetailDTO;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;

import java.util.List;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface WorkSpaceRepository {

    WorkSpaceDTO insert(WorkSpaceDTO workSpaceDTO);

    WorkSpaceDTO update(WorkSpaceDTO workSpaceDTO);

    List<WorkSpaceDTO> workSpacesByParentId(Long parentId);

    void updateByRoute(String type, Long resourceId, String odlRoute, String newRoute);

    Boolean hasChildWorkSpace(String type, Long resourceId, Long parentId);

    String queryMaxRank(String type, Long resourceId, Long parentId);

    String queryMinRank(String type, Long resourceId, Long parentId);

    String queryRank(String type, Long resourceId, Long id);

    String queryLeftRank(String type, Long resourceId, Long parentId, String rightRank);

    String queryRightRank(String type, Long resourceId, Long parentId, String leftRank);

    WorkSpaceDTO selectById(Long id);

    WorkSpaceDTO queryById(Long organizationId, Long projectId, Long workSpaceId);

    void checkById(Long organizationId, Long projectId, Long workSpaceId);

    PageDetailDTO queryDetail(Long id);

    PageDetailDTO queryReferenceDetail(Long id);

    void deleteByRoute(String route);

    List<WorkSpaceDTO> queryAllChildByWorkSpaceId(Long workSpaceId);

    List<WorkSpaceDTO> queryAll(Long resourceId, String type);
}
