package io.choerodon.kb.infra.permission.checker;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import io.choerodon.kb.api.vo.permission.UserInfoVO;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.infra.enums.PermissionConstants;

@Component
public class PublicPermissionRangeChecker extends AbstractPermissionRangeChecker implements PermissionChecker{
    @Override
    protected List<PermissionRange> checkOneTargetPermissionWithRangeType(
            UserInfoVO userInfo,
            Long organizationId,
            Long projectId,
            String targetType,
            Long targetValue
    ) {
        final String permissionRoleCode = this.permissionRangeRepository.findPermissionRoleCodeWithCache(
                organizationId,
                targetValue,
                targetType,
                targetValue,
                PermissionConstants.PermissionRangeType.PUBLIC.toString(),
                PermissionConstants.EMPTY_ID_PLACEHOLDER
        );
        return Collections.singletonList(PermissionRange.of(
                organizationId,
                projectId,
                targetType,
                targetValue,
                PermissionConstants.PermissionRangeType.PUBLIC.toString(),
                PermissionConstants.EMPTY_ID_PLACEHOLDER,
                permissionRoleCode
        ));
    }

    @Override
    public Set<String> applicabilityTargetType() {
        return PermissionConstants.PermissionTargetType.OBJECT_SETTING_TARGET_TYPES;
    }
}
