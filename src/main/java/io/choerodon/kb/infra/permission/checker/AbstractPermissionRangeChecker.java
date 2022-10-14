package io.choerodon.kb.infra.permission.checker;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;

import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.kb.api.vo.permission.PermissionCheckVO;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.entity.UserInfo;
import io.choerodon.kb.domain.repository.IamRemoteRepository;
import io.choerodon.kb.domain.repository.PermissionRangeKnowledgeObjectSettingRepository;
import io.choerodon.kb.domain.repository.PermissionRoleConfigRepository;
import io.choerodon.kb.infra.enums.PermissionConstants;

import org.hzero.core.util.Pair;

public abstract class AbstractPermissionRangeChecker extends BasePermissionChecker implements PermissionChecker{

    @Autowired
    protected IamRemoteRepository iamRemoteRepository;
    // 这里只要是PermissionRangeBaseRepository的实现类都行, 所以随便注入了一个
    @Autowired
    protected PermissionRangeKnowledgeObjectSettingRepository permissionRangeRepository;
    @Autowired
    protected PermissionRoleConfigRepository permissionRoleConfigRepository;

    /**
     * 知识库创建和知识库对象鉴权类型
     */
    protected Set<String> KNOWLEDGE_BASE_SETTING_CREATE_AND_OBJECT_TARGET_TYPES = SetUtils.union(
            PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_SETTING_CREATE_TARGET_TYPES,
            PermissionConstants.PermissionTargetType.OBJECT_SETTING_TARGET_TYPES
    );


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
        // 查不到用户信息, 返回无权限
        if(userInfo == null) {
            return this.generateNonPermission(permissionWaitCheck);
        }
        // 查询权限范围缓存
        List<PermissionRange> permissionRanges = this.checkOneTargetPermissionWithRangeType(
                userInfo,
                organizationId,
                projectId,
                targetType,
                targetValue
        );
        // 如果没有查到任何权限角色, 则返回无权限
        if(CollectionUtils.isEmpty(permissionRanges)) {
            return this.generateNonPermission(permissionWaitCheck);
        }

        List<PermissionCheckVO> result = new ArrayList<>();
        for (PermissionRange permissionRange : permissionRanges) {
            Set<Pair<String, Boolean>> roleConfigResults;
            if(PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_SETTING_CREATE_TARGET_TYPES.contains(targetType)) {
                // 知识库创建权限不在矩阵里配置, 所以需要单独判断
                roleConfigResults = permissionWaitCheck.stream().map(checkInfo -> {
                    final String permissionCode = checkInfo.getPermissionCode();
                    if(!PermissionConstants.ACTION_PERMISSION_CREATE_KNOWLEDGE_BASE.equals(permissionCode)) {
                        // 知识库创建只有一种action, 不是这种action都视为鉴权不通过
                        return Pair.of(permissionCode, Boolean.FALSE);
                    } else {
                        // 知识库创建权限在PermissionRange中的PermissionRoleCode都是"NULL"
                        // 只要前置鉴权器返回的不是空值就证明有权限
                        return Pair.of(permissionCode, permissionRange.getPermissionRoleCode() != null);
                    }
                }).collect(Collectors.toSet());
            } else {
                // 知识库其他对象的权限走权限矩阵配置
                // 查询权限矩阵缓存
                roleConfigResults = this.permissionRoleConfigRepository.batchQueryAuthorizeFlagWithCache(
                        organizationId,
                        projectId,
                        permissionRange.getTargetBaseType(),
                        permissionRange.getTargetValue(),
                        permissionRange.getPermissionRoleCode(),
                        permissionCodes
                );
            }
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
            Long targetValue
    );

}
