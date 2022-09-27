package io.choerodon.kb.app.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.validator.PermissionDetailValidator;
import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.choerodon.kb.api.vo.permission.RoleVO;
import io.choerodon.kb.app.service.PermissionRangeService;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.repository.IamRemoteRepository;
import io.choerodon.kb.domain.repository.PermissionRangeBaseRepository;
import io.choerodon.kb.domain.repository.PermissionRangeKnowledgeBaseSettingRepository;
import io.choerodon.kb.infra.common.ChoerodonRole;
import io.choerodon.kb.infra.enums.PermissionConstants;

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

    @Autowired
    private IamRemoteRepository iamRemoteRepository;
    @Autowired
    private PermissionRangeBaseRepository permissionRangeBaseRepository;
    @Autowired
    private PermissionRangeKnowledgeBaseSettingRepository permissionRangeKnowledgeBaseSettingRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initPermissionRangeOnOrganizationCreate(Long organizationId) {
        // 查询组织管理员，项目成员角色，用于默认权限配置
        List<RoleVO> orgRoleVOS = iamRemoteRepository.listRolesOnOrganizationLevel(organizationId, ChoerodonRole.Label.TENANT_ROLE_LABEL, null);
        List<RoleVO> projectRoleVOS = iamRemoteRepository.listRolesOnOrganizationLevel(organizationId, ChoerodonRole.Label.PROJECT_ROLE_LABEL, null);
        orgRoleVOS.addAll(projectRoleVOS);
        List<PermissionRange> defaultRanges = Lists.newArrayList();
        for (RoleVO orgRoleVO : orgRoleVOS) {
            switch (orgRoleVO.getCode()) {
                case ChoerodonRole.RoleCode.TENANT_ADMIN:
                    defaultRanges.add(PermissionRange.of(organizationId, 0L, PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_DEFAULT_ORG.toString(), organizationId, PermissionConstants.PermissionRangeType.ROLE.toString(), orgRoleVO.getId(), PermissionConstants.PermissionRole.MANAGER));
                    break;
                case ChoerodonRole.RoleCode.TENANT_MEMBER:
                    defaultRanges.add(PermissionRange.of(organizationId, 0L, PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_DEFAULT_ORG.toString(), organizationId, PermissionConstants.PermissionRangeType.ROLE.toString(), orgRoleVO.getId(), PermissionConstants.PermissionRole.EDITOR));
                    break;
                case ChoerodonRole.RoleCode.PROJECT_ADMIN:
                    defaultRanges.add(PermissionRange.of(organizationId, 0L, PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_DEFAULT_PROJECT.toString(), organizationId, PermissionConstants.PermissionRangeType.ROLE.toString(), orgRoleVO.getId(), PermissionConstants.PermissionRole.MANAGER));
                    break;
                case ChoerodonRole.RoleCode.PROJECT_MEMBER:
                    defaultRanges.add(PermissionRange.of(organizationId, 0L, PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_DEFAULT_PROJECT.toString(), organizationId, PermissionConstants.PermissionRangeType.ROLE.toString(), orgRoleVO.getId(), PermissionConstants.PermissionRole.EDITOR));
                    break;
                default:
                    break;
            }
        }
        permissionRangeKnowledgeBaseSettingRepository.initOrganizationPermissionRangeKnowledgeBaseSetting(organizationId, defaultRanges);
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
            if (permissionRangeBaseRepository.insert(permissionRange) != 1) {
                throw new CommonException("error.permission.range.insert");
            }
        }
        if (!deleteList.isEmpty()) {
            Set<Long> ids = deleteList.stream().map(PermissionRange::getId).collect(Collectors.toSet());
            permissionRangeBaseRepository.deleteByIds(ids);
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
        List<PermissionRange> existedList = permissionRangeBaseRepository.select(example);
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
