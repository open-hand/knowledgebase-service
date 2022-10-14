package io.choerodon.kb.infra.permission.checker;

import java.util.*;
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
            Collection<PermissionCheckVO> permissionWaitCheck
    ) {
        Assert.notNull(userDetails, BaseConstants.ErrorCode.NOT_NULL);
        Assert.notNull(organizationId, BaseConstants.ErrorCode.NOT_NULL);
        Assert.notNull(projectId, BaseConstants.ErrorCode.NOT_NULL);
        Assert.hasText(targetType, BaseConstants.ErrorCode.NOT_NULL);
        Assert.notNull(targetValue, BaseConstants.ErrorCode.NOT_NULL);
        if(CollectionUtils.isEmpty(permissionWaitCheck)) {
            return Collections.emptyList();
        }
        //
        List<ImmutableTriple<Long, String, String>> parentInfos = this.workSpaceRepository.findParentInfoWithCache(targetValue);

        List<Pair<String, Long>> checkTargetList = new ArrayList<>();
        //
        if(CollectionUtils.isNotEmpty(parentInfos)) {
            checkTargetList.addAll(parentInfos.stream().map(triple -> Pair.of(triple.getMiddle(), triple.getLeft())).collect(Collectors.toList()));
        } else {
            checkTargetList.add(Pair.of(targetType, targetValue));
        }
        return checkTargetList.stream()
                .map(checkTarget -> this.checkOneTargetPermission(
                        userDetails,
                        organizationId,
                        projectId,
                        checkTarget.getFirst(),
                        checkTarget.getSecond(),
                        permissionWaitCheck)
                )
                .collect(PermissionCheckVO.permissionCombiner)
                .stream().peek(checkInfo -> {
                    if(checkInfo.getControllerType() == null) {
                        checkInfo.setControllerType(PermissionConstants.PermissionRole.NULL);
                    }
                }).collect(Collectors.toList());
    }

    abstract protected List<PermissionCheckVO> checkOneTargetPermission(
            @Nonnull CustomUserDetails userDetails,
            @Nonnull Long organizationId,
            @Nonnull Long projectId,
            @Nonnull String targetType,
            @Nonnull Long targetValue,
            Collection<PermissionCheckVO> permissionWaitCheck
    );

    /**
     * 生成无权限返回值
     * @param permissionWaitCheck   权限校验信息
     * @return                      无权限返回值
     */
    protected List<PermissionCheckVO> generateNonPermission(Collection<PermissionCheckVO> permissionWaitCheck) {
        return Optional.ofNullable(permissionWaitCheck)
                .orElse(Collections.emptyList())
                .stream()
                .map(checkInfo -> checkInfo.setApprove(Boolean.FALSE).setControllerType(PermissionConstants.PermissionRole.NULL))
                .collect(Collectors.toList());
    }

}