package io.choerodon.kb.infra.permission.Voter;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;

import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.kb.api.vo.permission.PermissionCheckVO;
import io.choerodon.kb.api.vo.permission.UserInfoVO;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.repository.IamRemoteRepository;
import io.choerodon.kb.domain.repository.PermissionRangeKnowledgeObjectSettingRepository;
import io.choerodon.kb.domain.repository.PermissionRoleConfigRepository;
import io.choerodon.kb.infra.enums.PermissionConstants;

import org.hzero.core.util.Pair;

/**
 * 知识库对象鉴权投票器--权限范围控制基础实现
 * @author gaokuo.dai@zknow.com 2022-10-18
 */
public abstract class AbstractPermissionRangeVoter extends BasePermissionVoter implements PermissionVoter {

    @Autowired
    protected IamRemoteRepository iamRemoteRepository;
    // 这里只要是PermissionRangeBaseRepository的实现类都行, 所以随便注入了一个
    @Autowired
    protected PermissionRangeKnowledgeObjectSettingRepository permissionRangeRepository;
    @Autowired
    protected PermissionRoleConfigRepository permissionRoleConfigRepository;

    @Override
    public boolean onlyVoteSelf() {
        return false;
    }

    @Override
    public boolean needLogin() {
        return true;
    }

    /**
     * 知识库创建和知识库对象鉴权类型
     */
    protected Set<String> KNOWLEDGE_BASE_SETTING_CREATE_AND_OBJECT_TARGET_TYPES = SetUtils.union(
            PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_SETTING_CREATE_TARGET_TYPES,
            PermissionConstants.PermissionTargetType.OBJECT_SETTING_TARGET_TYPES
    );


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
        final List<String> permissionCodes = permissionWaitCheck.stream().map(PermissionCheckVO::getPermissionCode).collect(Collectors.toList());
        // 查询用户信息
        UserInfoVO userInfo = this.getUserInfo(userDetails, organizationId, projectId);
        // 查不到用户信息, 返回无权限
        if(userInfo == null) {
            return PermissionCheckVO.generateNonPermission(permissionWaitCheck);
        }
        // 查询权限范围缓存
        List<PermissionRange> permissionRanges = this.queryOneTargetPermissionWithRangeType(
                userInfo,
                organizationId,
                projectId,
                targetType,
                targetValue
        );
        // 如果没有查到任何权限范围授权, 则返回无权限
        if(CollectionUtils.isEmpty(permissionRanges)) {
            return PermissionCheckVO.generateNonPermission(permissionWaitCheck);
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
                        // 只要前置鉴权投票器返回的不是空值就证明有权限
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

    /**
     * 查询权限范围控制信息--仅单一对象, 不处理父级
     * @param userInfo              当前用户信息
     * @param organizationId        组织ID
     * @param projectId             项目ID
     * @param targetType            对象控制类
     * @param targetValue           对象ID
     * @return                      鉴权结果
     */
    abstract protected List<PermissionRange> queryOneTargetPermissionWithRangeType(
            UserInfoVO userInfo,
            Long organizationId,
            Long projectId,
            String targetType,
            Long targetValue
    );

    /**
     * 获取用户权限信息
     * @param userDetails       UserDetail
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @return                  用户权限信息
     */
    protected UserInfoVO getUserInfo(CustomUserDetails userDetails, Long organizationId, Long projectId) {
        UserInfoVO userInfo = UserInfoVO.currentUserInfo();
        if(userInfo == null) {
            // -- 如果ThreadLocal里没找到, 则调用IAM查询一次
            userInfo = this.iamRemoteRepository.queryUserInfo(userDetails.getUserId(), organizationId, projectId);
            if(userInfo == null) {
                UserInfoVO.putCurrentUserInfo(UserInfoVO.NONE);
            } else {
                UserInfoVO.putCurrentUserInfo(userInfo);
            }
        } else if(userInfo == UserInfoVO.NONE) {
            // -- 如果ThreadLocal里找到了但是为NONE, 则置空
            userInfo = null;
        }
        return userInfo;
    }

    @Nonnull
    @Override
    public Collector<List<PermissionCheckVO>, List<PermissionCheckVO>, List<PermissionCheckVO>> ticketCollectionRule() {
        return TicketCollectionRules.ANY_AGREE;
    }
}
