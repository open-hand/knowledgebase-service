package io.choerodon.kb.infra.permission.Voter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
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

/**
 * 知识库对象鉴权投票器--安全设置
 * @author gaokuo.dai@zknow.com 2022-10-18
 */
@Component
public class SecurityConfigVoter extends BasePermissionVoter implements PermissionVoter {

    @Autowired
    private SecurityConfigRepository securityConfigRepository;

    @Override
    protected List<PermissionCheckVO> voteOneTargetPermission(
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
                // 安全设置投票器是一票否决投票器
                // 故仅处理安全设置相关权限
                // 否则会导致普通权限被强制否决
                .filter(permissionCode -> PermissionConstants.SecurityConfigAction.SECURITY_CONFIG_ACTION_CODES.stream().anyMatch(permissionCode::endsWith))
                .collect(Collectors.toList());
        // 查询安全控制设置
        return this.securityConfigRepository.batchQueryAuthorizeFlagWithCache(
                organizationId,
                projectId,
                targetType,
                targetValue,
                permissionCodes
        ).stream()
                // 返回查询结果
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
    @Nonnull
    public Set<String> applicabilityTargetType() {
        return PermissionConstants.PermissionTargetType.OBJECT_SETTING_TARGET_TYPES;
    }

    @Override
    public boolean onlyVoteSelf() {
        return true;
    }

    @Override
    public boolean needLogin() {
        return false;
    }

    @Nonnull
    @Override
    public Collector<List<PermissionCheckVO>, List<PermissionCheckVO>, List<PermissionCheckVO>> ticketCollectionRule() {
        return TicketCollectionRules.ONE_VETO;
    }
}
