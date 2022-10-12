package io.choerodon.kb.domain.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.validation.constraints.NotBlank;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import io.choerodon.kb.api.vo.permission.PermissionCheckVO;
import io.choerodon.kb.domain.service.PermissionCheckDomainService;
import io.choerodon.kb.infra.enums.PermissionConstants;

import org.hzero.core.base.BaseConstants;

/**
 * 知识库鉴权 Domain Service 实现类
 * @author gaokuo.dai@zknow.com 2022-10-12
 */
@Service
public class PermissionCheckDomainServiceImpl implements PermissionCheckDomainService {
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

        // TODO 待实现
        return new ArrayList<>(permissionsWaitCheck);
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
        // 基础校验
        Assert.notNull(organizationId, BaseConstants.ErrorCode.NOT_NULL);
        if(projectId == null) {
            projectId = PermissionConstants.EMPTY_ID_PLACEHOLDER;
        }
        Assert.hasText(targetType, BaseConstants.ErrorCode.NOT_NULL);
        Assert.notNull(targetValue, BaseConstants.ErrorCode.NOT_NULL);
        Assert.notNull(knowledgeBaseId, BaseConstants.ErrorCode.NOT_NULL);
        Assert.hasText(permissionCodeWaitCheck, BaseConstants.ErrorCode.NOT_NULL);
        // TODO 待实现
        return false;
    }

}
