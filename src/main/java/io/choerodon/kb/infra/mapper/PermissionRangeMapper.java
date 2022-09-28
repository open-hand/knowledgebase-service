package io.choerodon.kb.infra.mapper;

import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.kb.domain.entity.PermissionRange;
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
}
