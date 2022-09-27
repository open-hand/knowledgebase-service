package io.choerodon.kb.infra.mapper;

import java.util.Collection;

import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 知识库权限应用范围Mapper
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
public interface PermissionRangeMapper extends BaseMapper<PermissionRange> {

    /**
     * 根据id集合删除
     * @param ids   id集合
     * @return      被删除的数量
     */
    int deleteByIds(Collection<Long> ids);
}
