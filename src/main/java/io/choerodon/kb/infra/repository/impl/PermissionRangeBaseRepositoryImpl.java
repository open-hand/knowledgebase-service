package io.choerodon.kb.infra.repository.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import io.choerodon.kb.api.vo.permission.Collaborator;
import io.choerodon.kb.api.vo.permission.RoleVO;
import io.choerodon.kb.api.vo.permission.WorkGroupVO;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.repository.IamRemoteRepository;
import io.choerodon.kb.domain.repository.PermissionRangeBaseRepository;
import io.choerodon.kb.infra.enums.PermissionConstants;
import io.choerodon.kb.infra.feign.vo.UserDO;
import io.choerodon.kb.infra.mapper.PermissionRangeMapper;

import org.hzero.mybatis.base.impl.BaseRepositoryImpl;

/**
 * 知识库权限应用范围 资源库实现
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
@Repository
public class PermissionRangeBaseRepositoryImpl extends BaseRepositoryImpl<PermissionRange> implements PermissionRangeBaseRepository {

    @Autowired
    protected PermissionRangeMapper permissionRangeMapper;
    @Autowired
    protected IamRemoteRepository iamRemoteRepository;

    @Override
    public void assemblyRangeData(Long organizationId, List<PermissionRange> permissionRanges) {
        // 取出需要组装的数据集
        Map<String, List<PermissionRange>> rangeTypeGroupMap = permissionRanges.stream().collect(Collectors.groupingBy(PermissionRange::getRangeType));
        for (Map.Entry<String, List<PermissionRange>> rangeTypeGroup : rangeTypeGroupMap.entrySet()) {
            List<PermissionRange> ranges = rangeTypeGroup.getValue();
            Set<Long> collaboratorIds = ranges.stream().map(PermissionRange::getRangeValue).collect(Collectors.toSet());
            switch (PermissionConstants.PermissionRangeType.of(rangeTypeGroup.getKey())) {
                case USER:
                    List<UserDO> userDOS = iamRemoteRepository.listUsersByIds(collaboratorIds, false);
                    Map<Long, UserDO> userDOMap = userDOS.stream().collect(Collectors.toMap(UserDO::getId, Function.identity()));
                    for (PermissionRange range : ranges) {
                        range.setCollaborator(Collaborator.ofUser(userDOMap.get(range.getRangeValue())));
                    }
                    break;
                case ROLE:
                    List<RoleVO> roleVOS = iamRemoteRepository.listRolesByIds(organizationId, collaboratorIds);
                    Map<Long, RoleVO> roleVOMap = roleVOS.stream().collect(Collectors.toMap(RoleVO::getId, Function.identity()));
                    for (PermissionRange range : ranges) {
                        range.setCollaborator(Collaborator.ofRole(roleVOMap.get(range.getRangeValue())));
                    }
                    break;
                case WORK_GROUP:
                    List<WorkGroupVO> workGroupVOList = iamRemoteRepository.listWorkGroups(organizationId);
                    Map<Long, WorkGroupVO> workGroupVOMap = workGroupVOList.stream().collect(Collectors.toMap(WorkGroupVO::getId, Function.identity()));
                    for (PermissionRange range : ranges) {
                        range.setCollaborator(Collaborator.ofWorkGroup(workGroupVOMap.get(range.getRangeValue())));
                    }
                    break;
                default:
                    break;
            }
        }

    }

    @Override
    public void deleteByIds(Set<Long> ids) {
        if(ObjectUtils.isEmpty(ids)) {
            return;
        }
        permissionRangeMapper.deleteByIds(ids);
    }
}
