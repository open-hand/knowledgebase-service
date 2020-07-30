package io.choerodon.kb.infra.mapper;

import java.util.AbstractList;
import java.util.List;

import io.choerodon.core.domain.Page;
import org.apache.ibatis.annotations.Param;

import io.choerodon.kb.api.vo.*;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface WorkSpaceMapper extends BaseMapper<WorkSpaceDTO> {

    WorkSpaceInfoVO queryWorkSpaceInfo(@Param("id") Long id);

    Boolean hasChildWorkSpace(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("parentId") Long parentId);

    String queryMaxRank(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("parentId") Long parentId);

    String queryMinRank(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("parentId") Long parentId);

    String queryRank(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("id") Long id);

    String queryLeftRank(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("parentId") Long parentId, @Param("rightRank") String rightRank);

    String queryRightRank(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("parentId") Long parentId, @Param("leftRank") String leftRank);

    List<WorkSpaceDTO> workSpaceListByParentIds(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId,
                                                @Param("parentIds") List<Long> parentIds);

    void updateChildDeleteByRoute(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("route") String route, @Param("delete") Boolean delete);

    void updateChildByRoute(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("oldRoute") String oldRoute, @Param("newRoute") String newRoute);

    /**
     * 只查询所有子空间，不包含自身
     *
     * @param route
     * @return
     */
    List<WorkSpaceDTO> selectAllChildByRoute(@Param("route") String route, @Param("isNotDelete") Boolean isNotDelete);

    List<WorkSpaceDTO> queryAll(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("baseId") Long baseId);

    List<WorkSpaceDTO> queryAllDelete(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId);

    List<RecycleVO> queryAllDeleteOptions(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("searchDTO") SearchDTO searchDTO);

    List<WorkSpaceDTO> selectSpaceByIds(@Param("projectId") Long projectId, @Param("spaceIds") List<Long> spaceIds);

    List<WorkSpaceRecentVO> selectRecent(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("baseId") Long baseId);

    List<Long> listAllParentIdByBaseId(Long organizationId, Long projectId, Long baseId);

    DocumentTemplateInfoVO queryDocumentTemplate(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("baseId") Long baseId, @Param("id") Long id);

    List<DocumentTemplateInfoVO> listDocumentTemplate(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("baseId") Long baseId, @Param("searchVO") SearchVO searchVO);

    List<WorkSpaceDTO> listTemplateByBaseIds(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("list") List<Long> baseIds);

    List<WorkSpaceRecentVO> querylatest(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("baseIds") List<Long> baseIds);

    //修复数据
    List<WorkSpaceDTO> selectAllWorkSpace(@Param("type") String type);

    void updateWorkSpace(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("baseId") Long baseId);

    List<WorkBenchRecentVO> selectProjectRecentList(@Param("organizationId") Long organizationId,
                                                    @Param("projectIdList") List<Long> projectIdList,
                                                    @Param("userId") Long userId,
                                                    @Param("selfFlag") boolean selfFlag,
                                                    @Param("failed") boolean failed);
}
