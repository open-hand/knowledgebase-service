package io.choerodon.kb.app.service.impl;

import static io.choerodon.kb.infra.enums.PermissionConstants.PermissionTargetType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.validator.PermissionDetailValidator;
import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.choerodon.kb.app.service.SecurityConfigService;
import io.choerodon.kb.domain.entity.SecurityConfig;
import io.choerodon.kb.domain.repository.SecurityConfigRepository;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionDetailVO saveSecurity(Long organizationId,
                                           Long projectId,
                                           PermissionDetailVO permissionDetailVO) {
        permissionDetailVO.transformBaseTargetType(projectId);
        PermissionDetailValidator.validateAndFillTargetType(
                permissionDetailVO,
                PermissionConstants.PermissionTargetType.OBJECT_SETTING_TARGET_TYPES,
                PermissionConstants.PermissionRangeType.OBJECT_SETTING_RANGE_TYPES,
                PermissionConstants.PermissionRole.OBJECT_SETTING_ROLE_CODES
        );
        String targetType = permissionDetailVO.getTargetType();
        Long targetValue = permissionDetailVO.getTargetValue();
        if (projectId == null) {
            projectId = PermissionConstants.EMPTY_ID_PLACEHOLDER;
        }
        List<SecurityConfig> securityConfigs = permissionDetailVO.getSecurityConfigs();
        if (securityConfigs == null) {
            securityConfigs = new ArrayList<>();
            permissionDetailVO.setSecurityConfigs(securityConfigs);
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
        return permissionDetailVO;
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
}
