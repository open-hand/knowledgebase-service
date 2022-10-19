package io.choerodon.kb.domain.service;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;

import io.choerodon.kb.api.vo.permission.PermissionCheckVO;
import io.choerodon.kb.api.vo.permission.PermissionTreeCheckVO;

/**
 * 知识库鉴权 Domain Service
 * @author gaokuo.dai@zknow.com 2022-10-12
 */
public interface PermissionCheckDomainService {

    /**
     * 知识库对象鉴权<br/>
     * 默认行为:<br/>
     * <ul>
     *     <li>处理完成后清除用户信息缓存</li>
     *     <li>检查父级权限</li>
     *     <li>进行对象安全设置一票否决投票</li>
     * </ul>
     * @param organizationId        组织ID
     * @param projectId             项目ID
     * @param targetBaseType        控制对象基础类型, 与targetType二选一即可
     * @param targetType            控制对象类型, 与targetBaseType二选一即可
     * @param targetValue           控制对象ID
     * @param permissionsWaitCheck  待鉴权的权限
     * @return                      鉴权结果
     */
    default List<PermissionCheckVO> checkPermission(
            @Nonnull Long organizationId,
            Long projectId,
            String targetBaseType,
            String targetType,
            @Nonnull Long targetValue,
            Collection<PermissionCheckVO> permissionsWaitCheck
    ) {
        return this.checkPermission(
                organizationId,
                projectId,
                targetBaseType,
                targetType,
                targetValue,
                permissionsWaitCheck,
                true
        );
    }

    /**
     * 知识库对象鉴权<br/>
     * 默认行为:<br/>
     * <ul>
     *     <li>检查父级权限</li>
     *     <li>进行对象安全设置一票否决投票</li>
     * </ul>
     * @param organizationId        组织ID
     * @param projectId             项目ID
     * @param targetBaseType        控制对象基础类型, 与targetType二选一即可
     * @param targetType            控制对象类型, 与targetBaseType二选一即可
     * @param targetValue           控制对象ID
     * @param permissionsWaitCheck  待鉴权的权限
     * @param clearUserInfoCache    是否清除用户信息缓存, 如果选择不自动清除, 请务必调用UserInfoVO.clearCurrentUserInfo()手动清除
     * @return                      鉴权结果
     */
    default List<PermissionCheckVO> checkPermission(
            @Nonnull Long organizationId,
            Long projectId,
            String targetBaseType,
            String targetType,
            @Nonnull Long targetValue,
            Collection<PermissionCheckVO> permissionsWaitCheck,
            boolean clearUserInfoCache
    ) {
        return this.checkPermission(
                organizationId,
                projectId,
                targetBaseType,
                targetType,
                targetValue,
                permissionsWaitCheck,
                clearUserInfoCache,
                true,
                true
        );
    }

    /**
     * 知识库对象鉴权
     * @param organizationId        组织ID
     * @param projectId             项目ID
     * @param targetBaseType        控制对象基础类型, 与targetType二选一即可
     * @param targetType            控制对象类型, 与targetBaseType二选一即可
     * @param targetValue           控制对象ID
     * @param permissionsWaitCheck  待鉴权的权限
     * @param clearUserInfoCache    是否清除用户信息缓存, 如果选择不自动清除, 请务必调用UserInfoVO.clearCurrentUserInfo()手动清除
     * @param checkWithParent       是否检查父级权限
     * @param doSecurityConfigVote  是否进行对象安全设置一票否决投票
     * @return                      鉴权结果
     */
    List<PermissionCheckVO> checkPermission(
            @Nonnull Long organizationId,
            Long projectId,
            String targetBaseType,
            String targetType,
            @Nonnull Long targetValue,
            Collection<PermissionCheckVO> permissionsWaitCheck,
            boolean clearUserInfoCache,
            boolean checkWithParent,
            boolean doSecurityConfigVote
    );

    /**
     * 知识库对象树鉴权
     * @param organizationId            组织ID
     * @param projectId                 项目ID
     * @param rootId                    根节点ID
     * @param rootTargetBaseType        根节点对象控制基础类型
     * @param permissionTreeWaitCheck   待鉴定权限
     * @return                          鉴权处理结果
     */
    List<PermissionTreeCheckVO> checkTreePermission(
            @Nonnull Long organizationId,
            Long projectId,
            Long rootId,
            String rootTargetBaseType,
            Collection<PermissionTreeCheckVO> permissionTreeWaitCheck
    );

    /**
     * 知识库对象鉴权
     * @param organizationId            组织ID
     * @param projectId                 项目ID
     * @param targetBaseType            控制对象基础类型, 与targetType二选一即可
     * @param targetType                控制对象类型, 与targetBaseType二选一即可
     * @param targetValue               控制对象ID
     * @param permissionCodeWaitCheck   待鉴权的权限
     * @return                          鉴权结果
     */
    default boolean checkPermission(
            @Nonnull Long organizationId,
            Long projectId,
            String targetBaseType,
            String targetType,
            @Nonnull Long targetValue,
            @Nonnull String permissionCodeWaitCheck
    ) {
        return this.checkPermission(
                organizationId,
                projectId,
                targetBaseType,
                targetType,
                targetValue,
                permissionCodeWaitCheck,
                true
        );
    }
    /**
     * 知识库鉴对象权
     * @param organizationId            组织ID
     * @param projectId                 项目ID
     * @param targetBaseType            控制对象基础类型, 与targetType二选一即可
     * @param targetType                控制对象类型, 与targetBaseType二选一即可
     * @param targetValue               控制对象ID
     * @param permissionCodeWaitCheck   待鉴权的权限
     * @param clearUserInfoCache        是否清除用户信息缓存, 如果选择不自动清除, 请务必调用UserInfoVO.clearCurrentUserInfo()手动清除
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

}
