package io.choerodon.kb.infra.permission.checker;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.kb.api.vo.permission.PermissionCheckVO;

/**
 * 知识库对象鉴权器
 * @author gaokuo.dai@zknow.com 2022-10-18
 */
public interface PermissionChecker {

    /**
     * 知识库对象鉴权
     * @param userDetails           当前用户信息
     * @param organizationId        组织ID
     * @param projectId             项目ID
     * @param targetType            对象控制类型
     * @param targetValue           对象ID
     * @param permissionWaitCheck   待鉴定权限
     * @param checkWithParent       是否处理父级权限
     * @return                      鉴权结果
     */
    List<PermissionCheckVO> checkPermission(
            @Nonnull CustomUserDetails userDetails,
            @Nonnull Long organizationId,
            @Nonnull Long projectId,
            @Nonnull String targetType,
            @Nonnull Long targetValue,
            Collection<PermissionCheckVO> permissionWaitCheck,
            boolean checkWithParent
    );

    /**
     * @return 鉴权器适用的控制对象类型Code
     */
    Set<String> applicabilityTargetType();

    /**
     * @return 是否只校验自己, 不校验父级
     */
    boolean onlyCheckSelf();

}
