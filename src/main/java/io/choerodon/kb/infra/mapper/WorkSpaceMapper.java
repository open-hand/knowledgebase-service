package io.choerodon.kb.infra.mapper;

import io.choerodon.kb.infra.dto.PageDetailDTO;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface WorkSpaceMapper extends Mapper<WorkSpaceDTO> {

    Boolean hasChildWorkSpace(@Param("type") String type,
                              @Param("resourceId") Long resourceId,
                              @Param("parentId") Long parentId);

    String queryMaxRank(@Param("type") String type,
                        @Param("resourceId") Long resourceId,
                        @Param("parentId") Long parentId);

    String queryMinRank(@Param("type") String type,
                        @Param("resourceId") Long resourceId,
                        @Param("parentId") Long parentId);

    String queryRank(@Param("type") String type,
                     @Param("resourceId") Long resourceId,
                     @Param("id") Long id);

    String queryLeftRank(@Param("type") String type,
                         @Param("resourceId") Long resourceId,
                         @Param("parentId") Long parentId,
                         @Param("rightRank") String rightRank);

    String queryRightRank(@Param("type") String type,
                          @Param("resourceId") Long resourceId,
                          @Param("parentId") Long parentId,
                          @Param("leftRank") String leftRank);

    PageDetailDTO queryDetail(@Param("id") Long id);

    PageDetailDTO queryReferenceDetail(@Param("id") Long id);

    void deleteByRoute(@Param("route") String route);

    List<WorkSpaceDTO> workSpaceListByParentIds(@Param("resourceId") Long resourceId,
                                                @Param("parentIds") List<Long> parentIds,
                                                @Param("type") String type
    );

    List<WorkSpaceDTO> workSpaceListByParentId(@Param("resourceId") Long resourceId,
                                               @Param("parentId") Long parentId,
                                               @Param("type") String type
    );

    List<WorkSpaceDTO> workSpacesByParentId(@Param("parentId") Long parentId);

    void updateByRoute(@Param("type") String type,
                       @Param("resourceId") Long resourceId,
                       @Param("odlRoute") String odlRoute,
                       @Param("newRoute") String newRoute
    );

    List<WorkSpaceDTO> selectAllChildByRoute(@Param("route") String route);

    List<WorkSpaceDTO> queryAll(@Param("resourceId") Long resourceId, @Param("type") String type);

    List<WorkSpaceDTO> selectSpaceByIds(@Param("projectId") Long projectId, @Param("spaceIds") List<Long> spaceIds);
}
