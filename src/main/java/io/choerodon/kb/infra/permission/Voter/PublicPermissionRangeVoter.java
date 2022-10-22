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
 * 知识库对象鉴权投票器--公开权限范围控制
 * @author gaokuo.dai@zknow.com 2022-10-18
 */
@Component
public class PublicPermissionRangeVoter extends AbstractPermissionRangeVoter implements PermissionVoter {
    @Override
    protected List<PermissionRange> queryOneTargetPermissionWithRangeType(
            UserInfoVO userInfo,
            Long organizationId,
            Long projectId,
            String targetType,
            Long targetValue
    ) {
        // 查询当前对象是否配置了公开权限
        final String permissionRoleCode = this.permissionRangeRepository.findPermissionRoleCodeWithCache(
                organizationId,
                projectId,
                targetType,
                targetValue,
                PermissionConstants.PermissionRangeType.PUBLIC.toString(),
                PermissionConstants.EMPTY_ID_PLACEHOLDER
        );
        // 返回结果
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
    @Nonnull
    public Set<String> applicabilityTargetType() {
        return PermissionConstants.PermissionTargetType.OBJECT_SETTING_TARGET_TYPES;
    }
}
