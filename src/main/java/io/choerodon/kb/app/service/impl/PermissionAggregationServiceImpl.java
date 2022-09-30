package io.choerodon.kb.app.service.impl;

import java.util.List;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.choerodon.kb.api.vo.WorkSpaceTreeVO;
import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.choerodon.kb.app.service.PermissionAggregationService;
import io.choerodon.kb.domain.entity.SecurityConfig;
import io.choerodon.kb.domain.repository.SecurityConfigRepository;
import io.choerodon.kb.domain.service.PermissionRangeKnowledgeObjectSettingService;
import io.choerodon.kb.infra.enums.PermissionConstants;

/**
 * Copyright (c) 2022. Zknow Enterprise Solution. All right reserved.
 *
 * @author zongqi.hao@zknow.com
 * @since 2022/9/29
 */
@Service
public class PermissionAggregationServiceImpl implements PermissionAggregationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionAggregationServiceImpl.class);

    private final PermissionRangeKnowledgeObjectSettingService objectSettingService;
    private final SecurityConfigRepository securityConfigRepository;


    public PermissionAggregationServiceImpl(PermissionRangeKnowledgeObjectSettingService objectSettingService, SecurityConfigRepository securityConfigRepository) {
        this.objectSettingService = objectSettingService;
        this.securityConfigRepository = securityConfigRepository;
    }

    @Override
    public void autoGeneratePermission(Long organizationId,
                                       Long projectId,
                                       PermissionConstants.PermissionTargetBaseType targetBaseType,
                                       WorkSpaceTreeVO workSpace) {
        projectId = projectId == null ? 0L : projectId;
        String targetType = PermissionConstants.PermissionTargetType.getPermissionTargetType(projectId, targetBaseType.toString()).getCode();
        Long parentTargetValue = workSpace.getParentId();
        // 如果是顶层文件或文件夹，应该这里新增的数据应该是复制自知识库的
        String parentTargetType = targetType;
        if (parentTargetValue == 0L) {
            parentTargetType = PermissionConstants.PermissionTargetType.getPermissionTargetType(projectId, PermissionConstants.PermissionTargetBaseType.KNOWLEDGE_BASE.toString()).getCode();
            parentTargetValue = workSpace.getBaseId();
        }
        // 1 查询父级安全设置信息
        SecurityConfig securityConfig = new SecurityConfig();
        securityConfig.setOrganizationId(organizationId);
        securityConfig.setProjectId(projectId);
        securityConfig.setTargetType(parentTargetType);
        securityConfig.setTargetValue(parentTargetValue);
        List<SecurityConfig> parentSecurityConfig = securityConfigRepository.select(securityConfig);
        // 2 清除数据变为可新增对象
        for (SecurityConfig config : parentSecurityConfig) {
            config.copy(targetType, workSpace.getId());
        }
        LOGGER.info("自动生成权限组装好的权限范围数据: {}", parentSecurityConfig);
        // 3 新增至数据库
        PermissionDetailVO of = PermissionDetailVO.of(securityConfig.getTargetType(), workSpace.getId(), Lists.newArrayList(), parentSecurityConfig);
        of.setBaseTargetType(targetBaseType.toString());
        // TODO 安全设置保存暂未生效
        objectSettingService.saveRangeAndSecurity(organizationId, projectId, of);
    }

}
