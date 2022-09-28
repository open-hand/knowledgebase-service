package io.choerodon.kb.domain.service.impl;

import java.util.ArrayList;
import java.util.Collections;
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

import org.hzero.core.util.Pair;

/**
 * 权限范围知识对象设置 领域Service实现类
 *
 * @author gaokuo.dai@zknow.com 2022-09-27
 */
@Service
public class PermissionRangeKnowledgeObjectSettingServiceImpl implements PermissionRangeKnowledgeObjectSettingService {

    @Autowired
    private PermissionRangeKnowledgeObjectSettingRepository permissionRangeKnowledgeObjectSettingRepository;
    @Autowired
    private SecurityConfigService securityConfigService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionDetailVO saveRangeAndSecurity(Long organizationId,
                                                   Long projectId,
                                                   PermissionDetailVO permissionDetailVO) {
        PermissionDetailValidator.validate(permissionDetailVO);
        savePermissionRange(organizationId, projectId, permissionDetailVO);
        securityConfigService.saveSecurity(organizationId, projectId, permissionDetailVO);
        return permissionDetailVO;
    }

    @Override
    public PermissionDetailVO saveRange(Long organizationId,
                                        Long projectId,
                                        PermissionDetailVO permissionDetailVO) {
        PermissionDetailValidator.validate(permissionDetailVO);
        savePermissionRange(organizationId, projectId, permissionDetailVO);
        return permissionDetailVO;
    }

    @Override
    public List<PermissionRange> queryObjectSettingCollaborator(Long organizationId, Long projectId, CollaboratorSearchVO searchVO) {
        Assert.isTrue(PermissionConstants.PermissionTargetType.WORKSPACE_AND_BASE_TARGET_TYPES.contains(searchVO.getTargetType()), PermissionErrorCode.ERROR_TARGET_TYPES);
        return permissionRangeKnowledgeObjectSettingRepository.queryObjectSettingCollaborator(organizationId, projectId, searchVO);
    }

    private void savePermissionRange(Long organizationId,
                                     Long projectId,
                                     PermissionDetailVO permissionDetailVO) {
        String targetType = permissionDetailVO.getTargetType();
        Long targetValue = permissionDetailVO.getTargetValue();
        if (projectId == null) {
            projectId = 0L;
        }
        List<PermissionRange> permissionRanges = permissionDetailVO.getPermissionRanges();
        if (permissionRanges == null) {
            permissionRanges = Collections.emptyList();
        }
        Pair<List<PermissionRange>, List<PermissionRange>> pair =
                processAddAndDeleteList(organizationId, projectId, targetType, targetValue, permissionRanges);
        List<PermissionRange> addList = pair.getFirst();
        List<PermissionRange> deleteList = pair.getSecond();
        permissionRangeKnowledgeObjectSettingRepository.batchInsert(addList);
        if (!deleteList.isEmpty()) {
            permissionRangeKnowledgeObjectSettingRepository.batchDeleteByPrimaryKey(deleteList);
        }
    }

    private Pair<List<PermissionRange>, List<PermissionRange>> processAddAndDeleteList(Long organizationId,
                                                                                       Long projectId,
                                                                                       String targetType,
                                                                                       Long targetValue,
                                                                                       List<PermissionRange> permissionRanges) {
        List<PermissionRange> addList = new ArrayList<>();
        List<PermissionRange> deleteList = new ArrayList<>();
        PermissionRange example =
                PermissionRange.of(
                        organizationId,
                        projectId,
                        targetType,
                        targetValue,
                        null,
                        null,
                        null);
        List<PermissionRange> existedList = permissionRangeKnowledgeObjectSettingRepository.select(example);
        //交集
        List<PermissionRange> intersection = new ArrayList<>();
        for (PermissionRange permissionRange : permissionRanges) {
            for (PermissionRange existedPermissionRange : existedList) {
                if (permissionRange.equals(existedPermissionRange)) {
                    intersection.add(existedPermissionRange);
                }
            }
        }
        for (PermissionRange permissionRange : permissionRanges) {
            if (!intersection.contains(permissionRange)) {
                permissionRange.setOrganizationId(organizationId);
                permissionRange.setProjectId(projectId);
                addList.add(permissionRange);
            }
        }
        for (PermissionRange permissionRange : existedList) {
            if (!intersection.contains(permissionRange)) {
                deleteList.add(permissionRange);
            }
        }
        return Pair.of(addList, deleteList);
    }

}
