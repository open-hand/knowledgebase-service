package io.choerodon.kb.infra.permission.checker;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.kb.api.vo.permission.PermissionCheckVO;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.entity.UserInfo;
import io.choerodon.kb.domain.repository.IamRemoteRepository;
import io.choerodon.kb.domain.repository.PermissionRangeKnowledgeObjectSettingRepository;
import io.choerodon.kb.domain.repository.PermissionRoleConfigRepository;
import io.choerodon.kb.infra.enums.PermissionConstants;

import org.hzero.core.base.BaseConstants;
import org.hzero.core.util.Pair;

public abstract class AbstractPermissionRangeChecker extends BasePermissionChecker implements PermissionChecker{

    @Autowired
    protected IamRemoteRepository iamRemoteRepository;
    // 这里只要是PermissionRangeBaseRepository的实现类都行, 所以随便注入了一个
    @Autowired
    protected PermissionRangeKnowledgeObjectSettingRepository permissionRangeRepository;
    @Autowired
    protected PermissionRoleConfigRepository permissionRoleConfigRepository;

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
        final List<String> permissionCodes = permissionWaitCheck.stream().map(PermissionCheckVO::getPermissionCode).collect(Collectors.toList());
        // 查询用户信息
        final UserInfo userInfo = this.iamRemoteRepository.queryUserInfo(userDetails.getUserId(), organizationId, projectId);
        Assert.notNull(userInfo, BaseConstants.ErrorCode.DATA_NOT_EXISTS);
        // 查询权限范围缓存
        List<PermissionRange> permissionRanges = this.checkOneTargetPermissionWithRangeType(
                userInfo,
                organizationId,
                projectId,
                targetType,
                targetValue,
                permissionWaitCheck
        );
        // 如果没有查到任何角色, 则返回无权限
        if(CollectionUtils.isEmpty(permissionRanges)) {
            return permissionWaitCheck.stream()
                    .map(checkInfo -> checkInfo.setApprove(Boolean.FALSE).setControllerType(PermissionConstants.PermissionRole.NULL))
                    .collect(Collectors.toList());
        }

        List<PermissionCheckVO> result = new ArrayList<>();
        for (PermissionRange permissionRange : permissionRanges) {
            // 查询权限矩阵缓存
            Set<Pair<String, Boolean>> roleConfigResults = this.permissionRoleConfigRepository.batchQueryAuthorizeFlagWithCache(
                    organizationId,
                    projectId,
                    permissionRange.getTargetBaseType(),
                    permissionRange.getTargetValue(),
                    permissionRange.getPermissionRoleCode(),
                    permissionCodes
            );
            // 通过权限范围和权限矩阵计算出操作权限
            result.addAll(
                    roleConfigResults.stream()
                            .map(roleConfigResult -> new PermissionCheckVO()
                                    .setPermissionCode(roleConfigResult.getFirst())
                                    .setApprove(Boolean.TRUE.equals(roleConfigResult.getSecond()))
                                    .setControllerType(permissionRange.getPermissionRoleCode())
                            ).collect(Collectors.toList())
            );
        }

        return result;
    }

    abstract protected List<PermissionRange> checkOneTargetPermissionWithRangeType(
            UserInfo userInfo,
            Long organizationId,
            Long projectId,
            String targetType,
            Long targetValue,
            Collection<PermissionCheckVO> permissionWaitCheck
    );


}
