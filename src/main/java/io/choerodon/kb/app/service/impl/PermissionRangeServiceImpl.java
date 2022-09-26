package io.choerodon.kb.app.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.kb.api.vo.permission.Collaborator;
import io.choerodon.kb.api.vo.permission.OrganizationPermissionSettingVO;
import io.choerodon.kb.api.vo.permission.RoleVO;
import io.choerodon.kb.api.vo.permission.WorkGroupVO;
import io.choerodon.kb.app.service.PermissionRangeService;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.repository.IamRemoteRepository;
import io.choerodon.kb.domain.repository.PermissionRangeRepository;
import io.choerodon.kb.domain.repository.PermissionRangeTenantSettingRepository;
import io.choerodon.kb.infra.enums.PermissionRangeTargetType;
import io.choerodon.kb.infra.enums.PermissionRangeType;
import io.choerodon.kb.infra.feign.vo.UserDO;

import org.hzero.core.base.BaseAppService;
import org.hzero.mybatis.helper.SecurityTokenHelper;

/**
 * 知识库权限应用范围应用服务默认实现
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
@Service
public class PermissionRangeServiceImpl extends BaseAppService implements PermissionRangeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionRangeServiceImpl.class);

    @Autowired
    private IamRemoteRepository iamRemoteRepository;
    @Autowired
    private PermissionRangeRepository permissionRangeRepository;
    @Autowired
    private PermissionRangeTenantSettingRepository permissionRangeTenantSettingRepository;

    @Override
    public PermissionRange create(Long tenantId, PermissionRange permissionRange) {
        validObject(permissionRange);
        permissionRangeRepository.insertSelective(permissionRange);
        return permissionRange;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionRange update(Long tenantId, PermissionRange permissionRange) {
        SecurityTokenHelper.validToken(permissionRange);
        permissionRangeRepository.updateByPrimaryKeySelective(permissionRange);
        return permissionRange;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(PermissionRange permissionRange) {
        SecurityTokenHelper.validToken(permissionRange);
        permissionRangeRepository.deleteByPrimaryKey(permissionRange);
    }

    @Override
    public OrganizationPermissionSettingVO queryOrgPermissionSetting(Long organizationId) {
        OrganizationPermissionSettingVO organizationPermissionSettingVO = new OrganizationPermissionSettingVO();
        List<PermissionRange> permissionRanges = permissionRangeTenantSettingRepository.selectOrgSetting(organizationId);
        // 组装常规数据, (user, role, work_group)
        assemblyRangeData(organizationId, permissionRanges);
        // 根据项目和组织进行分组，如果只有一个则为单角色，如果有多个则为选择范围, 设置到不同的属性
        Map<String, List<PermissionRange>> targetMap = permissionRanges.stream().collect(Collectors.groupingBy(PermissionRange::getTargetType));
        for (Map.Entry<String, List<PermissionRange>> rangeEntry : targetMap.entrySet()) {
            List<PermissionRange> groupRanges = rangeEntry.getValue();
            for (PermissionRange groupRange : groupRanges) {
                // 填充信息
            }
            switch (PermissionRangeTargetType.of(rangeEntry.getKey())) {
                case KNOWLEDGE_CREATE_ORG:
                    organizationPermissionSettingVO.setOrganizationCreateSetting(groupRanges);
                    break;
                case KNOWLEDGE_CREATE_PROJECT:
                    organizationPermissionSettingVO.setProjectCreateSetting(groupRanges);
                    break;
                case KNOWLEDGE_DEFAULT_ORG:
                    organizationPermissionSettingVO.setOrganizationDefaultPermissionRange(groupRanges);
                    break;
                case KNOWLEDGE_DEFAULT_PROJECT:
                    organizationPermissionSettingVO.setProjectDefaultPermissionRange(groupRanges);
                default:
                    break;
            }
        }
        return organizationPermissionSettingVO;
    }

    @Override
    public void initOrgPermissionRange(Long organizationId) {
        // 查询组织管理员，项目成员角色，用于默认权限配置
        List<RoleVO> orgRoleVOS = iamRemoteRepository.listRolesOnOrganizationLevel(organizationId, "TENANT_ROLE", null);
        List<RoleVO> projectRoleVOS = iamRemoteRepository.listRolesOnOrganizationLevel(organizationId, "PROJECT_ROLE", null);
//        permissionRangeTenantSettingRepository.initSetting(organizationId);
    }


    private void assemblyRangeData(Long organizationId, List<PermissionRange> permissionRanges) {
        // 取出需要组装的数据集
        Map<String, List<PermissionRange>> rangeTypeGroupMap = permissionRanges.stream().collect(Collectors.groupingBy(PermissionRange::getRangeType));
        for (Map.Entry<String, List<PermissionRange>> rangeTypeGroup : rangeTypeGroupMap.entrySet()) {
            List<PermissionRange> ranges = rangeTypeGroup.getValue();
            Set<Long> collaboratorIds = ranges.stream().map(PermissionRange::getRangeValue).collect(Collectors.toSet());
            switch (PermissionRangeType.of(rangeTypeGroup.getKey())) {
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
}
