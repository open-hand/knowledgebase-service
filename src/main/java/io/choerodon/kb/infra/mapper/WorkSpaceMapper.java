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

    List<RecycleVO> queryAllDeleteOptions(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("searchDTO") SearchDTO searchDTO, @Param("userInfo") UserInfoVO userInfo, @Param("rowNums") List<Integer> rowNums, Boolean permissionFlag);

    List<WorkSpaceDTO> selectSpaceByIds(@Param("projectId") Long projectId, @Param("spaceIds") List<Long> spaceIds);

    List<WorkSpaceRecentVO> selectRecent(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("baseId") Long baseId, @Param("permissionFlag") boolean permissionFlag, @Param("rowNums") List<Integer> rowNums, @Param("userInfo") UserInfoVO userInfo);

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
                                                    @Param("userInfo") UserInfoVO userInfo,
                                                    @Param("selfFlag") boolean selfFlag,
                                                    @Param("failed") boolean failed,
                                                    @Param("rowNums") List<Integer> rowNums,
                                                    @Param("permissionFlag") boolean permissionFlag);

    List<WorkSpaceDTO> queryWorkSpaceById(@Param("organizationId") Long organizationId,
                                          @Param("projectId") Long projectId,
                                          @Param("workSpaceId") Long workSpaceId);

    void deleteByIds(@Param("list") Set<Long> deleteFolderIds);


    List<WorkSpaceDTO> selectErrorRoute();

    /**
     * 根据id集合查询名称
     *
     * @param workSpaceIds id集合
     * @return 名称集合
     */
    List<WorkSpaceDTO> selectWorkSpaceNameByIds(Collection<Long> workSpaceIds);
}
