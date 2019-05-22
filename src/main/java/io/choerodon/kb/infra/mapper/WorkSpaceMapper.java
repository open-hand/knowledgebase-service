package io.choerodon.kb.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.kb.infra.dataobject.PageDetailDO;
import io.choerodon.kb.infra.dataobject.WorkSpaceDO;
import io.choerodon.mybatis.common.Mapper;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface WorkSpaceMapper extends Mapper<WorkSpaceDO> {

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

    PageDetailDO queryDetail(@Param("id") Long id);

    PageDetailDO queryReferenceDetail(@Param("id") Long id);

    void deleteByRoute(@Param("route") String route);

    List<WorkSpaceDO> workSpaceListByParentIds(@Param("resourceId") Long resourceId,
                                               @Param("parentIds") List<Long> parentIds,
                                               @Param("type") String type
    );

    List<WorkSpaceDO> workSpaceListByParentId(@Param("resourceId") Long resourceId,
                                              @Param("parentId") Long parentId,
                                              @Param("type") String type
    );

    List<WorkSpaceDO> workSpacesByParentId(@Param("parentId") Long parentId);

    void updateByRoute(@Param("type") String type,
                       @Param("resourceId") Long resourceId,
                       @Param("odlRoute") String odlRoute,
                       @Param("newRoute") String newRoute
    );
}
