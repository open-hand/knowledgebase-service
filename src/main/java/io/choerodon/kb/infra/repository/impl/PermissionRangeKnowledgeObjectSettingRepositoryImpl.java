package io.choerodon.kb.infra.repository.impl;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import io.choerodon.kb.api.vo.permission.PermissionSearchVO;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.entity.UserInfo;
import io.choerodon.kb.domain.repository.KnowledgeBaseRepository;
import io.choerodon.kb.domain.repository.PermissionRangeKnowledgeObjectSettingRepository;
import io.choerodon.kb.domain.repository.WorkSpaceRepository;
import io.choerodon.kb.infra.common.PermissionErrorCode;
import io.choerodon.kb.infra.dto.KnowledgeBaseDTO;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.enums.PermissionConstants;

import org.hzero.core.base.BaseConstants;
import org.hzero.mybatis.domian.Condition;

/**
 * 权限范围知识对象设置 领域资源库实现
 *
 * @author zongqi.hao@zknow.com 2022-09-23
 */
@Repository
public class PermissionRangeKnowledgeObjectSettingRepositoryImpl extends PermissionRangeBaseRepositoryImpl implements PermissionRangeKnowledgeObjectSettingRepository {

    @Autowired
    private WorkSpaceRepository workSpaceRepository;
    @Autowired
    private KnowledgeBaseRepository knowledgeBaseRepository;

    @Override
    public List<PermissionRange> queryObjectSettingCollaborator(Long organizationId, Long projectId, PermissionSearchVO searchVO) {
        Condition condition = getCondition();
        Condition.Criteria criteria = condition.createCriteria();
        criteria.andEqualTo(PermissionRange.FIELD_ORGANIZATION_ID, organizationId);
        criteria.andEqualTo(PermissionRange.FIELD_PROJECT_ID, projectId);
        criteria.andEqualTo(PermissionRange.FIELD_TARGET_TYPE, searchVO.getTargetType());
        criteria.andEqualTo(PermissionRange.FIELD_TARGET_VALUE, searchVO.getTargetValue());
        List<PermissionRange> result = selectByCondition(condition);
        assemblyRangeData(organizationId, result);
        return result;
    }

    @Override
    public void remove(Long organizationId, Long projectId, Long targetValue) {
        permissionRangeMapper.removeByTarget(organizationId,
                projectId == null ? PermissionConstants.EMPTY_ID_PLACEHOLDER : projectId,
                PermissionConstants.PermissionTargetType.OBJECT_SETTING_TARGET_TYPES,
                targetValue);
    }

    @Override
    public List<PermissionRange> selectFolderAndFileByTargetValues(Long organizationId, Long projectId, Set<PermissionConstants.PermissionTargetType> resourceTargetTypes, Set<String> workspaceIds) {

        Condition condition = getCondition();
        Condition.Criteria criteria = condition.createCriteria();
        criteria.andEqualTo(PermissionRange.FIELD_ORGANIZATION_ID, organizationId);
        criteria.andEqualTo(PermissionRange.FIELD_PROJECT_ID, projectId);
        criteria.andIn(PermissionRange.FIELD_TARGET_TYPE, resourceTargetTypes);
        criteria.andIn(PermissionRange.FIELD_TARGET_VALUE, workspaceIds);
        List<PermissionRange> select = selectByCondition(condition);
        assemblyRangeData(organizationId, select);
        return select;
    }

