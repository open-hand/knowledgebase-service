package io.choerodon.kb.domain.repository;

import io.choerodon.kb.domain.entity.PermissionRange;

import org.hzero.mybatis.base.BaseRepository;

import java.util.Set;

/**
 * 知识库权限应用范围资源库
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
public interface PermissionRangeRepository extends BaseRepository<PermissionRange> {

    /**
     * 根据id批量删除
     *
     * @param ids
     */
    void deleteByIds(Set<Long> ids);

}
