package io.choerodon.kb.domain.service;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;

import io.choerodon.kb.api.vo.permission.PermissionCheckVO;
import io.choerodon.kb.domain.entity.PermissionCheckReader;

/**
 * 知识库鉴权 Domain Service
 * @author gaokuo.dai@zknow.com 2022-10-12
 */
public interface PermissionCheckDomainService {

    /**
     * 知识库鉴权
     * @param organizationId        组织ID
     * @param projectId             项目ID
     * @param targetBaseType        控制对象基础类型, 与targetType二选一即可
     * @param targetType            控制对象类型, 与targetBaseType二选一即可
     * @param targetValue           控制对象ID
     * @param permissionsWaitCheck  待鉴权的权限
     * @return                      鉴权结果
     */
    List<PermissionCheckVO> checkPermission(
            @Nonnull Long organizationId,
            Long projectId,
            String targetBaseType,
            String targetType,
            @Nonnull Long targetValue,
            Collection<PermissionCheckVO> permissionsWaitCheck
    );

    /**
     * 知识库鉴权
     * @param organizationId        组织ID
     * @param projectId             项目ID
     * @param targetBaseType        控制对象基础类型, 与targetType二选一即可
     * @param targetType            控制对象类型, 与targetBaseType二选一即可
     * @param targetValue           控制对象ID
     * @param permissionsWaitCheck  待鉴权的权限
     * @param clearUserInfoCache    是否清除用户信息缓存
     * @return                      鉴权结果
     */
    List<PermissionCheckVO> checkPermission(
            @Nonnull Long organizationId,
            Long projectId,
            String targetBaseType,
            String targetType,
            @Nonnull Long targetValue,
            Collection<PermissionCheckVO> permissionsWaitCheck,
            boolean clearUserInfoCache
    );

    /**
     * 知识库鉴权
     * @param organizationId            组织ID
     * @param projectId                 项目ID
     * @param targetBaseType        控制对象基础类型, 与targetType二选一即可
     * @param targetType            控制对象类型, 与targetBaseType二选一即可
     * @param targetValue               控制对象ID
     * @param permissionCodeWaitCheck   待鉴权的权限
     * @return                          鉴权结果
     */
    boolean checkPermission(
            @Nonnull Long organizationId,
            Long projectId,
            String targetBaseType,
            String targetType,
            @Nonnull Long targetValue,
            @Nonnull String permissionCodeWaitCheck
    );
    /**
     * 知识库鉴权
     * @param organizationId            组织ID
     * @param projectId                 项目ID
     * @param targetBaseType        控制对象基础类型, 与targetType二选一即可
     * @param targetType            控制对象类型, 与targetBaseType二选一即可
     * @param targetValue               控制对象ID
     * @param permissionCodeWaitCheck   待鉴权的权限
     * @param clearUserInfoCache        是否清除用户信息缓存
     * @return                          鉴权结果
     */
    boolean checkPermission(
            @Nonnull Long organizationId,
            Long projectId,
            String targetBaseType,
            String targetType,
            @Nonnull Long targetValue,
            @Nonnull String permissionCodeWaitCheck,
            boolean clearUserInfoCache
    );

    boolean checkPermissionReader(@Nonnull Long organizationId,
                                  Long projectId,
                                  PermissionCheckReader permissionCheckReader);

    List<PermissionCheckReader> checkPermissionReader(@Nonnull Long organizationId,
                                                      Long projectId,
                                                      List<PermissionCheckReader> permissionCheckReaders);

}
