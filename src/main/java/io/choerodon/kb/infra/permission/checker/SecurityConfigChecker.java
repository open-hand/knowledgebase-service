package io.choerodon.kb.infra.permission.checker;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.kb.api.vo.permission.PermissionCheckVO;
import io.choerodon.kb.domain.repository.SecurityConfigRepository;
import io.choerodon.kb.infra.enums.PermissionConstants;

import org.hzero.core.base.BaseConstants;

@Component
public class SecurityConfigChecker extends BasePermissionChecker implements PermissionChecker{

    @Autowired
    private SecurityConfigRepository securityConfigRepository;

    @Override
    protected List<PermissionCheckVO> checkOneTargetPermission(
            @Nonnull CustomUserDetails userDetails,
            @Nonnull Long organizationId,
            @Nonnull Long projectId,
            @Nonnull String targetType,
            @Nonnull Long targetValue,
            Collection<PermissionCheckVO> permissionWaitCheck
    ) {
        if(CollectionUtils.isEmpty(permissionWaitCheck)) {
            return Collections.emptyList();
        }
        final List<String> permissionCodes = permissionWaitCheck.stream()
                .map(PermissionCheckVO::getPermissionCode)
                .collect(Collectors.toList());
        return this.securityConfigRepository.batchQueryAuthorizeFlagWithCache(
                organizationId,
                projectId,
                targetType,
                targetValue,
                permissionCodes
        ).stream()
                .map(pair -> new PermissionCheckVO()
                        .setPermissionCode(pair.getFirst())
                        .setApprove(BaseConstants.Flag.YES.equals(pair.getSecond()))
                        .setControllerType(
                                BaseConstants.Flag.YES.equals(pair.getSecond()) ?
                                        PermissionConstants.PermissionRole.MANAGER :
                                        PermissionConstants.PermissionRole.NULL
                        )
                )
                .collect(Collectors.toList());
    }

    @Override
    public Set<String> applicabilityTargetType() {
        return PermissionConstants.PermissionTargetType.OBJECT_SETTING_TARGET_TYPES;
    }
}
