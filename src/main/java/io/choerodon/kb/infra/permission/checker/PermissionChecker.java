package io.choerodon.kb.infra.permission.checker;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.kb.api.vo.permission.PermissionCheckVO;

public interface PermissionChecker {

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

}
