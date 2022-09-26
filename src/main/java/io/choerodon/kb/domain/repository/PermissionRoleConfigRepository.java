package io.choerodon.kb.domain.repository;

import io.choerodon.kb.domain.entity.PermissionRoleConfig;

import org.hzero.mybatis.base.BaseRepository;

/**
 * 知识库权限矩阵资源库
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
public interface PermissionRoleConfigRepository extends BaseRepository<PermissionRoleConfig> {

    /**
     * 根据唯一键查找实体
     * @param permissionRoleConfig 查询条件
     * @return 查询结果
     */
    PermissionRoleConfig findByUniqueKey(PermissionRoleConfig permissionRoleConfig);
    
}
