package io.choerodon.kb.domain.service.impl;

import java.util.*;
import javax.annotation.Nonnull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.api.vo.permission.PermissionCheckVO;
import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.choerodon.kb.api.vo.permission.UserInfoVO;
import io.choerodon.kb.domain.service.PermissionCheckDomainService;
import io.choerodon.kb.infra.enums.PermissionConstants;
import io.choerodon.kb.infra.permission.checker.PermissionChecker;

import org.hzero.core.base.BaseConstants;

/**
 * 知识库鉴权 Domain Service 实现类
 *
 * @author gaokuo.dai@zknow.com 2022-10-12
 */
@Service
public class PermissionCheckDomainServiceImpl implements PermissionCheckDomainService {

    private final Set<PermissionChecker> permissionCheckers;

    public PermissionCheckDomainServiceImpl(@Autowired Set<PermissionChecker> permissionCheckers) {
        this.permissionCheckers = Optional.ofNullable(permissionCheckers).orElse(Collections.emptySet());
    }

    @Override
    public List<PermissionCheckVO> checkPermission(
            @Nonnull Long organizationId,
            Long projectId,
            String targetBaseType,
            String targetType,
            @Nonnull Long targetValue,
            Collection<PermissionCheckVO> permissionsWaitCheck
    ) {
        return this.checkPermission(
                organizationId,
                projectId,
                targetBaseType,
                targetType,
                targetValue,
                permissionsWaitCheck,
                true
        );
    }

    @Override
    public List<PermissionCheckVO> checkPermission(
            @Nonnull Long organizationId,
            Long projectId,
            String targetBaseType,
            String targetType,
            @Nonnull Long targetValue,
            Collection<PermissionCheckVO> permissionsWaitCheck,
            boolean checkPermission
    ) {
        // 基础校验
        if (CollectionUtils.isEmpty(permissionsWaitCheck)) {
            return Collections.emptyList();
        }
        Assert.notNull(organizationId, BaseConstants.ErrorCode.NOT_NULL);
        if (projectId == null) {
            projectId = PermissionConstants.EMPTY_ID_PLACEHOLDER;
        }
        // 处理targetBaseType
        if(StringUtils.isBlank(targetType)) {
            targetType = new PermissionDetailVO()
                    .setBaseTargetType(targetBaseType)
                    .transformBaseTargetType(projectId)
                    .getTargetType();
        }
        Assert.isTrue(StringUtils.isNotBlank(targetBaseType) || StringUtils.isNotBlank(targetType), BaseConstants.ErrorCode.NOT_NULL);
        Assert.notNull(targetValue, BaseConstants.ErrorCode.NOT_NULL);

        // 当前用户没有登录, 直接按无权限处理
        final CustomUserDetails userDetails = DetailsHelper.getUserDetails();
        if(userDetails == null) {
            return PermissionCheckVO.generateNonPermission(permissionsWaitCheck);
        }
        // 如果用户是超管, 则直接放行
        if(Boolean.TRUE.equals(userDetails.getAdmin())) {
            return PermissionCheckVO.generateManagerPermission(permissionsWaitCheck);
        }

        Long finalProjectId = projectId;
        String finalTargetType = targetType;
        // 预留下后续reactive化改造空间
        final List<PermissionCheckVO> result = this.permissionCheckers.stream()
                // 取出生效的鉴权器
                .filter(checker -> checker.applicabilityTargetType().contains(finalTargetType))
                // 鉴权
                .map(checker -> checker.checkPermission(userDetails, organizationId, finalProjectId, finalTargetType, targetValue, permissionsWaitCheck))
                // 合并鉴权结果
                .collect(PermissionCheckVO.permissionCombiner);
        // 清理用户信息缓存
        if(checkPermission) {
            UserInfoVO.clearCurrentUserInfo();
        }
        return result;
    }

    @Override
    public boolean checkPermission(
            @Nonnull Long organizationId,
            Long projectId,
            String targetBaseType,
            String targetType,
            @Nonnull Long targetValue,
            @Nonnull String permissionCodeWaitCheck
    ) {
        return this.checkPermission(
                organizationId,
                projectId,
                targetBaseType,
                targetType,
                targetValue,
                permissionCodeWaitCheck,
                true
        );
    }

    @Override
    public boolean checkPermission(
            @Nonnull Long organizationId,
            Long projectId,
            String targetBaseType,
            String targetType,
            @Nonnull Long targetValue,
            @Nonnull String permissionCodeWaitCheck,
            boolean clearUserInfoCache
    ) {
        List<PermissionCheckVO> checkInfo = Collections.singletonList(new PermissionCheckVO().setPermissionCode(permissionCodeWaitCheck));
        checkInfo = this.checkPermission(organizationId, projectId, targetBaseType, targetType, targetValue, checkInfo);
        if(CollectionUtils.isEmpty(checkInfo)) {
            return false;
        }
        return Boolean.TRUE.equals(checkInfo.get(0).getApprove());
    }

}
