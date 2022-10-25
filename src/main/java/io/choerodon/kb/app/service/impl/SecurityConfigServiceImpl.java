package io.choerodon.kb.app.service.impl;

import static io.choerodon.kb.infra.enums.PermissionConstants.PermissionTargetType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.validator.PermissionDetailValidator;
import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.choerodon.kb.app.service.SecurityConfigService;
import io.choerodon.kb.domain.entity.SecurityConfig;
import io.choerodon.kb.domain.repository.SecurityConfigRepository;
import io.choerodon.kb.domain.service.PermissionCheckDomainService;
import io.choerodon.kb.infra.enums.PermissionConstants;

import org.hzero.core.base.BaseAppService;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.util.AssertUtils;
import org.hzero.core.util.Pair;

/**
 * 知识库安全设置应用服务默认实现
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
@Service
public class SecurityConfigServiceImpl extends BaseAppService implements SecurityConfigService {

    @Autowired
    private SecurityConfigRepository securityConfigRepository;
    @Autowired
    private PermissionCheckDomainService permissionCheckDomainService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionDetailVO saveSecurity(Long organizationId,
                                           Long projectId,
                                           PermissionDetailVO permissionDetail,
                                           boolean checkPermission) {
        permissionDetail.transformBaseTargetType(projectId);
        PermissionDetailValidator.validateAndFillTargetType(
                permissionDetail,
                PermissionConstants.PermissionTargetType.OBJECT_SETTING_TARGET_TYPES,
                PermissionConstants.PermissionRangeType.OBJECT_SETTING_RANGE_TYPES,
                PermissionConstants.PermissionRole.OBJECT_SETTING_ROLE_CODES
        );
        String targetType = permissionDetail.getTargetType();
        Long targetValue = permissionDetail.getTargetValue();
        if (projectId == null) {
            projectId = PermissionConstants.EMPTY_ID_PLACEHOLDER;
        }
        List<SecurityConfig> securityConfigs = permissionDetail.getSecurityConfigs();
        if (securityConfigs == null) {
            securityConfigs = new ArrayList<>();
            permissionDetail.setSecurityConfigs(securityConfigs);
        }
        if(checkPermission) {
            Assert.isTrue(this.canModify(organizationId, projectId, permissionDetail), BaseConstants.ErrorCode.FORBIDDEN);
        }
        PermissionTargetType permissionTargetType = PermissionTargetType.valueOf(targetType.toUpperCase());
        Pair<List<SecurityConfig>, Boolean> existedListPair =
                queryExistedList(organizationId, projectId, targetType, targetValue, permissionTargetType);
        List<SecurityConfig> existedList = existedListPair.getFirst();
        Boolean dbEmpty = existedListPair.getSecond();
        if (ObjectUtils.isEmpty(securityConfigs) && Boolean.TRUE.equals(dbEmpty)) {
            //init default security config
            securityConfigs.addAll(
                    generateConfigFromAction(organizationId, projectId, targetType, targetValue, permissionTargetType));
        }
        Pair<List<SecurityConfig>, List<SecurityConfig>> pair =
                processInsertAndUpdateList(organizationId, projectId, targetType, targetValue, securityConfigs, existedList);
        List<SecurityConfig> insertList = pair.getFirst();
        List<SecurityConfig> updateList = pair.getSecond();
        securityConfigRepository.batchInsert(insertList);
        securityConfigRepository.batchUpdateByPrimaryKey(updateList);

        this.securityConfigRepository.clearCache(organizationId, projectId, ListUtils.union(insertList, updateList));
        return permissionDetail;
    }

    private Pair<List<SecurityConfig>, List<SecurityConfig>> processInsertAndUpdateList(Long organizationId,
                                                                                        Long projectId,
                                                                                        String targetType,
                                                                                        Long targetValue,
                                                                                        List<SecurityConfig> inputList,
                                                                                        List<SecurityConfig> existedList) {
        List<SecurityConfig> insertList = new ArrayList<>();
        List<SecurityConfig> updateList = new ArrayList<>();

        for (SecurityConfig input : inputList) {
            input.setTargetType(targetType);
            input.setTargetValue(targetValue);
            input.setOrganizationId(organizationId);
            input.setProjectId(projectId);
            for (SecurityConfig existedOne : existedList) {
                if (!input.equalsWithoutAuthorizeFlag(existedOne)) {
                    continue;
                }
                Long id = existedOne.getId();
                Integer authorizeFlag = input.getAuthorizeFlag();
                AssertUtils.notNull(authorizeFlag, "error.illegal.permission.security.config.authorizeFlag.null", "");
                if (ObjectUtils.isEmpty(id)) {
                    //插入
                    existedOne.setAuthorizeFlag(authorizeFlag);
                    insertList.add(existedOne);
                } else {
                    Integer targetAuthorizeFlag = existedOne.getAuthorizeFlag();
                    if (!targetAuthorizeFlag.equals(authorizeFlag)) {
                        existedOne.setAuthorizeFlag(authorizeFlag);
                        updateList.add(existedOne);
                    }
                }
            }
        }
        return Pair.of(insertList, updateList);
    }

    private Pair<List<SecurityConfig>, Boolean> queryExistedList(Long organizationId,
                                                                 Long projectId,
                                                                 String targetType,
                                                                 Long targetValue,
                                                                 PermissionTargetType permissionTargetType) {
        SecurityConfig example =
                SecurityConfig.of(
                        organizationId,
                        projectId,
                        targetType,
                        targetValue,
                        null,
                        null);
        List<SecurityConfig> existedList = securityConfigRepository.select(example);
        Boolean dbEmpty = existedList.isEmpty();
        List<SecurityConfig> securityConfigByAction = generateConfigFromAction(organizationId, projectId, targetType, targetValue, permissionTargetType);
        List<SecurityConfig> initList = new ArrayList<>();
        for (SecurityConfig securityConfig : securityConfigByAction) {
            if (!securityConfig.in(existedList, true)) {
                initList.add(securityConfig);
            }
        }
        existedList.addAll(initList);
        return Pair.of(existedList, dbEmpty);
    }

    private List<SecurityConfig> generateConfigFromAction(Long organizationId, Long projectId, String targetType, Long targetValue, PermissionTargetType permissionTargetType) {
        List<SecurityConfig> securityConfigByAction = new ArrayList<>();
        PermissionConstants.PermissionTargetBaseType permissionTargetBaseType = permissionTargetType.getBaseType();
        if (permissionTargetBaseType == null) {
            throw new CommonException("error.permission.target.type.not.mapping.base.type");
        }
        Set<String> permissionCodes =
                PermissionConstants.SecurityConfigAction
                        .buildPermissionCodeByType(permissionTargetBaseType);
        for (String permissionCode : permissionCodes) {
            SecurityConfig securityConfig =
                    SecurityConfig.of(
                            organizationId,
                            projectId,
                            targetType,
                            targetValue,
                            permissionCode,
                            BaseConstants.Flag.YES);
            securityConfigByAction.add(securityConfig);
        }
        return securityConfigByAction;
    }

    /**
     * 是否有权限修改安全设置
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @param permissionDetail  待保存的权限数据
     * @return                  鉴权结果
     */
    private boolean canModify(Long organizationId, Long projectId, PermissionDetailVO permissionDetail) {
        // 基础校验 & 数据准备
        if(organizationId == null || projectId == null || permissionDetail == null) {
            return false;
        }
        final String targetType = permissionDetail.getTargetType();
        final Long targetValue = permissionDetail.getTargetValue();
        if(StringUtils.isBlank(targetType) || targetValue  == null) {
            return false;
        }
        final PermissionConstants.PermissionTargetBaseType targetBaseType = PermissionConstants.PermissionTargetType.of(targetType).getBaseType();
        final String permissionCodeWaitCheck;
        if(PermissionConstants.PermissionTargetBaseType.KNOWLEDGE_BASE.equals(targetBaseType)) {
            permissionCodeWaitCheck = PermissionConstants.ActionPermission.KNOWLEDGE_BASE_SECURITY_SETTINGS.getCode();
        } else if(PermissionConstants.PermissionTargetBaseType.FOLDER.equals(targetBaseType)) {
            permissionCodeWaitCheck = PermissionConstants.ActionPermission.FOLDER_SECURITY_SETTINGS.getCode();
        } else if(PermissionConstants.PermissionTargetBaseType.FILE.equals(targetBaseType)) {
            permissionCodeWaitCheck = PermissionConstants.ActionPermission.FILE_SECURITY_SETTINGS.getCode();
        } else {
            throw new CommonException(BaseConstants.ErrorCode.DATA_INVALID);
        }
        // 调用鉴权器鉴权
        return this.permissionCheckDomainService.checkPermission(
                organizationId,
                projectId,
                null,
                targetType,
                targetValue,
                permissionCodeWaitCheck
        );
    }
}
