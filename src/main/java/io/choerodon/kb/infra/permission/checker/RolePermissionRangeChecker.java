package io.choerodon.kb.infra.permission.checker;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import io.choerodon.kb.api.vo.permission.PermissionCheckVO;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.entity.UserInfo;
import io.choerodon.kb.infra.enums.PermissionConstants;

import org.hzero.core.util.Pair;

@Component
public class RolePermissionRangeChecker extends AbstractPermissionRangeChecker implements PermissionChecker{
    @Override
    protected List<PermissionRange> checkOneTargetPermissionWithRangeType(
            UserInfo userInfo,
            Long organizationId,
            Long projectId,
            String targetType,
            Long targetValue,
            Collection<PermissionCheckVO> permissionWaitCheck
    ) {
        final Set<Long> roleIds = userInfo.getRoleIds();
        if(CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptyList();
        }
        final Set<Pair<Pair<String, Long>, String>> rangeToPermissionRoleCodePairs = this.permissionRangeRepository.batchQueryPermissionRoleCodeWithCache(
                organizationId,
                projectId,
                targetType,
                targetValue,
                roleIds.stream()
                        .map(roleId -> Pair.of(PermissionConstants.PermissionRangeType.ROLE.toString(), roleId))
                        .collect(Collectors.toList())
        );
        return rangeToPermissionRoleCodePairs.stream().map(pair -> PermissionRange.of(
                organizationId,
                projectId,
                targetType,
                targetValue,
                pair.getFirst().getFirst(),
                pair.getFirst().getSecond(),
                pair.getSecond()
        )).collect(Collectors.toList());
    }
}
