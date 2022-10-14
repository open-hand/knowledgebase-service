package io.choerodon.kb.domain.repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.entity.UserInfo;

import org.hzero.core.util.Pair;
import org.hzero.mybatis.base.BaseRepository;

/**
 * 知识库权限应用范围资源库基础
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
public interface PermissionRangeBaseRepository extends BaseRepository<PermissionRange> {

    /**
     * 组装权限范围数据
     *
     * @param organizationId   租户id
     * @param permissionRanges 需要组装的权限范围数据
     * @return                 处理之后的数据
     */
    List<PermissionRange> assemblyRangeData(Long organizationId, List<PermissionRange> permissionRanges);

    /**
     * 查询用户的工作组和在组织/项目下的角色
     *
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @return                  查询结果
     */
    UserInfo queryUserInfo(Long organizationId,
                           Long projectId);

    /**
     * 通过缓存查询授权角色, 查不到会去DB查询
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @param targetType        控制对象类型
     * @param targetValue       控制对象ID
     * @param rangeType         授权对象类型
     * @param rangeValue        授权对象ID
     * @return                  授权角色Code
     */
    String findPermissionRoleCodeWithCache(Long organizationId, Long projectId, String targetType, Long targetValue, String rangeType, Long rangeValue);

    /**
     * 通过缓存批量查询授权角色, 查不到会去DB查询
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @param targetType        控制对象类型
     * @param targetValue       控制对象ID
     * @param rangePairs        Collection&lt;授权对象类型, 授权对象ID&gt;
     * @return                  Set&lt;Pair&lt;Pair&lt;授权对象类型, 授权对象ID&gt;授权角色Code&gt;&gt;
     */
    Set<Pair<Pair<String, Long>, String>> batchQueryPermissionRoleCodeWithCache(Long organizationId, Long projectId, String targetType, Long targetValue, Collection<Pair<String, Long>> rangePairs);

    /**
     * 清除所有缓存
     */
    void clearCache();

    /**
     * 清除有变更的数据的缓存
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @param permissionRanges  有变更的数据
     */
    void clearCache(Long organizationId, Long projectId, List<PermissionRange> permissionRanges);

}
