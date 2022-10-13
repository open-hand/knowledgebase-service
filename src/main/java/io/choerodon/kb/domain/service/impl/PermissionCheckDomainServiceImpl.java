package io.choerodon.kb.domain.service.impl;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.validation.constraints.NotBlank;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.api.vo.permission.PermissionCheckVO;
import io.choerodon.kb.domain.service.PermissionCheckDomainService;
import io.choerodon.kb.infra.enums.PermissionConstants;
import io.choerodon.kb.infra.permission.checker.PermissionChecker;

import org.hzero.core.base.BaseConstants;

/**
 * 知识库鉴权 Domain Service 实现类
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
            @NotBlank String targetType,
            @Nonnull Long targetValue,
            @Nonnull Long knowledgeBaseId,
            Collection<PermissionCheckVO> permissionsWaitCheck
    ) {
        // 基础校验
        if(CollectionUtils.isEmpty(permissionsWaitCheck)) {
            return Collections.emptyList();
        }
        Assert.notNull(organizationId, BaseConstants.ErrorCode.NOT_NULL);
        if(projectId == null) {
            projectId = PermissionConstants.EMPTY_ID_PLACEHOLDER;
        }
        Assert.hasText(targetType, BaseConstants.ErrorCode.NOT_NULL);
        Assert.notNull(targetValue, BaseConstants.ErrorCode.NOT_NULL);
        Assert.notNull(knowledgeBaseId, BaseConstants.ErrorCode.NOT_NULL);

        // 当前用户没有登录, 直接按无权限处理
        final CustomUserDetails userDetails = DetailsHelper.getUserDetails();
        if(userDetails == null) {
            return permissionsWaitCheck.stream()
                    .peek(permissionCheck -> permissionCheck.setApprove(Boolean.FALSE).setControllerType(PermissionConstants.PermissionRole.NULL))
                    .collect(Collectors.toList());
        }
        // 如果用户是超管, 则直接放行
        if(Boolean.TRUE.equals(userDetails.getAdmin())) {
            return permissionsWaitCheck.stream()
                    .peek(permissionCheck -> permissionCheck.setApprove(Boolean.TRUE).setControllerType(PermissionConstants.PermissionRole.MANAGER))
                    .collect(Collectors.toList());
        }

        Long finalProjectId = projectId;
        // 预留下后续reactive化改造空间
        return this.permissionCheckers.stream()
                .map(checker -> checker.checkPermission(userDetails, organizationId, finalProjectId, targetType, targetValue, permissionsWaitCheck))
                .collect(PermissionCheckVO.permissionCombiner);
    }

    @Override
    public boolean checkPermission(
            @Nonnull Long organizationId,
            Long projectId,
            @NotBlank String targetType,
            @Nonnull Long targetValue,
            @Nonnull Long knowledgeBaseId,
            @Nonnull String permissionCodeWaitCheck
    ) {
        List<PermissionCheckVO> checkInfo = Collections.singletonList(new PermissionCheckVO().setPermissionCode(permissionCodeWaitCheck));
        checkInfo = this.checkPermission(organizationId, projectId, targetType, targetValue, knowledgeBaseId, checkInfo);
        if(CollectionUtils.isEmpty(checkInfo)) {
            return false;
        }
        return Boolean.TRUE.equals(checkInfo.get(0).getApprove());
    }

}
