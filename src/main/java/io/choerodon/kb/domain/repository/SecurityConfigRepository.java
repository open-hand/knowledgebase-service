package io.choerodon.kb.domain.repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import io.choerodon.kb.api.vo.permission.PermissionSearchVO;
import io.choerodon.kb.domain.entity.SecurityConfig;

import org.hzero.core.util.Pair;
import org.hzero.mybatis.base.BaseRepository;

/**
 * 知识库安全设置资源库
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
public interface SecurityConfigRepository extends BaseRepository<SecurityConfig> {


    List<SecurityConfig> queryByTarget(Long organizationId, Long projectId, PermissionSearchVO searchVO);

    /**
     * 通过缓存查询是否授权操作, 查不到会去DB查询
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @param targetType        控制对象类型
     * @param targetValue       控制对象ID
     * @param permissionCode    操作权限Code
     * @return                  是否授权操作
     */
    Integer findAuthorizeFlagWithCache(Long organizationId, Long projectId, String targetType, Long targetValue, String permissionCode);

    /**
     * 通过缓存批量查询是否授权操作, 查不到会去DB查询
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @param targetType        控制对象类型
     * @param targetValue       控制对象ID
     * @param permissionCodes   操作权限Code集合
     * @return                  Set&lt;Pair&lt;操作权限Code, 是否授权操作&gt;&gt;
     */
    Set<Pair<String, Integer>> batchQueryAuthorizeFlagWithCache(Long organizationId, Long projectId, String targetType, Long targetValue, Collection<String> permissionCodes);

    /**
     * 清除所有缓存
     */
    void clearCache();

    /**
     * 清除有变更的数据的缓存
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @param securityConfigs   有变更的数据
     */
    void clearCache(Long organizationId, Long projectId, List<SecurityConfig> securityConfigs);
}
