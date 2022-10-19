package io.choerodon.kb.infra.permission.Voter;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

import org.springframework.stereotype.Component;

import io.choerodon.kb.api.vo.permission.UserInfoVO;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.infra.enums.PermissionConstants;

/**
 * 知识库对象鉴权投票器--用户权限范围控制
 * @author gaokuo.dai@zknow.com 2022-10-18
 */
@Component
public class UserPermissionRangeVoter extends AbstractPermissionRangeVoter implements PermissionVoter {
    @Override
    protected List<PermissionRange> queryOneTargetPermissionWithRangeType(
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
            // 故在进行知识库创建权限校验时, projectId强制置为0
            projectId = PermissionConstants.EMPTY_ID_PLACEHOLDER;
        }
        // 查询当前对象是否配置了当前用户的权限
        final String permissionRoleCode = this.permissionRangeRepository.findPermissionRoleCodeWithCache(
                organizationId,
                projectId,
                targetType,
                targetValue,
                PermissionConstants.PermissionRangeType.USER.toString(),
                userId
        );
        // 返回结果
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
    @Nonnull
    public Set<String> applicabilityTargetType() {
        return KNOWLEDGE_BASE_SETTING_CREATE_AND_OBJECT_TARGET_TYPES;
    }
}
