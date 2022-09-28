package io.choerodon.kb.domain.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.api.validator.PermissionDetailValidator;
import io.choerodon.kb.api.vo.permission.OrganizationPermissionSettingVO;
import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.choerodon.kb.api.vo.permission.RoleVO;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.repository.IamRemoteRepository;
import io.choerodon.kb.domain.repository.PermissionRangeKnowledgeBaseSettingRepository;
import io.choerodon.kb.domain.service.PermissionRangeKnowledgeBaseSettingService;
import io.choerodon.kb.infra.common.ChoerodonRole;
import io.choerodon.kb.infra.enums.PermissionConstants;

import org.hzero.core.base.BaseConstants;

/**
 * 权限范围知识库配置 领域Service实现类
 * @author gaokuo.dai@zknow.com 2022-09-27
 */
@Service
public class PermissionRangeKnowledgeBaseSettingServiceImpl extends PermissionRangeBaseDomainServiceImpl implements PermissionRangeKnowledgeBaseSettingService {

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
                    defaultRanges.add(PermissionRange.of(
                            organizationId,
                            PermissionConstants.EMPTY_ID_PLACEHOLDER,
                            PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_DEFAULT_ORG.toString(),
                            organizationId,
                            PermissionConstants.PermissionRangeType.ROLE.toString(),
                            orgRoleVO.getId(),
                            PermissionConstants.PermissionRole.MANAGER
                    ));
                    break;
                case ChoerodonRole.RoleCode.TENANT_MEMBER:
                    defaultRanges.add(PermissionRange.of(
                            organizationId,
                            PermissionConstants.EMPTY_ID_PLACEHOLDER,
                            PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_DEFAULT_ORG.toString(),
                            organizationId,
                            PermissionConstants.PermissionRangeType.ROLE.toString(),
                            orgRoleVO.getId(),
                            PermissionConstants.PermissionRole.EDITOR
                    ));
                    break;
                case ChoerodonRole.RoleCode.PROJECT_ADMIN:
                    defaultRanges.add(PermissionRange.of(
                            organizationId,
                            PermissionConstants.EMPTY_ID_PLACEHOLDER,
                            PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_DEFAULT_PROJECT.toString(),
                            organizationId,
                            PermissionConstants.PermissionRangeType.ROLE.toString(),
                            orgRoleVO.getId(),
                            PermissionConstants.PermissionRole.MANAGER
                    ));
                    break;
                case ChoerodonRole.RoleCode.PROJECT_MEMBER:
                    defaultRanges.add(PermissionRange.of(
                            organizationId,
                            PermissionConstants.EMPTY_ID_PLACEHOLDER,
                            PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_DEFAULT_PROJECT.toString(),
                            organizationId,
                            PermissionConstants.PermissionRangeType.ROLE.toString(),
                            orgRoleVO.getId(),
                            PermissionConstants.PermissionRole.EDITOR
                    ));
                    break;
                default:
                    break;
            }
        }
        permissionRangeKnowledgeBaseSettingRepository.initOrganizationPermissionRangeKnowledgeBaseSetting(organizationId, defaultRanges);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(Long organizationId, OrganizationPermissionSettingVO organizationPermissionSetting) {
        Assert.notNull(organizationPermissionSetting, BaseConstants.ErrorCode.NOT_NULL);
        this.saveKnowledgeBaseCreatePermissionRange(ResourceLevel.ORGANIZATION, organizationId, organizationPermissionSetting);
        this.saveKnowledgeBaseCreatePermissionRange(ResourceLevel.PROJECT, organizationId, organizationPermissionSetting);
        this.saveKnowledgeBaseDefaultPermissionRange(ResourceLevel.ORGANIZATION, organizationId, organizationPermissionSetting);
        this.saveKnowledgeBaseDefaultPermissionRange(ResourceLevel.PROJECT, organizationId, organizationPermissionSetting);
    }

    private void saveKnowledgeBaseCreatePermissionRange(ResourceLevel resourceLevel, Long organizationId, OrganizationPermissionSettingVO organizationPermissionSetting) {
        Assert.notNull(resourceLevel, BaseConstants.ErrorCode.NOT_NULL);
        Assert.isTrue(ResourceLevel.ORGANIZATION.equals(resourceLevel) || ResourceLevel.PROJECT.equals(resourceLevel), BaseConstants.ErrorCode.DATA_INVALID);
        Assert.notNull(organizationPermissionSetting, BaseConstants.ErrorCode.NOT_NULL);

        final PermissionConstants.PermissionRangeType permissionRangeType = PermissionConstants.PermissionRangeType.of(
                ResourceLevel.ORGANIZATION.equals(resourceLevel) ?
                    organizationPermissionSetting.getOrganizationCreateRangeType() :
                    organizationPermissionSetting.getProjectCreateRangeType()
        );
        List<PermissionRange> permissionRanges = Optional.ofNullable(
                    ResourceLevel.ORGANIZATION.equals(resourceLevel) ?
                        organizationPermissionSetting.getOrganizationCreateSetting() :
                        organizationPermissionSetting.getProjectCreateSetting()
                ).orElse(Collections.emptyList())
                .stream()
                .map(PermissionRange::processTargetValueDecrypt)
                .collect(Collectors.toList());
        final String targetType = ResourceLevel.ORGANIZATION.equals(resourceLevel) ?
                PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_CREATE_ORG.getCode() :
                PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_CREATE_PROJECT.getCode();

        switch ((permissionRangeType)) {
            case MANAGER:
                permissionRanges = Collections.singletonList(PermissionRange.of(
                        organizationId,
                        PermissionConstants.EMPTY_ID_PLACEHOLDER,
                        targetType,
                        PermissionConstants.EMPTY_ID_PLACEHOLDER,
                        PermissionConstants.PermissionRangeType.MANAGER.toString(),
                        PermissionConstants.EMPTY_ID_PLACEHOLDER,
                        PermissionConstants.PermissionRole.NULL));
                break;
            case MEMBER:
                permissionRanges = Collections.singletonList(PermissionRange.of(
                        organizationId,
                        PermissionConstants.EMPTY_ID_PLACEHOLDER,
                        targetType,
                        PermissionConstants.EMPTY_ID_PLACEHOLDER,
                        PermissionConstants.PermissionRangeType.MEMBER.toString(),
                        PermissionConstants.EMPTY_ID_PLACEHOLDER,
                        PermissionConstants.PermissionRole.NULL));
                break;
            case SPECIFY_RANGE:
                permissionRanges = permissionRanges.stream().map(permissionRange ->
                        permissionRange.setOrganizationId(organizationId)
                                .setProjectId(PermissionConstants.EMPTY_ID_PLACEHOLDER)
                                .setTargetType(targetType)
                                .setTargetValue(PermissionConstants.EMPTY_ID_PLACEHOLDER)
                ).collect(Collectors.toList());
                break;
            default:
                throw new CommonException(BaseConstants.ErrorCode.DATA_INVALID);
        }

        final PermissionDetailVO permissionDetail = PermissionDetailVO.of(targetType, PermissionConstants.EMPTY_ID_PLACEHOLDER, permissionRanges);
        PermissionDetailValidator.validate(
                permissionDetail,
                PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_SETTING_TARGET_TYPES,
                PermissionConstants.PermissionRangeType.KNOWLEDGE_BASE_SETTING_RANGE_TYPES,
                PermissionConstants.PermissionRole.ALL_CODES
        );
        this.commonSave(
                organizationId,
                PermissionConstants.EMPTY_ID_PLACEHOLDER,
                permissionDetail
        );
    }


    private void saveKnowledgeBaseDefaultPermissionRange(ResourceLevel resourceLevel, Long organizationId, OrganizationPermissionSettingVO organizationPermissionSetting) {
        Assert.notNull(resourceLevel, BaseConstants.ErrorCode.NOT_NULL);
        Assert.isTrue(ResourceLevel.ORGANIZATION.equals(resourceLevel) || ResourceLevel.PROJECT.equals(resourceLevel), BaseConstants.ErrorCode.DATA_INVALID);
        Assert.notNull(organizationPermissionSetting, BaseConstants.ErrorCode.NOT_NULL);
        Assert.notNull(organizationPermissionSetting, BaseConstants.ErrorCode.NOT_NULL);

        final String targetType = ResourceLevel.ORGANIZATION.equals(resourceLevel) ?
                PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_DEFAULT_ORG.getCode() :
                PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_DEFAULT_PROJECT.getCode();
        final List<PermissionRange> permissionRanges = Optional.ofNullable(
                        ResourceLevel.ORGANIZATION.equals(resourceLevel) ?
                                organizationPermissionSetting.getOrganizationDefaultPermissionRange() :
                                organizationPermissionSetting.getProjectDefaultPermissionRange()
                ).orElse(Collections.emptyList())
                .stream()
                .map(PermissionRange::processTargetValueDecrypt)
                .map(permissionRange ->
                        permissionRange.setOrganizationId(organizationId)
                                .setProjectId(PermissionConstants.EMPTY_ID_PLACEHOLDER)
                                .setTargetType(targetType)
                                .setTargetValue(PermissionConstants.EMPTY_ID_PLACEHOLDER)
                )
                .collect(Collectors.toList());

        final PermissionDetailVO permissionDetail = PermissionDetailVO.of(targetType, PermissionConstants.EMPTY_ID_PLACEHOLDER, permissionRanges);
        PermissionDetailValidator.validate(
                permissionDetail,
                PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_SETTING_TARGET_TYPES,
                PermissionConstants.PermissionRangeType.OBJECT_SETTING_RANGE_TYPES,
                PermissionConstants.PermissionRole.OBJECT_SETTING_ROLE_CODES
        );
        this.commonSave(
                organizationId,
                PermissionConstants.EMPTY_ID_PLACEHOLDER,
                permissionDetail
        );
    }

}
