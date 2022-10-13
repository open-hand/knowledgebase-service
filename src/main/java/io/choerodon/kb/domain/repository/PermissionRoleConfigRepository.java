package io.choerodon.kb.domain.repository;

import java.util.Collection;
import java.util.Set;

import io.choerodon.kb.domain.entity.PermissionRoleConfig;

import org.hzero.core.util.Pair;
import org.hzero.mybatis.base.BaseRepository;

/**
 * 知识库权限矩阵资源库
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
public interface PermissionRoleConfigRepository extends BaseRepository<PermissionRoleConfig> {

    /**
     * 重新加载缓存<br/>
     * 缓存结构:<br/>
     * HASH<br/>
     * key: knowledge:permission:role-config:${tenantId}:${projectId}:${targetBaseType}:${permissionRoleCode}<br/>
     * hash-key: ${permissionCode}<br/>
     * value: ${authorizeFlag}
     */
    void reloadCache();

    /**
     * 根据唯一键查找实体
     * @param permissionRoleConfig 查询条件
     * @return 查询结果
     */
    PermissionRoleConfig findByUniqueKey(PermissionRoleConfig permissionRoleConfig);

    /**
     * 通过缓存查询授权标识
     * @param organizationId        组织ID
     * @param projectId             项目ID
     * @param targetBaseType        控制对象基础类型
     * @param targetValue           控制对象ID
     * @param permissionRoleCode    授权角色
     * @param permissionCode        操作权限Code
     * @return                      授权标识
     */
    Boolean findAuthorizeFlagWithCache(Long organizationId, Long projectId, String targetBaseType, Long targetValue, String permissionRoleCode, String permissionCode);

    /**
     * 通过缓存批量查询授权标识
     * @param organizationId        组织ID
     * @param projectId             项目ID
     * @param targetBaseType        控制对象基础类型
     * @param targetValue           控制对象ID
     * @param permissionRoleCode    授权角色
     * @param permissionCodes       操作权限Code集合
     * @return                      授权标识
     */
    Set<Pair<String, Boolean>> batchQueryAuthorizeFlagWithCache(Long organizationId, Long projectId, String targetBaseType, Long targetValue, String permissionRoleCode, Collection<String> permissionCodes);
}
