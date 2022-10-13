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
public class PublicPermissionRangeChecker extends AbstractPermissionRangeChecker implements PermissionChecker{
    @Override
    protected List<PermissionRange> checkOneTargetPermissionWithRangeType(
            UserInfo userInfo,
            Long organizationId,
            Long projectId,
            String targetType,
            Long targetValue,
            Collection<PermissionCheckVO> permissionWaitCheck
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
}
