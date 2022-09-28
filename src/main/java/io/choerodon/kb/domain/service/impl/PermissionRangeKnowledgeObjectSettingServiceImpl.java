package io.choerodon.kb.domain.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.kb.api.validator.PermissionDetailValidator;
import io.choerodon.kb.api.vo.permission.CollaboratorSearchVO;
import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.choerodon.kb.app.service.SecurityConfigService;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.repository.PermissionRangeKnowledgeObjectSettingRepository;
import io.choerodon.kb.domain.service.PermissionRangeKnowledgeObjectSettingService;
import io.choerodon.kb.infra.common.PermissionErrorCode;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionDetailVO saveRangeAndSecurity(Long organizationId,
                                                   Long projectId,
                                                   PermissionDetailVO permissionDetailVO) {
        PermissionDetailValidator.validate(
                permissionDetailVO,
                PermissionConstants.PermissionTargetType.OBJECT_SETTING_TARGET_TYPES,
                PermissionConstants.PermissionRangeType.OBJECT_SETTING_RANGE_TYPES,
                PermissionConstants.PermissionRole.OBJECT_SETTING_ROLE_CODES
        );
        permissionDetailVO = this.commonSave(organizationId, projectId, permissionDetailVO);
        securityConfigService.saveSecurity(organizationId, projectId, permissionDetailVO);
        return permissionDetailVO;
    }

    @Override
    public PermissionDetailVO saveRange(Long organizationId,
                                        Long projectId,
                                        PermissionDetailVO permissionDetailVO) {
        PermissionDetailValidator.validate(
                permissionDetailVO,
                PermissionConstants.PermissionTargetType.OBJECT_SETTING_TARGET_TYPES,
                PermissionConstants.PermissionRangeType.OBJECT_SETTING_RANGE_TYPES,
                PermissionConstants.PermissionRole.OBJECT_SETTING_ROLE_CODES
        );
        return this.commonSave(organizationId, projectId, permissionDetailVO);
    }

    @Override
    public List<PermissionRange> queryObjectSettingCollaborator(Long organizationId, Long projectId, CollaboratorSearchVO searchVO) {
        Assert.isTrue(PermissionConstants.PermissionTargetType.OBJECT_SETTING_TARGET_TYPES.contains(searchVO.getTargetType()), PermissionErrorCode.ERROR_TARGET_TYPES);
        return permissionRangeKnowledgeObjectSettingRepository.queryObjectSettingCollaborator(organizationId, projectId, searchVO);
    }

}
