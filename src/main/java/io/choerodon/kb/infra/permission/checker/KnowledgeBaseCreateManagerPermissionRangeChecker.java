package io.choerodon.kb.infra.permission.checker;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import io.choerodon.kb.api.vo.permission.UserInfoVO;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.infra.enums.PermissionConstants;

/**
 * 知识库对象鉴权器--知识库创建权限--管理员权限范围控制
 * @author gaokuo.dai@zknow.com 2022-10-18
 */
@Component
public class KnowledgeBaseCreateManagerPermissionRangeChecker extends AbstractPermissionRangeChecker implements PermissionChecker{
    @Override
    protected List<PermissionRange> checkOneTargetPermissionWithRangeType(
            UserInfoVO userInfo,
            Long organizationId,
            Long projectId,
            String targetType,
            Long targetValue
    ) {
        boolean isManager = Boolean.TRUE.equals(userInfo.getManagerFlag());
        if(!isManager) {
            return Collections.emptyList();
        }
        // 目前知识库创建权限还没有项目级的, 都在组织层配置, 故projectId强制置为0
        projectId = PermissionConstants.EMPTY_ID_PLACEHOLDER;
        // 查询当前组织是否配置了管理者可创建知识库
        final String permissionRoleCode = this.permissionRangeRepository.findPermissionRoleCodeWithCache(
                organizationId,
                projectId,
                targetType,
                targetValue,
                PermissionConstants.PermissionRangeType.MANAGER.toString(),
                PermissionConstants.EMPTY_ID_PLACEHOLDER
        );
        // 返回结果
        return Collections.singletonList(PermissionRange.of(
                organizationId,
                projectId,
                targetType,
                targetValue,
                PermissionConstants.PermissionRangeType.MANAGER.toString(),
                PermissionConstants.EMPTY_ID_PLACEHOLDER,
                permissionRoleCode
        ));
    }

    @Override
    public Set<String> applicabilityTargetType() {
       return PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_SETTING_CREATE_TARGET_TYPES;
    }
}
