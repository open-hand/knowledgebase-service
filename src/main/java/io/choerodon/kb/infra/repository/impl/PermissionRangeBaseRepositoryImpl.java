package io.choerodon.kb.infra.repository.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.api.vo.permission.CollaboratorVO;
import io.choerodon.kb.api.vo.permission.RoleVO;
import io.choerodon.kb.api.vo.permission.WorkGroupVO;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.entity.UserInfo;
import io.choerodon.kb.domain.repository.IamRemoteRepository;
import io.choerodon.kb.domain.repository.PermissionRangeBaseRepository;
import io.choerodon.kb.infra.enums.PermissionConstants;
import io.choerodon.kb.infra.feign.vo.UserDO;
import io.choerodon.kb.infra.mapper.PermissionRangeMapper;

import org.hzero.mybatis.base.impl.BaseRepositoryImpl;

/**
 * 知识库权限应用范围 资源库基础实现
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
public abstract class PermissionRangeBaseRepositoryImpl extends BaseRepositoryImpl<PermissionRange> implements PermissionRangeBaseRepository {

    @Autowired
    protected IamRemoteRepository iamRemoteRepository;
    @Autowired
    protected PermissionRangeMapper permissionRangeMapper;

    @Override
    public List<PermissionRange> assemblyRangeData(Long organizationId, List<PermissionRange> permissionRanges) {
        // 取出需要组装的数据集
        List<PermissionRange> result = new ArrayList<>();
        Map<String, List<PermissionRange>> rangeTypeGroupMap = permissionRanges.stream().collect(Collectors.groupingBy(PermissionRange::getRangeType));
        for (Map.Entry<String, List<PermissionRange>> rangeTypeGroup : rangeTypeGroupMap.entrySet()) {
            List<PermissionRange> ranges = rangeTypeGroup.getValue();
            Set<Long> collaboratorIds = ranges.stream().map(PermissionRange::getRangeValue).collect(Collectors.toSet());
            switch (PermissionConstants.PermissionRangeType.of(rangeTypeGroup.getKey())) {
                case USER:
                    List<UserDO> userDOS = iamRemoteRepository.listUsersByIds(collaboratorIds, false);
                    Map<Long, UserDO> userDOMap = userDOS.stream().collect(Collectors.toMap(UserDO::getId, Function.identity()));
                    for (PermissionRange range : ranges) {
                        UserDO userDO = userDOMap.get(range.getRangeValue());
                        if (userDO != null) {
                            range.setCollaborator(CollaboratorVO.ofUser(userDO));
                            result.add(range);
                        }
                    }
                    break;
                case ROLE:
                    List<RoleVO> roleVOS = iamRemoteRepository.listRolesByIds(organizationId, collaboratorIds);
//                 TODO 填充聚合信息 eg. 角色下包含的人数
                    Map<Long, RoleVO> roleVOMap = roleVOS.stream().collect(Collectors.toMap(RoleVO::getId, Function.identity()));
                    for (PermissionRange range : ranges) {
                        RoleVO roleVO = roleVOMap.get(range.getRangeValue());
                        if (roleVO != null) {
                            range.setCollaborator(CollaboratorVO.ofRole(roleVO));
                            result.add(range);
                        }
                    }
                    break;
                case WORK_GROUP:
                    List<WorkGroupVO> workGroupVOList = iamRemoteRepository.listWorkGroups(organizationId);
//                 TODO 填充聚合信息 eg. 角色下包含的人数
                    Map<Long, WorkGroupVO> workGroupVOMap = workGroupVOList.stream().collect(Collectors.toMap(WorkGroupVO::getId, Function.identity()));
                    for (PermissionRange range : ranges) {
                        WorkGroupVO workGroupVO = workGroupVOMap.get(range.getRangeValue());
                        if (workGroupVO != null) {
                            range.setCollaborator(CollaboratorVO.ofWorkGroup(workGroupVO));
                            result.add(range);
                        }
                    }
                    break;
                default:
                    result.addAll(ranges);
                    break;
            }
        }
        return result;
    }

    @Override
    public UserInfo queryUserInfo(Long organizationId,
                                  Long projectId) {
        CustomUserDetails customUserDetails = DetailsHelper.getUserDetails();
        UserInfo userInfo = iamRemoteRepository.queryUserInfo(customUserDetails.getUserId(), organizationId, projectId);
        Assert.notNull(userInfo, "error.permission.range.user.not.existed");
        userInfo.setAdminFlag(customUserDetails.getAdmin());
        return userInfo;
    }

}
