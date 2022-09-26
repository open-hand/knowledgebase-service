package io.choerodon.kb.app.service.impl;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.validator.PermissionDetailValidator;
import io.choerodon.kb.api.vo.permission.*;
import io.choerodon.kb.app.service.PermissionRangeService;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.repository.IamRemoteRepository;
import io.choerodon.kb.domain.repository.PermissionRangeRepository;
import io.choerodon.kb.domain.repository.PermissionRangeTenantSettingRepository;
import io.choerodon.kb.infra.common.ChoerodonRole;
import io.choerodon.kb.infra.enums.PermissionConstants;
import io.choerodon.kb.infra.feign.vo.UserDO;

import org.hzero.core.base.BaseAppService;
import org.hzero.core.util.Pair;
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
                // TODO 填充聚合信息 eg. 角色下包含的人数
            }
            switch (PermissionConstants.PermissionTargetType.of(rangeEntry.getKey())) {
                case CREATE_ORG:
                    Optional<PermissionRange> any = groupRanges.stream()
                            .filter(permissionRange -> PermissionConstants.PermissionRangeType.RADIO_RANGES.contains(permissionRange.getRangeType()))
                            .findFirst();
                    organizationPermissionSettingVO.setOrganizationCreateRangeType(any.map(PermissionRange::getRangeType).orElse(PermissionConstants.PermissionRangeType.SPECIFY_RANGE.toString()));
                    organizationPermissionSettingVO.setOrganizationCreateSetting(groupRanges);
                    break;
                case CREATE_PROJECT:
                    any = groupRanges.stream()
                            .filter(permissionRange -> PermissionConstants.PermissionRangeType.RADIO_RANGES.contains(permissionRange.getRangeType()))
                            .findFirst();
                    organizationPermissionSettingVO.setProjectCreateRangeType(any.map(PermissionRange::getRangeType).orElse(PermissionConstants.PermissionRangeType.SPECIFY_RANGE.toString()));
                    organizationPermissionSettingVO.setProjectCreateSetting(groupRanges);
                    break;
                case DEFAULT_ORG:
                    organizationPermissionSettingVO.setOrganizationDefaultPermissionRange(groupRanges);
                    break;
                case DEFAULT_PROJECT:
                    organizationPermissionSettingVO.setProjectDefaultPermissionRange(groupRanges);
                default:
                    break;
            }
        }
        return organizationPermissionSettingVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initOrgPermissionRange(Long organizationId) {
        // 查询组织管理员，项目成员角色，用于默认权限配置
        List<RoleVO> orgRoleVOS = iamRemoteRepository.listRolesOnOrganizationLevel(organizationId, ChoerodonRole.Label.TENANT_ROLE_LABEL, null);
        List<RoleVO> projectRoleVOS = iamRemoteRepository.listRolesOnOrganizationLevel(organizationId, ChoerodonRole.Label.PROJECT_ROLE_LABEL, null);
        orgRoleVOS.addAll(projectRoleVOS);
        List<PermissionRange> defaultRanges = Lists.newArrayList();
        for (RoleVO orgRoleVO : orgRoleVOS) {
            switch (orgRoleVO.getCode()) {
                case ChoerodonRole.RoleCode.TENANT_ADMIN:
                    defaultRanges.add(PermissionRange.of(organizationId, 0L, PermissionConstants.PermissionTargetType.DEFAULT_ORG.toString(), organizationId, PermissionConstants.PermissionRangeType.ROLE.toString(), orgRoleVO.getId(), PermissionConstants.PermissionRole.MANAGER));
                    break;
                case ChoerodonRole.RoleCode.TENANT_MEMBER:
                    defaultRanges.add(PermissionRange.of(organizationId, 0L, PermissionConstants.PermissionTargetType.DEFAULT_ORG.toString(), organizationId, PermissionConstants.PermissionRangeType.ROLE.toString(), orgRoleVO.getId(), PermissionConstants.PermissionRole.EDITOR));
                    break;
                case ChoerodonRole.RoleCode.PROJECT_ADMIN:
                    defaultRanges.add(PermissionRange.of(organizationId, 0L, PermissionConstants.PermissionTargetType.DEFAULT_PROJECT.toString(), organizationId, PermissionConstants.PermissionRangeType.ROLE.toString(), orgRoleVO.getId(), PermissionConstants.PermissionRole.MANAGER));
                    break;
                case ChoerodonRole.RoleCode.PROJECT_MEMBER:
                    defaultRanges.add(PermissionRange.of(organizationId, 0L, PermissionConstants.PermissionTargetType.DEFAULT_PROJECT.toString(), organizationId, PermissionConstants.PermissionRangeType.ROLE.toString(), orgRoleVO.getId(), PermissionConstants.PermissionRole.EDITOR));
                    break;
                default:
                    break;
            }
        }
        permissionRangeTenantSettingRepository.initSetting(organizationId, defaultRanges);
    }

    /**
     * 组装权限范围数据
     *
     * @param organizationId   租户id
     * @param permissionRanges 需要组装的权限范围数据
     */
    private void assemblyRangeData(Long organizationId, List<PermissionRange> permissionRanges) {
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
    @Transactional(rollbackFor = Exception.class)
    public PermissionDetailVO save(Long organizationId,
                                   Long projectId,
                                   PermissionDetailVO permissionDetailVO) {
        SecurityTokenHelper.validToken(permissionDetailVO);
        PermissionDetailValidator.validate(permissionDetailVO);
        savePermissionRange(organizationId, projectId, permissionDetailVO);
        saveSecurityConfig(organizationId, projectId, permissionDetailVO);
        return permissionDetailVO;
    }

    private void saveSecurityConfig(Long organizationId,
                                    Long projectId,
                                    PermissionDetailVO permissionDetailVO) {
    }

    private void savePermissionRange(Long organizationId,
                                     Long projectId,
                                     PermissionDetailVO permissionDetailVO) {
        String targetType = permissionDetailVO.getTargetType();
        Long targetValue = permissionDetailVO.getTargetValue();
        if (projectId == null) {
            projectId = 0L;
        }
        List<PermissionRange> permissionRanges = permissionDetailVO.getPermissionRanges();
        if (permissionRanges == null) {
            permissionRanges = Collections.emptyList();
        }
        Pair<List<PermissionRange>, List<PermissionRange>> pair =
                processAddAndDeleteList(organizationId, projectId, targetType, targetValue, permissionRanges);
        List<PermissionRange> addList = pair.getFirst();
        List<PermissionRange> deleteList = pair.getSecond();

        for (PermissionRange permissionRange : addList) {
            if (permissionRangeRepository.insert(permissionRange) != 1) {
                throw new CommonException("error.permission.range.insert");
            }
        }
        if (!deleteList.isEmpty()) {
            Set<Long> ids = deleteList.stream().map(PermissionRange::getId).collect(Collectors.toSet());
            permissionRangeRepository.deleteByIds(ids);
        }
    }

    private Pair<List<PermissionRange>, List<PermissionRange>> processAddAndDeleteList(Long organizationId,
                                                                                       Long projectId,
                                                                                       String targetType,
                                                                                       Long targetValue,
                                                                                       List<PermissionRange> permissionRanges) {
        List<PermissionRange> addList = new ArrayList<>();
        List<PermissionRange> deleteList = new ArrayList<>();
        PermissionRange example =
                PermissionRange.of(
                        organizationId,
                        projectId,
                        targetType,
                        targetValue,
                        null,
                        null,
                        null);
        List<PermissionRange> existedList = permissionRangeRepository.select(example);
        //交集
        List<PermissionRange> intersection = new ArrayList<>();
        for (PermissionRange permissionRange : permissionRanges) {
            for (PermissionRange existedPermissionRange : existedList) {
                if (permissionRange.equals(existedPermissionRange)) {
                    intersection.add(existedPermissionRange);
                }
            }
        }
        for (PermissionRange permissionRange : permissionRanges) {
            if (!intersection.contains(permissionRange)) {
                permissionRange.setOrganizationId(organizationId);
                permissionRange.setProjectId(projectId);
                addList.add(permissionRange);
            }
        }
        for (PermissionRange permissionRange : existedList) {
            if (!intersection.contains(permissionRange)) {
                deleteList.add(permissionRange);
            }
        }
        return Pair.of(addList, deleteList);
    }

}
