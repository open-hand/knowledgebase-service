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
 * 知识库对象鉴权投票器--知识库创建权限--成员权限范围控制
 * @author gaokuo.dai@zknow.com 2022-10-18
 */
@Component
public class KnowledgeBaseCreateMemberPermissionRangeVoter extends AbstractPermissionRangeVoter implements PermissionVoter {
    @Override
    protected List<PermissionRange> queryOneTargetPermissionWithRangeType(
            UserInfoVO userInfo,
            Long organizationId,
            Long projectId,
            String targetType,
            Long targetValue
    ) {
        boolean isMember = Boolean.TRUE.equals(userInfo.getMemberFlag());
        if(!isMember) {
            return Collections.emptyList();
        }
        // 目前知识库创建权限还没有项目级的, 都在组织层配置, 故projectId强制置为0
        projectId = PermissionConstants.EMPTY_ID_PLACEHOLDER;
        // 查询当前组织是否配置了成员可创建知识库
        final String permissionRoleCode = this.permissionRangeRepository.findPermissionRoleCodeWithCache(
                organizationId,
                projectId,
                targetType,
                targetValue,
                PermissionConstants.PermissionRangeType.MEMBER.toString(),
                PermissionConstants.EMPTY_ID_PLACEHOLDER
        );
        // 返回结果
        return Collections.singletonList(PermissionRange.of(
                organizationId,
                projectId,
                targetType,
                targetValue,
                PermissionConstants.PermissionRangeType.MEMBER.toString(),
                PermissionConstants.EMPTY_ID_PLACEHOLDER,
                permissionRoleCode
        ));
    }

    @Override
    @Nonnull
    public Set<String> applicabilityTargetType() {
       return PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_SETTING_CREATE_TARGET_TYPES;
    }
}
