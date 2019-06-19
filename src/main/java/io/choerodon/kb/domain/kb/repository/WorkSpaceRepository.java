package io.choerodon.kb.domain.kb.repository;

import io.choerodon.kb.infra.dataobject.PageDetailDO;
import io.choerodon.kb.infra.dataobject.WorkSpaceDO;

import java.util.List;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface WorkSpaceRepository {

    WorkSpaceDO inset(WorkSpaceDO workSpaceDO);

    WorkSpaceDO update(WorkSpaceDO workSpaceDO);

    List<WorkSpaceDO> workSpaceListByParentIds(Long resourceId, List<Long> parentIds, String type);

    List<WorkSpaceDO> workSpaceListByParentId(Long resourceId, Long parentId, String type);

    List<WorkSpaceDO> workSpacesByParentId(Long parentId);

    void updateByRoute(String type, Long resourceId, String odlRoute, String newRoute);

    Boolean hasChildWorkSpace(String type, Long resourceId, Long parentId);

    String queryMaxRank(String type, Long resourceId, Long parentId);

    String queryMinRank(String type, Long resourceId, Long parentId);

    String queryRank(String type, Long resourceId, Long id);

    String queryLeftRank(String type, Long resourceId, Long parentId, String rightRank);

    String queryRightRank(String type, Long resourceId, Long parentId, String leftRank);

    int selectOrganizationId(Long orgId);

    WorkSpaceDO selectById(Long id);

    PageDetailDO queryDetail(Long id);

    PageDetailDO queryReferenceDetail(Long id);

    void deleteByRoute(String route);

    List<WorkSpaceDO> selectByRoute(String route);

    List<WorkSpaceDO> queryAll(Long resourceId, String type);
}
