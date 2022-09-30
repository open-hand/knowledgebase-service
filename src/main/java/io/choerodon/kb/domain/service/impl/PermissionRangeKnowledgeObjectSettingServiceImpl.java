package io.choerodon.kb.domain.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import io.choerodon.kb.api.validator.PermissionDetailValidator;
import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.choerodon.kb.api.vo.permission.PermissionSearchVO;
import io.choerodon.kb.app.service.KnowledgeBaseService;
import io.choerodon.kb.app.service.SecurityConfigService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.repository.PermissionRangeKnowledgeObjectSettingRepository;
import io.choerodon.kb.domain.service.PermissionRangeKnowledgeObjectSettingService;
import io.choerodon.kb.infra.common.PermissionErrorCode;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.enums.PermissionConstants;

import org.hzero.core.base.BaseConstants;

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
    private WorkSpaceService workSpaceService;
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionDetailVO saveRangeAndSecurity(Long organizationId,
                                                   Long projectId,
                                                   PermissionDetailVO permissionDetailVO) {
        permissionDetailVO.transformBaseTargetType(projectId);
        PermissionDetailValidator.validateAndFillTargetType(
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
    @Transactional(rollbackFor = Exception.class)
    public PermissionDetailVO saveRange(Long organizationId,
                                        Long projectId,
                                        PermissionDetailVO permissionDetailVO) {
        permissionDetailVO.transformBaseTargetType(projectId);
        PermissionDetailValidator.validateAndFillTargetType(
                permissionDetailVO,
                PermissionConstants.PermissionTargetType.OBJECT_SETTING_TARGET_TYPES,
                PermissionConstants.PermissionRangeType.OBJECT_SETTING_RANGE_TYPES,
                PermissionConstants.PermissionRole.OBJECT_SETTING_ROLE_CODES
        );
        return this.commonSave(organizationId, projectId, permissionDetailVO);
    }

    @Override
    public List<PermissionRange> queryCollaborator(Long organizationId, Long projectId, PermissionSearchVO searchVO) {
        Assert.isTrue(PermissionConstants.PermissionTargetBaseType.isValid(searchVO.getBaseTargetType()), PermissionErrorCode.ERROR_TARGET_TYPES);
        searchVO.transformBaseTargetType(projectId);
        // 不是知识库需要查询父级
        PermissionConstants.PermissionTargetBaseType targetBaseType = PermissionConstants.PermissionTargetBaseType.of(searchVO.getBaseTargetType());
        List<PermissionRange> kbRanges = null;
        List<PermissionRange> wsRanges = null;
        Long createdBy;
        if (targetBaseType != PermissionConstants.PermissionTargetBaseType.KNOWLEDGE_BASE) {
            WorkSpaceDTO workSpaceDTO = workSpaceService.selectById(searchVO.getTargetValue());
            createdBy = workSpaceDTO.getCreatedBy();
            // 查询继承自知识库的权限
            PermissionSearchVO kbSearchVO = new PermissionSearchVO();
            kbSearchVO.setTargetType(PermissionConstants.PermissionTargetType.getKBTargetType(projectId).getCode());
            kbSearchVO.setTargetValue(workSpaceDTO.getBaseId());
            kbRanges = permissionRangeKnowledgeObjectSettingRepository.queryObjectSettingCollaborator(organizationId, projectId, kbSearchVO);
            // 如果父级是不是知识库，要查询继承自父级的文件和文件夹权限
            if (workSpaceDTO.getParentId() != 0L) {
                String route = workSpaceDTO.getRoute();
                Set<String> workspaceIds = Sets.newHashSet(StringUtils.split(route, BaseConstants.Symbol.POINT));
                workspaceIds.remove(searchVO.getTargetValue());
                // 根据层级拿到层级的文件和文件夹类型
                HashSet<PermissionConstants.PermissionTargetType> resourceTargetTypes = PermissionConstants.PermissionTargetType.getKBObjectTargetTypes(projectId);
                wsRanges = permissionRangeKnowledgeObjectSettingRepository.selectFolderAndFileByTargetValues(organizationId, projectId, resourceTargetTypes, workspaceIds);
            }
        } else {
            // 赋值知识库的创建者
            createdBy = knowledgeBaseService.queryById(searchVO.getTargetValue()).getCreatedBy();
        }
        List<PermissionRange> selfCollaborators = permissionRangeKnowledgeObjectSettingRepository.queryObjectSettingCollaborator(organizationId, projectId, searchVO);
        // 创建人在最前面
        Long finalCreatedBy = createdBy;
        Map<Boolean, List<PermissionRange>> createGroup = selfCollaborators.stream().collect(Collectors.partitioningBy(pr -> pr.getCreatedBy().equals(finalCreatedBy)));
        List<PermissionRange> elements = createGroup.get(true);
        for (PermissionRange element : elements) {
            element.setOwnerFlag(true);
        }
        List<PermissionRange> results = Lists.newArrayList(elements);
        // 然后是知识库继承
        Optional.ofNullable(kbRanges).ifPresent(results::addAll);
        // 再是上级继承
        Optional.ofNullable(wsRanges).ifPresent(results::addAll);
        // 最后是自己编辑的
        Optional.ofNullable(createGroup.get(false)).ifPresent(results::addAll);
        return results;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clear(Long organizationId, Long projectId, PermissionConstants.PermissionTargetBaseType baseTargetType, Long targetValue) {
        permissionRangeKnowledgeObjectSettingRepository.clear(organizationId, projectId, targetValue);
    }

}
