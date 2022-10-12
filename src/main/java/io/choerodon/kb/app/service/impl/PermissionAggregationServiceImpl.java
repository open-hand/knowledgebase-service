package io.choerodon.kb.app.service.impl;

import static io.choerodon.kb.infra.enums.PermissionConstants.PermissionTargetBaseType;
import static io.choerodon.kb.infra.enums.PermissionConstants.PermissionTargetType;

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
import io.choerodon.kb.domain.repository.WorkSpaceRepository;
import io.choerodon.kb.domain.service.PermissionRangeKnowledgeObjectSettingService;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.enums.PermissionConstants;
import io.choerodon.kb.infra.enums.WorkSpaceType;

import org.hzero.core.util.AssertUtils;

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
    private final WorkSpaceRepository workSpaceRepository;

    public PermissionAggregationServiceImpl(PermissionRangeKnowledgeObjectSettingService objectSettingService,
                                            SecurityConfigRepository securityConfigRepository,
                                            WorkSpaceRepository workSpaceRepository) {
        this.objectSettingService = objectSettingService;
        this.securityConfigRepository = securityConfigRepository;
        this.workSpaceRepository = workSpaceRepository;
    }

    @Override
    public void autoGeneratePermission(Long organizationId,
                                       Long projectId,
                                       PermissionTargetBaseType targetBaseType,
                                       WorkSpaceTreeVO workSpace) {
        projectId = projectId == null ? 0L : projectId;
        String targetType = PermissionTargetType.getPermissionTargetType(projectId, targetBaseType.toString()).getCode();
        Long parentTargetValue = workSpace.getParentId();
        // 如果是顶层文件或文件夹，应该这里新增的数据应该是复制自知识库的
        String parentTargetType;
        if (parentTargetValue == 0L) {
            parentTargetType = PermissionTargetType.getPermissionTargetType(projectId, PermissionConstants.PermissionTargetBaseType.KNOWLEDGE_BASE.toString()).getCode();
            parentTargetValue = workSpace.getBaseId();
        } else {
            parentTargetType = queryParentTargetType(projectId, parentTargetValue);
        }
        // 1 查询父级安全设置信息
        SecurityConfig securityConfig =
                SecurityConfig.of(organizationId, projectId, parentTargetType, parentTargetValue, null, null);
        List<SecurityConfig> parentSecurityConfig = securityConfigRepository.select(securityConfig);
        // 2 清除数据变为可新增对象
        for (SecurityConfig config : parentSecurityConfig) {
            config.copy(targetType, workSpace.getId());
        }
        LOGGER.info("自动生成权限组装好的权限范围数据: {}", parentSecurityConfig);
        // 3 新增至数据库
        PermissionDetailVO permissionDetail = PermissionDetailVO.of(targetType, workSpace.getId(), Lists.newArrayList(), parentSecurityConfig);
        permissionDetail.setBaseTargetType(targetBaseType.toString());
        // TODO 安全设置保存暂未生效
        objectSettingService.saveRangeAndSecurity(organizationId, projectId, permissionDetail);
    }

    /**
     * 查询父级的PermissionTargetType
     * @param projectId             项目ID
     * @param parentTargetValue     父级ID
     * @return                      父级PermissionTargetType
     */
    private String queryParentTargetType(Long projectId, Long parentTargetValue) {
        WorkSpaceDTO parent = workSpaceRepository.selectByPrimaryKey(parentTargetValue);
        AssertUtils.notNull(parent, "error.work.space.parent.null");
        String parentType = parent.getType();
        PermissionTargetBaseType parentTargetBaseType;
        if (WorkSpaceType.FOLDER.getValue().equals(parentType)) {
            parentTargetBaseType = PermissionTargetBaseType.FOLDER;
        } else {
            parentTargetBaseType = PermissionTargetBaseType.FILE;
        }
        return PermissionTargetType.getPermissionTargetType(projectId, parentTargetBaseType.toString()).getCode();
    }

}
