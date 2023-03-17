package io.choerodon.kb.infra.permission.Voter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.kb.api.vo.permission.PermissionCheckVO;
import io.choerodon.kb.domain.repository.WorkSpaceRepository;
import io.choerodon.kb.infra.enums.PermissionConstants;

import org.hzero.core.base.BaseConstants;
import org.hzero.core.util.Pair;

/**
 * 知识库对象鉴权投票器基础实现
 * @author gaokuo.dai@zknow.com 2022-10-18
 */
public abstract class BasePermissionVoter implements PermissionVoter {

    @Autowired
    protected WorkSpaceRepository workSpaceRepository;

    @Override
    public List<PermissionCheckVO> votePermission(
            @Nonnull CustomUserDetails userDetails,
            @Nonnull Long organizationId,
            @Nonnull Long projectId,
            @Nonnull String targetType,
            @Nonnull Long targetValue,
            Collection<PermissionCheckVO> permissionWaitCheck,
            boolean checkWithParent
    ) {
        // 基础校验
        if(this.needLogin() && userDetails == null) {
            return PermissionCheckVO.generateNonPermission(permissionWaitCheck);
        }
        Assert.notNull(organizationId, BaseConstants.ErrorCode.NOT_NULL);
        Assert.notNull(projectId, BaseConstants.ErrorCode.NOT_NULL);
        Assert.hasText(targetType, BaseConstants.ErrorCode.NOT_NULL);
        Assert.notNull(targetValue, BaseConstants.ErrorCode.NOT_NULL);
        if(CollectionUtils.isEmpty(permissionWaitCheck)) {
            return Collections.emptyList();
        }
        // 克隆一下鉴权对象, 避免各个投票器交叉污染
        final List<PermissionCheckVO> finalPermissionWaitCheck = permissionWaitCheck.stream().map(PermissionCheckVO::clone).collect(Collectors.toList());

        final Pair<String, Long> selfCheckTarget = Pair.of(targetType, targetValue);
        final List<Pair<String, Long>> checkTargetList = new ArrayList<>();

        if(!this.onlyVoteSelf() && !this.voteForKnowledgeBase(targetType)) {
            // 如果需要处理父级信息
            // 注意, 知识库没有父级, 在父子缓存里也没有, 硬从缓存里找会找到错误的数据, 所以也需要跳过
            // 获取父级信息
            List<ImmutableTriple<Long, String, String>> parentInfos = checkWithParent ? this.workSpaceRepository.findParentInfoWithCache(targetValue): null;
            if(CollectionUtils.isNotEmpty(parentInfos)) {
                // 存在父级信息, 全部加入待鉴权对象列表
                checkTargetList.addAll(parentInfos.stream().map(triple -> Pair.of(triple.getMiddle(), triple.getLeft())).collect(Collectors.toList()));
            } else {
                // 不存在父级信息, 只鉴权自身
                checkTargetList.add(selfCheckTarget);
            }
        } else {
            // 不需要处理父级信息, 只鉴权自身
            checkTargetList.add(selfCheckTarget);
        }

        // 鉴权投票
        return checkTargetList.stream()
                // 遍历处理当前层级和所有已知父级
                .map(checkTarget -> this.voteOneTargetPermission(
                        userDetails,
                        organizationId,
                        projectId,
                        checkTarget.getFirst(),
                        checkTarget.getSecond(),
                        finalPermissionWaitCheck)
                )
                // 合并权限投票
                .collect(this.ticketCollectionRule())
                .stream().peek(checkInfo -> {
                    if(checkInfo.getControllerType() == null) {
                        checkInfo.setControllerType(PermissionConstants.PermissionRole.NULL);
                    }
                }).collect(Collectors.toList());
    }

    /**
     * 知识库对象鉴权投票--仅单一对象, 不处理父级
     * @param userDetails           当前用户信息
     * @param organizationId        组织ID
     * @param projectId             项目ID
     * @param targetType            对象控制类
     * @param targetValue           对象ID
     * @param permissionWaitCheck   待鉴定权限
     * @return                      鉴权结果
     */
    abstract protected List<PermissionCheckVO> voteOneTargetPermission(
            @Nonnull CustomUserDetails userDetails,
            @Nonnull Long organizationId,
            @Nonnull Long projectId,
            @Nonnull String targetType,
            @Nonnull Long targetValue,
            Collection<PermissionCheckVO> permissionWaitCheck
    );

    /**
     * 是否为知识库自身的鉴权投票, 知识库不在父子关系缓存里所以需要单独处理
     * @param targetType    targetType
     * @return              结果
     */
    private boolean voteForKnowledgeBase(String targetType) {
        if(StringUtils.isBlank(targetType)) {
            return false;
        }
        return PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_SETTING_TARGET_TYPES.contains(targetType)
                || PermissionConstants.PermissionTargetType.KB_TARGET_TYPES.contains(targetType);
    }

}
