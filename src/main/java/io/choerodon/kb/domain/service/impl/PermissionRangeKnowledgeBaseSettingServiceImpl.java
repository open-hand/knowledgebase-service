package io.choerodon.kb.domain.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.ListUtils;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initPermissionRangeOnOrganizationCreate(Long organizationId) {
        // 查询组织管理员，项目成员角色，用于默认权限配置
        List<RoleVO> orgRoles = iamRemoteRepository.listRolesOnOrganizationLevel(organizationId, ChoerodonRole.Label.TENANT_ROLE_LABEL, null);
        List<RoleVO> projectRoles = iamRemoteRepository.listRolesOnOrganizationLevel(organizationId, ChoerodonRole.Label.PROJECT_ROLE_LABEL, null);
        orgRoles.addAll(projectRoles);

        // 生成组织设置-创建权限
        final ArrayList<PermissionRange> orgCreatePermissionRanges = Lists.newArrayList(
                // 组织层创建默认为组织管理员
                PermissionRange.of(
                        organizationId,
                        PermissionConstants.EMPTY_ID_PLACEHOLDER,
                        PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_CREATE_ORG.getCode(),
                        PermissionConstants.EMPTY_ID_PLACEHOLDER,
                        PermissionConstants.PermissionRangeType.MANAGER.toString(),
                        PermissionConstants.EMPTY_ID_PLACEHOLDER,
                        PermissionConstants.PermissionRole.NULL
                ),
                // 项目层创建默认为项目成员
                PermissionRange.of(
                        organizationId,
                        PermissionConstants.EMPTY_ID_PLACEHOLDER,
                        PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_CREATE_PROJECT.getCode(),
                        PermissionConstants.EMPTY_ID_PLACEHOLDER,
                        PermissionConstants.PermissionRangeType.MEMBER.toString(),
                        PermissionConstants.EMPTY_ID_PLACEHOLDER,
                        PermissionConstants.PermissionRole.NULL
                )
        );
        // 生成组织设置-默认权限
        List<PermissionRange> orgDefaultPermissionRanges = Lists.newArrayList();
        for (RoleVO orgRoleVO : orgRoles) {
            switch (orgRoleVO.getCode()) {
                case ChoerodonRole.RoleCode.TENANT_ADMIN:
                    orgDefaultPermissionRanges.add(PermissionRange.of(
                            organizationId,
                            PermissionConstants.EMPTY_ID_PLACEHOLDER,
                            PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_DEFAULT_ORG.toString(),
                            PermissionConstants.EMPTY_ID_PLACEHOLDER,
                            PermissionConstants.PermissionRangeType.ROLE.toString(),
                            orgRoleVO.getId(),
                            PermissionConstants.PermissionRole.MANAGER
                    ));
                    break;
                case ChoerodonRole.RoleCode.TENANT_MEMBER:
                    orgDefaultPermissionRanges.add(PermissionRange.of(
                            organizationId,
                            PermissionConstants.EMPTY_ID_PLACEHOLDER,
                            PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_DEFAULT_ORG.toString(),
                            PermissionConstants.EMPTY_ID_PLACEHOLDER,
                            PermissionConstants.PermissionRangeType.ROLE.toString(),
                            orgRoleVO.getId(),
                            PermissionConstants.PermissionRole.EDITOR
                    ));
                    break;
                case ChoerodonRole.RoleCode.PROJECT_ADMIN:
                    orgDefaultPermissionRanges.add(PermissionRange.of(
                            organizationId,
                            PermissionConstants.EMPTY_ID_PLACEHOLDER,
                            PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_DEFAULT_PROJECT.toString(),
                            PermissionConstants.EMPTY_ID_PLACEHOLDER,
                            PermissionConstants.PermissionRangeType.ROLE.toString(),
                            orgRoleVO.getId(),
                            PermissionConstants.PermissionRole.MANAGER
                    ));
                    break;
                case ChoerodonRole.RoleCode.PROJECT_MEMBER:
                    orgDefaultPermissionRanges.add(PermissionRange.of(
                            organizationId,
                            PermissionConstants.EMPTY_ID_PLACEHOLDER,
                            PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_DEFAULT_PROJECT.toString(),
                            PermissionConstants.EMPTY_ID_PLACEHOLDER,
                            PermissionConstants.PermissionRangeType.ROLE.toString(),
                            orgRoleVO.getId(),
                            PermissionConstants.PermissionRole.EDITOR
                    ));
                    break;
                default:
                    break;
            }
        }

        this.save(organizationId, OrganizationPermissionSettingVO.of(ListUtils.union(orgCreatePermissionRanges, orgDefaultPermissionRanges)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(Long organizationId, OrganizationPermissionSettingVO organizationPermissionSetting) {
        Assert.notNull(organizationPermissionSetting, BaseConstants.ErrorCode.NOT_NULL);
        Assert.notNull(organizationPermissionSetting.getOrganizationCreateRangeType(), "error.organizationCreateRangeType.null");
        Assert.notNull(organizationPermissionSetting.getProjectCreateRangeType(), "error.projectCreateRangeType.null");
        this.saveKnowledgeBaseCreatePermissionRange(ResourceLevel.ORGANIZATION, organizationId, organizationPermissionSetting);
        this.saveKnowledgeBaseCreatePermissionRange(ResourceLevel.PROJECT, organizationId, organizationPermissionSetting);
        this.saveKnowledgeBaseDefaultPermissionRange(ResourceLevel.ORGANIZATION, organizationId, organizationPermissionSetting);
        this.saveKnowledgeBaseDefaultPermissionRange(ResourceLevel.PROJECT, organizationId, organizationPermissionSetting);
    }

    /**
     * 保存组织级知识库权限设置--可创建知识库权限
     * @param resourceLevel                 层级--组织层/项目层
     * @param organizationId                组织ID
     * @param organizationPermissionSetting 待处理的数据
     */
    private void saveKnowledgeBaseCreatePermissionRange(ResourceLevel resourceLevel, Long organizationId, OrganizationPermissionSettingVO organizationPermissionSetting) {
        // 基础校验
        Assert.notNull(resourceLevel, BaseConstants.ErrorCode.NOT_NULL);
        Assert.isTrue(ResourceLevel.ORGANIZATION.equals(resourceLevel) || ResourceLevel.PROJECT.equals(resourceLevel), BaseConstants.ErrorCode.DATA_INVALID);
        Assert.notNull(organizationPermissionSetting, BaseConstants.ErrorCode.NOT_NULL);
        // 准备数据
        final PermissionConstants.PermissionRangeType permissionRangeType = PermissionConstants.PermissionRangeType.of(
                ResourceLevel.ORGANIZATION.equals(resourceLevel) ?
                    organizationPermissionSetting.getOrganizationCreateRangeType() :
                    organizationPermissionSetting.getProjectCreateRangeType()
        );
        List<PermissionRange> permissionRanges = Optional.ofNullable(
                    ResourceLevel.ORGANIZATION.equals(resourceLevel) ?
                        organizationPermissionSetting.getOrganizationCreateSetting() :
                        organizationPermissionSetting.getProjectCreateSetting()
                ).orElse(Collections.emptyList());
        final String targetType = ResourceLevel.ORGANIZATION.equals(resourceLevel) ?
                PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_CREATE_ORG.getCode() :
                PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_CREATE_PROJECT.getCode();

        switch ((permissionRangeType)) {
            // 界面上选择管理者, 则默认生成一条管理者的PermissionRange
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
                // 界面上选择成员, 则默认生成一条成员的PermissionRange
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
                // 否则, 按界面指定的数据保存
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
        // 校验数据合法性
        final PermissionDetailVO permissionDetail = PermissionDetailVO.of(targetType, PermissionConstants.EMPTY_ID_PLACEHOLDER, permissionRanges);
        PermissionDetailValidator.validateAndFillTargetType(
                permissionDetail,
                PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_SETTING_TARGET_TYPES,
                PermissionConstants.PermissionRangeType.KNOWLEDGE_BASE_SETTING_RANGE_TYPES,
                PermissionConstants.PermissionRole.ALL_CODES
        );
        // 调用commonSave保存数据, 注意这里由菜单权限体系控制, 不用额外鉴权
        this.commonSave(
                organizationId,
                PermissionConstants.EMPTY_ID_PLACEHOLDER,
                permissionDetail,
                false
        );
    }

    /**
     * 保存组织级知识库权限设置--组织/项目知识库默认权限
     * @param resourceLevel                 层级--组织层/项目层
     * @param organizationId                组织ID
     * @param organizationPermissionSetting 待处理的数据
     */
    private void saveKnowledgeBaseDefaultPermissionRange(ResourceLevel resourceLevel, Long organizationId, OrganizationPermissionSettingVO organizationPermissionSetting) {
        // 基础校验
        Assert.notNull(resourceLevel, BaseConstants.ErrorCode.NOT_NULL);
        Assert.isTrue(ResourceLevel.ORGANIZATION.equals(resourceLevel) || ResourceLevel.PROJECT.equals(resourceLevel), BaseConstants.ErrorCode.DATA_INVALID);
        Assert.notNull(organizationPermissionSetting, BaseConstants.ErrorCode.NOT_NULL);
        Assert.notNull(organizationPermissionSetting, BaseConstants.ErrorCode.NOT_NULL);
        // 准备数据
        final String targetType = ResourceLevel.ORGANIZATION.equals(resourceLevel) ?
                PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_DEFAULT_ORG.getCode() :
                PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_DEFAULT_PROJECT.getCode();
        final List<PermissionRange> permissionRanges = Optional.ofNullable(
                        ResourceLevel.ORGANIZATION.equals(resourceLevel) ?
                                organizationPermissionSetting.getOrganizationDefaultPermissionRange() :
                                organizationPermissionSetting.getProjectDefaultPermissionRange()
                ).orElse(Collections.emptyList())
                .stream()
                .map(permissionRange ->
                        permissionRange.setOrganizationId(organizationId)
                                .setProjectId(PermissionConstants.EMPTY_ID_PLACEHOLDER)
                                .setTargetType(targetType)
                                .setTargetValue(PermissionConstants.EMPTY_ID_PLACEHOLDER)
                )
                .collect(Collectors.toList());
        // 校验数据合法性
        final PermissionDetailVO permissionDetail = PermissionDetailVO.of(targetType, PermissionConstants.EMPTY_ID_PLACEHOLDER, permissionRanges);
        PermissionDetailValidator.validateAndFillTargetType(
                permissionDetail,
                PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_SETTING_TARGET_TYPES,
                PermissionConstants.PermissionRangeType.OBJECT_SETTING_RANGE_TYPES,
                PermissionConstants.PermissionRole.OBJECT_SETTING_ROLE_CODES
        );
        // 调用commonSave保存数据, 注意这里由菜单权限体系控制, 不用额外鉴权
        this.commonSave(
                organizationId,
                PermissionConstants.EMPTY_ID_PLACEHOLDER,
                permissionDetail,
                false
        );
    }

}
