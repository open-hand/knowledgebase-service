package io.choerodon.kb.domain.service.impl;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import io.choerodon.kb.api.vo.permission.PermissionTreeCheckVO;
import io.choerodon.kb.api.vo.permission.UserInfoVO;
import io.choerodon.kb.domain.service.PermissionCheckDomainService;
import io.choerodon.kb.infra.enums.PermissionConstants;
import io.choerodon.kb.infra.permission.Voter.PermissionVoter;
import io.choerodon.kb.infra.permission.Voter.SecurityConfigVoter;
import io.choerodon.kb.infra.permission.Voter.TicketCollectionRules;

import org.hzero.core.base.BaseConstants;

/**
 * 知识库鉴权 Domain Service 实现类
 *
 * @author gaokuo.dai@zknow.com 2022-10-12
 */
@Service
public class PermissionCheckDomainServiceImpl implements PermissionCheckDomainService {

    /**
     * 自动装配的鉴权器
     */
    private final Set<PermissionVoter> permissionVoters;
    private final SecurityConfigVoter securityConfigVoter;

    @Autowired
    public PermissionCheckDomainServiceImpl(
            Set<PermissionVoter> permissionVoters,
            SecurityConfigVoter securityConfigVoter
    ) {
        this.permissionVoters = Optional.ofNullable(permissionVoters).orElse(Collections.emptySet());
        this.securityConfigVoter = securityConfigVoter;
        this.permissionVoters.remove(this.securityConfigVoter);
    }

    @Override
    public List<PermissionCheckVO> checkPermission(
            @Nonnull Long organizationId,
            Long projectId,
            String targetBaseType,
            String targetType,
            @Nonnull Long targetValue,
            Collection<PermissionCheckVO> permissionsWaitCheck,
            boolean clearUserInfoCache,
            boolean checkWithParent,
            boolean doSecurityConfigVote
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
        // 流式处理, 预留下后续reactive化改造空间
        // -- 处理普通权限
        final List<PermissionCheckVO> normalPermissionResult = this.permissionVoters.stream()
                // 取出生效的鉴权器
                .filter(checker -> checker.applicabilityTargetType().contains(finalTargetType))
                // 鉴权
                .map(checker -> checker.votePermission(userDetails, organizationId, finalProjectId, finalTargetType, targetValue, permissionsWaitCheck, checkWithParent))
                // 合并鉴权结果
                .collect(TicketCollectionRules.ANY_AGREE);
        // -- 处理安全设置
        final List<PermissionCheckVO> securityConfigResult = doSecurityConfigVote ?
                Stream.of(this.securityConfigVoter)
                        .filter(checker -> checker.applicabilityTargetType().contains(finalTargetType))
                        // 鉴权
                        .map(checker -> checker.votePermission(userDetails, organizationId, finalProjectId, finalTargetType, targetValue, permissionsWaitCheck, checkWithParent))
                        // 合并鉴权结果
                        .collect(TicketCollectionRules.ONE_VETO) :
                Collections.emptyList();
        // -- 合并结果, 安全设置一票否决普通权限
        final List<PermissionCheckVO> result = doSecurityConfigVote ?
                Stream.of(normalPermissionResult, securityConfigResult).collect(TicketCollectionRules.ONE_VETO) :
                normalPermissionResult;
        // 清理用户信息缓存
        if(clearUserInfoCache) {
            UserInfoVO.clearCurrentUserInfo();
        }
        return result;
    }
    @Override
    public List<PermissionTreeCheckVO> checkTreePermission(
            @Nonnull Long organizationId,
            Long projectId,
            Long rootId,
            String rootTargetBaseType,
            Collection<PermissionTreeCheckVO> permissionTreeWaitCheck
    ){
        if(CollectionUtils.isEmpty(permissionTreeWaitCheck)) {
            return Collections.emptyList();
        }
        Assert.notNull(organizationId, BaseConstants.ErrorCode.NOT_NULL);
        if(projectId == null) {
            projectId = PermissionConstants.EMPTY_ID_PLACEHOLDER;
        }
        // 先确保数据展平
        permissionTreeWaitCheck = PermissionTreeCheckVO.treeToList(permissionTreeWaitCheck);
        // 构建鉴权树
        permissionTreeWaitCheck = PermissionTreeCheckVO.listToTree(permissionTreeWaitCheck, rootId, rootTargetBaseType);
        // 通过两个工作空间交替使用的方法实现BFS遍历
        List<PermissionTreeCheckVO> result = new ArrayList<>(permissionTreeWaitCheck.size());
        List<PermissionTreeCheckVO> currentSlot = new ArrayList<>(permissionTreeWaitCheck);
        List<PermissionTreeCheckVO> next;
        while (CollectionUtils.isNotEmpty(currentSlot)) {
            // 鉴权当前工作空间
            for (PermissionTreeCheckVO node : currentSlot) {
                if(PermissionConstants.EMPTY_ID_PLACEHOLDER.equals(node.getId())) {
                    // 特殊处理虚拟根节点, 强制置为无权限, 不然会污染下级节点的鉴权池
                    node.mergePermissionCheckInfo(PermissionCheckVO.generateNonPermission(node.getPermissionCheckInfo()), null);
                } else {
                    // 内部缓存快速鉴权
                    final List<PermissionCheckVO> unCachedCheckInfo = node.checkWithInnerCache();
                    // 未能快速鉴权的部分, 交由鉴权器批量鉴权
                    node.mergePermissionCheckInfo(
                            this.checkPermission(
                                    organizationId,
                                    projectId,
                                    node.getTargetBaseType(),
                                    null,
                                    node.getId(),
                                    unCachedCheckInfo,
                                    false,
                                    true,
                                    false
                            ),
                            this.checkPermission(
                                    organizationId,
                                    projectId,
                                    node.getTargetBaseType(),
                                    null,
                                    node.getId(),
                                    node.getSecurityConfigCheckInfo(),
                                    false,
                                    true,
                                    true
                            )
                    );
                }
            }
            // 准备下级数据
            result.addAll(currentSlot);
            next = currentSlot.stream()
                    .filter(node -> CollectionUtils.isNotEmpty(node.getChildren()))
                    .flatMap(node -> node.getChildren().stream()
                            .peek(child -> child.setParent(node))
                            .peek(PermissionTreeCheckVO::inheritPermissionMap)
                    )
                    .collect(Collectors.toList());
            currentSlot = next;
        }
        // 清理用户信息缓存
        UserInfoVO.clearCurrentUserInfo();
        return result;
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
