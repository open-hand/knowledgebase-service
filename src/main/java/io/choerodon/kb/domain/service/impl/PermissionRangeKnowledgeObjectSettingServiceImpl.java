package io.choerodon.kb.domain.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.api.validator.PermissionDetailValidator;
import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.choerodon.kb.api.vo.permission.PermissionSearchVO;
import io.choerodon.kb.app.service.KnowledgeBaseService;
import io.choerodon.kb.app.service.SecurityConfigService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.entity.UserInfo;
import io.choerodon.kb.domain.repository.IamRemoteRepository;
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
    @Autowired
    private IamRemoteRepository iamRemoteRepository;

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
        if (targetBaseType != PermissionConstants.PermissionTargetBaseType.KNOWLEDGE_BASE) {
            WorkSpaceDTO workSpaceDTO = workSpaceService.selectById(searchVO.getTargetValue());
            Assert.notNull(workSpaceDTO, BaseConstants.ErrorCode.DATA_NOT_EXISTS);
            // 查询继承自知识库的权限
            kbRanges = queryPermissionRangesInheritFromKnowledgeBase(organizationId, projectId, workSpaceDTO.getBaseId());
            // 如果父级是不是知识库，要查询继承自父级的文件和文件夹权限
            if (workSpaceDTO.getParentId() != 0L) {
                wsRanges = queryPermissionRangesInheritFromParent(organizationId, projectId, searchVO, workSpaceDTO.getRoute());
            }
        }
        List<PermissionRange> selfCollaborators = permissionRangeKnowledgeObjectSettingRepository.queryObjectSettingCollaborator(organizationId, projectId, searchVO);
        // 按是否是所有者分组
        Map<Boolean, List<PermissionRange>> isOwnerGroup = selfCollaborators.stream().collect(Collectors.partitioningBy(pr -> Boolean.TRUE.equals(pr.getOwnerFlag())));
        // 所有者在最前面
        List<PermissionRange> results = Lists.newArrayList(isOwnerGroup.get(true));
        // 然后是知识库继承
        Optional.ofNullable(kbRanges).ifPresent(results::addAll);
        // 再是上级继承
        Optional.ofNullable(wsRanges).ifPresent(results::addAll);
        // 最后是自己编辑的
        Optional.ofNullable(isOwnerGroup.get(false)).ifPresent(results::addAll);
        return results;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clear(Long organizationId, Long projectId, PermissionConstants.PermissionTargetBaseType baseTargetType, Long targetValue) {
        permissionRangeKnowledgeObjectSettingRepository.clear(organizationId, projectId, targetValue);
    }

    @Override
    public boolean hasKnowledgeBasePermission(Long organizationId,
                                              Long projectId,
                                              Long baseId,
                                              UserInfo userInfo) {
        boolean isAdmin = Boolean.TRUE.equals(userInfo.getAdminFlag());
        if (isAdmin) {
            return true;
        }
        if (projectId == null) {
            projectId = PermissionConstants.EMPTY_ID_PLACEHOLDER;
        }
        PermissionConstants.PermissionTargetType permissionTargetType =
                PermissionConstants.PermissionTargetType.getPermissionTargetType(projectId, PermissionConstants.PermissionTargetBaseType.KNOWLEDGE_BASE.toString());
        String targetType = permissionTargetType.toString();
        PermissionRange publicRange = PermissionRange.of(
                organizationId,
                projectId,
                targetType,
                baseId,
                PermissionConstants.PermissionRangeType.PUBLIC.toString(),
                null,
                null);
        List<PermissionRange> publicRangeList = permissionRangeKnowledgeObjectSettingRepository.select(publicRange);
        if (!publicRangeList.isEmpty()) {
            return true;
        }
        List<PermissionRange> permissionRangeList =
                permissionRangeKnowledgeObjectSettingRepository.queryByUser(organizationId, projectId, targetType, baseId, userInfo);
        return !permissionRangeList.isEmpty();
    }

    @Override
    public UserInfo queryUserInfo(Long organizationId,
                                  Long projectId) {
        CustomUserDetails customUserDetails = DetailsHelper.getUserDetails();
        UserInfo userInfo = iamRemoteRepository.queryUserInfo(customUserDetails.getUserId(), organizationId, projectId);
        Assert.notNull(userInfo, "error.permission.range.user.not.existed");
        userInfo.setAdminFlag(customUserDetails.getAdmin());
        return userInfo;
    }

    /**
     * 查询继承自知识库的权限
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @param knowledgeBaseId   知识库ID
     * @return                  查询结果
     */
    private List<PermissionRange> queryPermissionRangesInheritFromKnowledgeBase(Long organizationId, Long projectId, Long knowledgeBaseId) {
        List<PermissionRange> kbRanges;
        PermissionSearchVO kbSearchVO = new PermissionSearchVO();
        kbSearchVO.setTargetType(PermissionConstants.PermissionTargetType.getKBTargetType(projectId).getCode());
        kbSearchVO.setTargetValue(knowledgeBaseId);
        kbRanges = permissionRangeKnowledgeObjectSettingRepository.queryObjectSettingCollaborator(organizationId, projectId, kbSearchVO);
        return kbRanges;
    }

    /**
     * 查询继承自父级的文件和文件夹权限
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @param searchVO          搜索条件
     * @param route             level path
     * @return                  查询结果
     */
    private List<PermissionRange> queryPermissionRangesInheritFromParent(Long organizationId, Long projectId, PermissionSearchVO searchVO, String route) {
        List<PermissionRange> wsRanges;
        Assert.hasText(route, BaseConstants.ErrorCode.NOT_NULL);
        final String[] workspaceIdsArray = StringUtils.split(route, BaseConstants.Symbol.POINT);
        Assert.notNull(workspaceIdsArray, BaseConstants.ErrorCode.NOT_NULL);
        Set<String> workspaceIds = Sets.newHashSet(workspaceIdsArray);
        workspaceIds.remove(String.valueOf(searchVO.getTargetValue()));
        // 根据层级拿到层级的文件和文件夹类型
        Set<PermissionConstants.PermissionTargetType> resourceTargetTypes = PermissionConstants.PermissionTargetType.getKBObjectTargetTypes(projectId);
        wsRanges = permissionRangeKnowledgeObjectSettingRepository.selectFolderAndFileByTargetValues(organizationId, projectId, resourceTargetTypes, workspaceIds);
        return wsRanges;
    }

}
