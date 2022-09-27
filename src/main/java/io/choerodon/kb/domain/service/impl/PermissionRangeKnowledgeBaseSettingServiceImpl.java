package io.choerodon.kb.domain.service.impl;

import java.util.List;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.kb.api.vo.permission.RoleVO;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.repository.IamRemoteRepository;
import io.choerodon.kb.domain.repository.PermissionRangeKnowledgeBaseSettingRepository;
import io.choerodon.kb.domain.service.PermissionRangeKnowledgeBaseSettingService;
import io.choerodon.kb.infra.common.ChoerodonRole;
import io.choerodon.kb.infra.enums.PermissionConstants;

@Service
public class PermissionRangeKnowledgeBaseSettingServiceImpl implements PermissionRangeKnowledgeBaseSettingService {

    @Autowired
    private IamRemoteRepository iamRemoteRepository;
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

}