    @Override
    public List<PermissionRange> queryByUser(Long organizationId,
                                             Long projectId,
                                             String targetType,
                                             Long targetValue,
                                             UserInfo userInfo) {
        return permissionRangeMapper.queryByUser(organizationId, projectId, targetType, targetValue, userInfo);
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
        List<PermissionRange> publicRangeList = this.select(publicRange);
        if (!publicRangeList.isEmpty()) {
            return true;
        }
        List<PermissionRange> permissionRangeList =
                this.queryByUser(organizationId, projectId, targetType, baseId, userInfo);
        return !permissionRangeList.isEmpty();
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
            WorkSpaceDTO workSpace = workSpaceRepository.selectByPrimaryKey(searchVO.getTargetValue());
            Assert.notNull(workSpace, BaseConstants.ErrorCode.DATA_NOT_EXISTS);
            // 查询继承自知识库的权限
            final Long knowledgeBaseId = workSpace.getBaseId();
            kbRanges = queryPermissionRangesInheritFromKnowledgeBase(organizationId, projectId, knowledgeBaseId);
            final KnowledgeBaseDTO knowledgeBase = this.knowledgeBaseRepository.selectByPrimaryKey(knowledgeBaseId);
            for (PermissionRange kbRange : kbRanges) {
                kbRange.setTargetName(Optional.ofNullable(knowledgeBase).map(KnowledgeBaseDTO::getName).orElse(null));
            }
            // 如果父级是不是知识库，要查询继承自父级的文件和文件夹权限
            if (!Objects.equals(workSpace.getParentId(), PermissionConstants.EMPTY_ID_PLACEHOLDER)) {
                wsRanges = queryPermissionRangesInheritFromParent(organizationId, projectId, searchVO, workSpace.getRoute());
                if(CollectionUtils.isNotEmpty(wsRanges)) {
                    final Set<Long> targetIds = wsRanges.stream().map(PermissionRange::getTargetValue).collect(Collectors.toSet());
                    List<WorkSpaceDTO> workSpaces = this.workSpaceRepository.selectWorkSpaceNameByIds(targetIds);
                    final Map<Long, WorkSpaceDTO> idToWorkSpaceMap = workSpaces.stream().collect(Collectors.toMap(WorkSpaceDTO::getId, Function.identity()));
                    for (PermissionRange wsRange : wsRanges) {
                        wsRange.setTargetName(
                                Optional.ofNullable(idToWorkSpaceMap.get(wsRange.getTargetValue())).map(WorkSpaceDTO::getName).orElse(null)
                        );
                    }
                }
            }
        }
        List<PermissionRange> selfCollaborators = this.queryObjectSettingCollaborator(organizationId, projectId, searchVO);
        // 按是否是所有者分组
        Map<Boolean, List<PermissionRange>> isOwnerGroup = selfCollaborators.stream()
                // 将自身的权限标记未非继承
                .peek(pr -> pr.setInheritFlag(Boolean.FALSE))
                // 分组
                .collect(Collectors.partitioningBy(pr -> Boolean.TRUE.equals(pr.getOwnerFlag())));
        // 所有者在最前面
        List<PermissionRange> results = Lists.newArrayList(isOwnerGroup.get(true));
        // 然后是知识库继承
        Optional.ofNullable(kbRanges)
                // 标记为继承
                .map(ranges -> ranges.stream().peek(range -> range.setInheritFlag(Boolean.TRUE)).collect(Collectors.toList()))
                .ifPresent(results::addAll);
        // 再是上级继承
        Optional.ofNullable(wsRanges)
                // 标记为继承
                .map(ranges -> ranges.stream().peek(range -> range.setInheritFlag(Boolean.TRUE)).collect(Collectors.toList()))
                .ifPresent(results::addAll);
        // 最后是自己编辑的
        Optional.ofNullable(isOwnerGroup.get(false)).ifPresent(results::addAll);
        return results;
    }

    /**
     * 查询继承自知识库的权限
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @param knowledgeBaseId   知识库ID
     * @return                  查询结果
     */
    private List<PermissionRange> queryPermissionRangesInheritFromKnowledgeBase(Long organizationId, Long projectId, Long knowledgeBaseId) {
        PermissionSearchVO kbSearchVO = new PermissionSearchVO();
        kbSearchVO.setTargetType(PermissionConstants.PermissionTargetType.getKBTargetType(projectId).getCode());
        kbSearchVO.setTargetValue(knowledgeBaseId);
        return this.queryObjectSettingCollaborator(organizationId, projectId, kbSearchVO);
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
        wsRanges = this.selectFolderAndFileByTargetValues(organizationId, projectId, resourceTargetTypes, workspaceIds);
        return wsRanges;
    }

}
