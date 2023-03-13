package io.choerodon.kb.infra.permission.Voter;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import javax.annotation.Nonnull;

import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.kb.api.vo.permission.PermissionCheckVO;

/**
 * 知识库对象鉴权投票器
 * @author gaokuo.dai@zknow.com 2022-10-18
 */
public interface PermissionVoter {

    /**
     * 知识库对象鉴权投票
     * @param userDetails           当前用户信息
     * @param organizationId        组织ID
     * @param projectId             项目ID
     * @param targetType            对象控制类型
     * @param targetValue           对象ID
     * @param permissionWaitCheck   待鉴定权限
     * @param checkWithParent       是否处理父级权限
     * @return                      鉴权结果
     */
    List<PermissionCheckVO> votePermission(
            @Nonnull CustomUserDetails userDetails,
            @Nonnull Long organizationId,
            @Nonnull Long projectId,
            @Nonnull String targetType,
            @Nonnull Long targetValue,
            Collection<PermissionCheckVO> permissionWaitCheck,
            boolean checkWithParent
    );

    /**
     * @return 鉴权投票器适用的控制对象类型Code
     */
    @Nonnull
    Set<String> applicabilityTargetType();

    /**
     * @return 是否只校验自己, 不校验父级
     */
    boolean onlyVoteSelf();

    /**
     * @return 是否必须登录才可使用此投票器
     */
    boolean needLogin();

    /**
     * @return 归票规则
     */
    @Nonnull
    Collector<List<PermissionCheckVO>, List<PermissionCheckVO>, List<PermissionCheckVO>> ticketCollectionRule();

}
