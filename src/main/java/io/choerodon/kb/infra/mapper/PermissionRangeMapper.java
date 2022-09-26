package io.choerodon.kb.infra.mapper;

import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.mybatis.common.BaseMapper;

import java.util.Set;

/**
 * 知识库权限应用范围Mapper
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
public interface PermissionRangeMapper extends BaseMapper<PermissionRange> {

    /**
     * 根据id集合删除
     *
     * @param ids
     */
    void deleteByIds(Set<Long> ids);
}
