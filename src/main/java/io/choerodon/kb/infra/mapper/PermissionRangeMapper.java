package io.choerodon.kb.infra.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.entity.UserInfo;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 知识库权限应用范围Mapper
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
public interface PermissionRangeMapper extends BaseMapper<PermissionRange> {

    void clearByTarget(@Param("organizationId") Long organizationId,
                       @Param("projectId") long projectId,
                       @Param("targetTypes") Set<String> targetTypes,
                       @Param("targetValue") Long targetValue);

    /**
     * 根据userInfo查询权限范围
     *
     * @param organizationId
     * @param projectId
     * @param targetType
     * @param targetValue
     * @param userInfo
     * @return
     */
    List<PermissionRange> queryByUser(@Param("organizationId") Long organizationId,
                                      @Param("projectId") Long projectId,
                                      @Param("targetType") String targetType,
                                      @Param("targetValue") Long targetValue,
                                      @Param("userInfo") UserInfo userInfo);
}
