package io.choerodon.kb.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.choerodon.kb.infra.enums.PermissionConstants;
import org.hzero.core.util.AssertUtils;
import org.hzero.core.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.kb.app.service.SecurityConfigService;
import io.choerodon.kb.domain.entity.SecurityConfig;
import io.choerodon.kb.domain.repository.SecurityConfigRepository;

import org.hzero.core.base.BaseAppService;
import org.hzero.mybatis.helper.SecurityTokenHelper;
import org.springframework.util.ObjectUtils;

import java.util.*;

import static io.choerodon.kb.infra.enums.PermissionConstants.PermissionTargetType;


/**
 * 知识库安全设置应用服务默认实现
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
@Service
public class SecurityConfigServiceImpl extends BaseAppService implements SecurityConfigService {

    private static final Map<PermissionTargetType, PermissionConstants.PermissionTarget> PERMISSION_TARGET_TYPE_MAPPING;

    static {
        PERMISSION_TARGET_TYPE_MAPPING = new EnumMap<>(PermissionTargetType.class);
        PERMISSION_TARGET_TYPE_MAPPING.put(PermissionTargetType.KNOWLEDGE_BASE_ORG, PermissionConstants.PermissionTarget.KNOWLEDGE_BASE);
        PERMISSION_TARGET_TYPE_MAPPING.put(PermissionTargetType.KNOWLEDGE_BASE_PROJECT, PermissionConstants.PermissionTarget.KNOWLEDGE_BASE);
        PERMISSION_TARGET_TYPE_MAPPING.put(PermissionTargetType.FOLDER_ORG, PermissionConstants.PermissionTarget.FOLDER);
        PERMISSION_TARGET_TYPE_MAPPING.put(PermissionTargetType.FOLDER_PROJECT, PermissionConstants.PermissionTarget.FOLDER);
        PERMISSION_TARGET_TYPE_MAPPING.put(PermissionTargetType.FILE_ORG, PermissionConstants.PermissionTarget.FILE);
        PERMISSION_TARGET_TYPE_MAPPING.put(PermissionTargetType.FILE_PROJECT, PermissionConstants.PermissionTarget.FILE);
    }


    @Autowired
    private SecurityConfigRepository securityConfigRepository;

    @Override
    public SecurityConfig create(Long tenantId, SecurityConfig securityConfig) {
        validObject(securityConfig);
        securityConfigRepository.insertSelective(securityConfig);
        return securityConfig;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SecurityConfig update(Long tenantId, SecurityConfig securityConfig) {
        SecurityTokenHelper.validToken(securityConfig);
        securityConfigRepository.updateByPrimaryKeySelective(securityConfig);
        return securityConfig;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(SecurityConfig securityConfig) {
        SecurityTokenHelper.validToken(securityConfig);
        securityConfigRepository.deleteByPrimaryKey(securityConfig);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionDetailVO saveSecurity(Long organizationId,
                                           Long projectId,
                                           PermissionDetailVO permissionDetailVO) {
        String targetType = permissionDetailVO.getTargetType();
        Long targetValue = permissionDetailVO.getTargetValue();
        if (projectId == null) {
            projectId = 0L;
        }
        List<SecurityConfig> securityConfigs = permissionDetailVO.getSecurityConfigs();
        if (securityConfigs == null) {
            securityConfigs = new ArrayList<>();
            permissionDetailVO.setSecurityConfigs(securityConfigs);
        }
        PermissionTargetType permissionTargetType = PermissionTargetType.valueOf(targetType.toUpperCase());
        PermissionConstants.PermissionTarget permissionTarget = PERMISSION_TARGET_TYPE_MAPPING.get(permissionTargetType);
        if (permissionTarget == null) {
            throw new CommonException("error.illegal.permission.range.target.type", targetType);
        }

        List<SecurityConfig> existedList =
                queryExistedList(organizationId, projectId, targetType, targetValue, permissionTarget);
        Pair<List<SecurityConfig>, List<SecurityConfig>> pair =
                processInsertAndUpdateList(organizationId, projectId, targetType, targetValue, securityConfigs, existedList);
        List<SecurityConfig> insertList = pair.getFirst();
        List<SecurityConfig> updateList = pair.getSecond();
        securityConfigRepository.batchInsert(insertList);
        securityConfigRepository.batchUpdateByPrimaryKey(updateList);
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

    private List<SecurityConfig> queryExistedList(Long organizationId,
                                                  Long projectId,
                                                  String targetType,
                                                  Long targetValue,
                                                  PermissionConstants.PermissionTarget permissionTarget) {
        SecurityConfig example =
                SecurityConfig.of(
                        organizationId,
                        projectId,
                        targetType,
                        targetValue,
                        null,
                        null);
        List<SecurityConfig> existedList = securityConfigRepository.select(example);
        List<SecurityConfig> securityConfigByAction = new ArrayList<>();
        for (PermissionConstants.SecurityConfigAction securityConfigAction : PermissionConstants.SecurityConfigAction.values()) {
            StringBuilder builder = new StringBuilder();
            builder.append(permissionTarget.name()).append(".").append(securityConfigAction.name());
            String permissionCode = builder.toString();
            SecurityConfig securityConfig =
                    SecurityConfig.of(
                            organizationId,
                            projectId,
                            targetType,
                            targetValue,
                            permissionCode,
                            0);
            securityConfigByAction.add(securityConfig);
        }
        List<SecurityConfig> initList = new ArrayList<>();
        for (SecurityConfig securityConfig : securityConfigByAction) {
            if (!securityConfig.in(existedList, true)) {
                initList.add(securityConfig);
            }
        }
        existedList.addAll(initList);
        return existedList;
    }
}
