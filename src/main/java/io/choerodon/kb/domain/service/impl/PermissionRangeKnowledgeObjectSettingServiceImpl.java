package io.choerodon.kb.domain.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.kb.api.validator.PermissionDetailValidator;
import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.choerodon.kb.app.service.SecurityConfigService;
import io.choerodon.kb.domain.repository.PermissionRangeKnowledgeObjectSettingRepository;
import io.choerodon.kb.domain.repository.WorkSpaceRepository;
import io.choerodon.kb.domain.service.PermissionCheckDomainService;
import io.choerodon.kb.domain.service.PermissionRangeKnowledgeObjectSettingService;
import io.choerodon.kb.infra.enums.PermissionConstants;

/**
 * 权限范围知识对象设置 领域Service实现类
 *
 * @author gaokuo.dai@zknow.com 2022-09-27
 */
@Service
public class PermissionRangeKnowledgeObjectSettingServiceImpl extends PermissionRangeBaseDomainServiceImpl implements PermissionRangeKnowledgeObjectSettingService {

    @Autowired
    private PermissionRangeKnowledgeObjectSettingRepository permissionRangeKnowledgeObjectSettingRepository;
    @Autowired
    private SecurityConfigService securityConfigService;
    @Autowired
    private PermissionCheckDomainService permissionCheckDomainService;
    @Autowired
    private WorkSpaceRepository workSpaceRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionDetailVO saveRangeAndSecurity(Long organizationId,
                                                   Long projectId,
                                                   PermissionDetailVO permissionDetailVO,
                                                   boolean checkPermission) {
        permissionDetailVO.transformBaseTargetType(projectId);
        PermissionDetailValidator.validateAndFillTargetType(
                permissionDetailVO,
                PermissionConstants.PermissionTargetType.OBJECT_SETTING_TARGET_TYPES,
                PermissionConstants.PermissionRangeType.OBJECT_SETTING_RANGE_TYPES,
                PermissionConstants.PermissionRole.OBJECT_SETTING_ROLE_CODES
        );
        permissionDetailVO = this.commonSave(organizationId, projectId, permissionDetailVO, checkPermission);
        securityConfigService.saveSecurity(organizationId, projectId, permissionDetailVO, checkPermission);
        return permissionDetailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionDetailVO saveRange(Long organizationId,
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
        return this.commonSave(organizationId, projectId, permissionDetail, checkPermission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removePermissionRange(Long organizationId, Long projectId, PermissionConstants.PermissionTargetBaseType baseTargetType, Long targetValue) {
        permissionRangeKnowledgeObjectSettingRepository.remove(organizationId, projectId, targetValue);
        deleteTargetParentRedisCache(baseTargetType, targetValue);
    }

    private void deleteTargetParentRedisCache(PermissionConstants.PermissionTargetBaseType baseTargetType,
                                              Long id) {
        if (PermissionConstants.PermissionTargetBaseType.KNOWLEDGE_BASE.equals(baseTargetType)) {
            //知识库不处理
            return;
        }
        workSpaceRepository.delTargetParentRedisCache(id);
    }

}
