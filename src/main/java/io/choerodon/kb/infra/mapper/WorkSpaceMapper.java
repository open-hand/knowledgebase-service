package io.choerodon.kb.infra.mapper;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.kb.api.vo.*;
import io.choerodon.kb.api.vo.permission.UserInfoVO;
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

    /**
     * 根据route查询所有子级
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @param route             route
     */
    List<WorkSpaceDTO> selectChildByRoute(@Param("organizationId") Long organizationId,  @Param("projectId") Long projectId, @Param("route") String route);

    /**
     * 根据route更新所有子级
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @param oldRoute          旧Route
     * @param newRoute          新Route
     */
    void updateChildByRoute(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("oldRoute") String oldRoute, @Param("newRoute") String newRoute);

    /**
     * 只查询所有子空间，不包含自身
     *
     * @param route
     * @return
     */
    List<WorkSpaceDTO> selectAllChildByRoute(@Param("route") String route, @Param("isNotDelete") Boolean isNotDelete);

    List<WorkSpaceDTO> queryAll(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("baseId") Long baseId, @Param("type") String type, @Param("excludeTypes") List<String> excludeTypes);

    List<WorkSpaceDTO> queryAllDelete(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId);

    List<WorkSpaceDTO> selectSpaceByIds(@Param("projectId") Long projectId, @Param("spaceIds") Collection<Long> spaceIds);

    List<WorkSpaceSimpleVO> selectWithPermission(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("baseId") Long baseId, @Param("permissionFlag") boolean permissionFlag, @Param("rowNums") List<Integer> rowNums, @Param("userInfo") UserInfoVO userInfo);

    /**
     * 查文档的最大深度
     *
     * @param organizationId
     * @param projectId
     * @param baseId
     * @param deleteFlag
     * @return
     */
    Integer selectRecentMaxDepth(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("baseId") Long baseId, @Param("deleteFlag") boolean deleteFlag);

    List<Long> listAllParentIdByBaseId(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("baseId") Long baseId);

    DocumentTemplateInfoVO queryDocumentTemplate(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("baseId") Long baseId, @Param("id") Long id);

    List<DocumentTemplateInfoVO> listDocumentTemplate(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("baseId") Long baseId, @Param("searchVO") SearchVO searchVO);

    List<WorkSpaceDTO> listTemplateByBaseIds(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("list") Collection<Long> baseIds);

    List<WorkSpaceSimpleVO> queryLatest(@Param("organizationId") Long organizationId, @Param("baseIds") List<Long> baseIds);

    //修复数据
    List<WorkSpaceDTO> selectAllWorkSpace(@Param("type") String type);

    void updateWorkSpace(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("baseId") Long baseId);

    List<WorkBenchRecentVO> selectProjectRecentList(@Param("organizationId") Long organizationId,
                                                    @Param("projectIdList") List<Long> projectIdList,
                                                    @Param("selfFlag") boolean selfFlag,
                                                    @Param("isOrganizationAdmin") boolean isOrganizationAdmin,
                                                    @Param("userId") Long userId);

    List<WorkSpaceDTO> queryWorkSpaceById(@Param("organizationId") Long organizationId,
                                          @Param("projectId") Long projectId,
                                          @Param("workSpaceId") Long workSpaceId);

    List<WorkSpaceDTO> selectErrorRoute();

    /**
     * 根据id集合查询名称
     *
     * @param workSpaceIds id集合
     * @return 名称集合
     */
    List<WorkSpaceDTO> selectWorkSpaceNameByIds(Collection<Long> workSpaceIds);

    /**
     * 根据知识库id集合查询workspace
     *
     * @param knowledgeBaseIds
     * @return
     */
    List<WorkSpaceDTO> listByKnowledgeBaseIds(@Param("knowledgeBaseIds") Set<Long> knowledgeBaseIds);

    void syncTemplateFlag();

}
