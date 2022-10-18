package io.choerodon.kb.infra.permission.checker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import org.apache.commons.collections4.CollectionUtils;
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
 * 知识库对象鉴权器基础实现
 * @author gaokuo.dai@zknow.com 2022-10-18
 */
public abstract class BasePermissionChecker implements PermissionChecker {

    @Autowired
    protected WorkSpaceRepository workSpaceRepository;

    @Override
    public List<PermissionCheckVO> checkPermission(
            @Nonnull CustomUserDetails userDetails,
            @Nonnull Long organizationId,
            @Nonnull Long projectId,
            @Nonnull String targetType,
            @Nonnull Long targetValue,
            Collection<PermissionCheckVO> permissionWaitCheck,
            boolean checkWithParent
    ) {
        // 基础校验
        Assert.notNull(userDetails, BaseConstants.ErrorCode.NOT_NULL);
        Assert.notNull(organizationId, BaseConstants.ErrorCode.NOT_NULL);
        Assert.notNull(projectId, BaseConstants.ErrorCode.NOT_NULL);
        Assert.hasText(targetType, BaseConstants.ErrorCode.NOT_NULL);
        Assert.notNull(targetValue, BaseConstants.ErrorCode.NOT_NULL);
        if(CollectionUtils.isEmpty(permissionWaitCheck)) {
            return Collections.emptyList();
        }
        // 获取父级信息
        List<ImmutableTriple<Long, String, String>> parentInfos = checkWithParent ? this.workSpaceRepository.findParentInfoWithCache(targetValue): null;
        List<Pair<String, Long>> checkTargetList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(parentInfos)) {
            checkTargetList.addAll(parentInfos.stream().map(triple -> Pair.of(triple.getMiddle(), triple.getLeft())).collect(Collectors.toList()));
        } else {
            checkTargetList.add(Pair.of(targetType, targetValue));
        }
        // 鉴权
        return checkTargetList.stream()
                // 遍历处理当前层级和所有已知父级
                .map(checkTarget -> this.checkOneTargetPermission(
                        userDetails,
                        organizationId,
                        projectId,
                        checkTarget.getFirst(),
                        checkTarget.getSecond(),
                        permissionWaitCheck)
                )
                // 合并权限
                .collect(PermissionCheckVO.permissionCombiner)
                .stream().peek(checkInfo -> {
                    if(checkInfo.getControllerType() == null) {
                        checkInfo.setControllerType(PermissionConstants.PermissionRole.NULL);
                    }
                }).collect(Collectors.toList());
    }

    /**
     * 知识库对象鉴权--仅单一对象, 不处理父级
     * @param userDetails           当前用户信息
     * @param organizationId        组织ID
     * @param projectId             项目ID
     * @param targetType            对象控制类
     * @param targetValue           对象ID
     * @param permissionWaitCheck   待鉴定权限
     * @return                      鉴权结果
     */
    abstract protected List<PermissionCheckVO> checkOneTargetPermission(
            @Nonnull CustomUserDetails userDetails,
            @Nonnull Long organizationId,
            @Nonnull Long projectId,
            @Nonnull String targetType,
            @Nonnull Long targetValue,
            Collection<PermissionCheckVO> permissionWaitCheck
    );

}
