package io.choerodon.kb.domain.kb.repository;

import java.util.List;

import io.choerodon.kb.domain.kb.entity.PageDetailE;
import io.choerodon.kb.domain.kb.entity.WorkSpaceE;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface WorkSpaceRepository {

    WorkSpaceE inset(WorkSpaceE workSpaceE);

    WorkSpaceE update(WorkSpaceE workSpaceE);

    List<WorkSpaceE> workSpaceListByParentIds(Long resourceId, List<Long> parentIds, String type);

    List<WorkSpaceE> workSpaceListByParentId(Long resourceId, Long parentId, String type);

    Boolean hasChildWorkSpace(String type, Long resourceId, Long parentId);

    String queryMaxRank(String type, Long resourceId, Long parentId);

    WorkSpaceE selectById(Long id);

    PageDetailE queryDetail(Long id);

    PageDetailE queryReferenceDetail(Long id);

    void deleteByRoute(String route);
}
