package io.choerodon.kb.infra.permission.checker;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import io.choerodon.kb.api.vo.permission.UserInfoVO;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.infra.enums.PermissionConstants;

import org.hzero.core.util.Pair;

/**
 * 知识库对象鉴权器--角色权限范围控制
 * @author gaokuo.dai@zknow.com 2022-10-18
 */
@Component
public class RolePermissionRangeChecker extends AbstractPermissionRangeChecker implements PermissionChecker{
    @Override
    protected List<PermissionRange> checkOneTargetPermissionWithRangeType(
            UserInfoVO userInfo,
            Long organizationId,
            Long projectId,
            String targetType,
            Long targetValue
    ) {
        final Set<Long> roleIds = userInfo.getRoleIds();
        if(CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptyList();
        }
        if(PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_SETTING_CREATE_TARGET_TYPES.contains(targetType)) {
            // 目前知识库创建权限还没有项目级的, 都在组织层配置
            // 故在进行知识库创建权限校验时, projectId强制置为0
            projectId = PermissionConstants.EMPTY_ID_PLACEHOLDER;
        }
        // 查询当前对象是否配置了当前角色的权限
        final Set<Pair<Pair<String, Long>, String>> rangeToPermissionRoleCodePairs = this.permissionRangeRepository.batchQueryPermissionRoleCodeWithCache(
                organizationId,
                projectId,
                targetType,
                targetValue,
                roleIds.stream()
                        .map(roleId -> Pair.of(PermissionConstants.PermissionRangeType.ROLE.toString(), roleId))
                        .collect(Collectors.toList())
        );
        // 返回结果
        final Long finalProjectId = projectId;
        return rangeToPermissionRoleCodePairs.stream().map(pair -> PermissionRange.of(
                organizationId,
                finalProjectId,
                targetType,
                targetValue,
                pair.getFirst().getFirst(),
                pair.getFirst().getSecond(),
                pair.getSecond()
        )).collect(Collectors.toList());
    }

    @Override
    public Set<String> applicabilityTargetType() {
        return KNOWLEDGE_BASE_SETTING_CREATE_AND_OBJECT_TARGET_TYPES;
    }
}
