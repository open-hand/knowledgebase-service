package io.choerodon.kb.domain.repository;

import java.util.Collection;
import java.util.List;

import io.choerodon.kb.domain.entity.PermissionRange;

import org.hzero.mybatis.base.BaseRepository;

/**
 * 知识库权限应用范围资源库
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
public interface PermissionRangeBaseRepository extends BaseRepository<PermissionRange> {

    /**
     * 组装权限范围数据
     *
     * @param organizationId   租户id
     * @param permissionRanges 需要组装的权限范围数据
     */
    void assemblyRangeData(Long organizationId, List<PermissionRange> permissionRanges);

    /**
     * 根据id集合删除
     * @param ids   id集合
     * @return      被删除的数量
     */
    int deleteByIds(Collection<Long> ids);

}
