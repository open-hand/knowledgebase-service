package io.choerodon.kb.infra.permission.checker;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import io.choerodon.kb.api.vo.permission.UserInfoVO;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.infra.enums.PermissionConstants;

@Component
public class UserPermissionRangeChecker extends AbstractPermissionRangeChecker implements PermissionChecker{
    @Override
    protected List<PermissionRange> checkOneTargetPermissionWithRangeType(
            UserInfoVO userInfo,
            Long organizationId,
            Long projectId,
            String targetType,
            Long targetValue
    ) {
        final Long userId = userInfo.getUserId();
        if(userId == null) {
            return Collections.emptyList();
        }
        if(PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_SETTING_CREATE_TARGET_TYPES.contains(targetType)) {
            // 目前知识库创建权限还没有项目级的, 都在组织层配置
            projectId = PermissionConstants.EMPTY_ID_PLACEHOLDER;
        }
        final String permissionRoleCode = this.permissionRangeRepository.findPermissionRoleCodeWithCache(
                organizationId,
                projectId,
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

    @Override
    public Set<String> applicabilityTargetType() {
        return KNOWLEDGE_BASE_SETTING_CREATE_AND_OBJECT_TARGET_TYPES;
    }
}
