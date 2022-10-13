package io.choerodon.kb.infra.permission.checker;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import io.choerodon.kb.api.vo.permission.PermissionCheckVO;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.entity.UserInfo;
import io.choerodon.kb.infra.enums.PermissionConstants;

@Component
public class UserPermissionRangeChecker extends AbstractPermissionRangeChecker implements PermissionChecker{
    @Override
    protected List<PermissionRange> checkOneTargetPermissionWithRangeType(
            UserInfo userInfo,
            Long organizationId,
            Long projectId,
            String targetType,
            Long targetValue,
            Collection<PermissionCheckVO> permissionWaitCheck
    ) {
        final Long userId = userInfo.getUserId();
        if(userId == null) {
            return Collections.emptyList();
        }
        final String permissionRoleCode = this.permissionRangeRepository.findPermissionRoleCodeWithCache(
                organizationId,
                targetValue,
                targetType,
                targetValue,
                PermissionConstants.PermissionRangeType.USER.toString(),
                userId
        );
        return Collections.singletonList(PermissionRange.of(
                organizationId,
                projectId,
                targetType,
                targetValue,
                PermissionConstants.PermissionRangeType.USER.toString(),
                userId,
                permissionRoleCode
        ));
    }
}
