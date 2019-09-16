package io.choerodon.kb.infra.mapper;

import io.choerodon.kb.api.vo.WorkSpaceInfoVO;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface WorkSpaceMapper extends Mapper<WorkSpaceDTO> {

    WorkSpaceInfoVO queryWorkSpaceInfo(@Param("id") Long id);

    Boolean hasChildWorkSpace(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("parentId") Long parentId);

    String queryMaxRank(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("parentId") Long parentId);

    String queryMinRank(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("parentId") Long parentId);

    String queryRank(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("id") Long id);

    String queryLeftRank(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("parentId") Long parentId, @Param("rightRank") String rightRank);

    String queryRightRank(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("parentId") Long parentId, @Param("leftRank") String leftRank);

    List<WorkSpaceDTO> workSpaceListByParentIds(@Param("resourceId") Long resourceId,
                                                @Param("parentIds") List<Long> parentIds,
                                                @Param("type") String type);

    List<WorkSpaceDTO> workSpaceListByParentId(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("parentId") Long parentId);

    List<WorkSpaceDTO> workSpacesByParentId(@Param("parentId") Long parentId);

    void updateChildByRoute(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("oldRoute") String oldRoute, @Param("newRoute") String newRoute);

    /**
     * 只查询所有子空间，不包含自身
     *
     * @param route
     * @return
     */
    List<WorkSpaceDTO> selectAllChildByRoute(@Param("route") String route);

    List<WorkSpaceDTO> queryAll(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId);

    List<WorkSpaceDTO> selectSpaceByIds(@Param("projectId") Long projectId, @Param("spaceIds") List<Long> spaceIds);
}
