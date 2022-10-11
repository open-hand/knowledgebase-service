package io.choerodon.kb.domain.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.repository.KnowledgeBaseRepository;
import io.choerodon.kb.domain.repository.PermissionRangeKnowledgeObjectSettingRepository;
import io.choerodon.kb.domain.repository.WorkSpaceRepository;
import io.choerodon.kb.infra.dto.KnowledgeBaseDTO;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.enums.PermissionConstants;
import io.choerodon.mybatis.domain.AuditDomain;

import org.hzero.core.base.BaseConstants;
import org.hzero.core.util.Pair;
import org.hzero.mybatis.common.Criteria;

public abstract class PermissionRangeBaseDomainServiceImpl {

    @Autowired
    private KnowledgeBaseRepository knowledgeBaseRepository;
    @Autowired
    private WorkSpaceRepository workSpaceRepository;

    @Autowired
    // 这里只要继承自PermissionRangeBaseRepository的Bean都行, 就随便挑了一个 gaokuo.dai@zknow.com 2022-09-28
    private PermissionRangeKnowledgeObjectSettingRepository permissionRangeKnowledgeObjectSettingRepository;

    /**
     * 权限范围通用保存<br/>
     * 根据permissionDetailVO的targetType+targetValue<br/>
     * 自动比对与数据库中的permissionRanges的差异<br/>
     * 进而自动进行差异数据的create和delete<br/>
     * 注意: 进入此方法的默认已将baseTargetType转换为targetType
     * @param organizationId        组织ID
     * @param projectId             项目ID
     * @param permissionDetail      permissionDetail
     * @return 处理后的数据
     */
    protected PermissionDetailVO commonSave(
            Long organizationId,
            Long projectId,
            PermissionDetailVO permissionDetail
    ) {
        // 准备数据
        String targetType = permissionDetail.getTargetType();
        Long targetValue = permissionDetail.getTargetValue();
        if (organizationId == null) {
            organizationId = PermissionConstants.EMPTY_ID_PLACEHOLDER;
        }
        if (projectId == null) {
            projectId = PermissionConstants.EMPTY_ID_PLACEHOLDER;
        }
        fillInSourceAndTarget(organizationId, projectId, permissionDetail, targetValue);
        final Long ownerUserId = this.findOwnerUserId(targetType, targetValue);
        // 确保数据中存在所有者
        final List<PermissionRange> permissionRanges = this.makeSureInputDataContainOwner(
                organizationId,
                projectId,
                targetType,
                targetValue,
                permissionDetail.getPermissionRanges(),
                ownerUserId
        );
        // 与数据库中的数据进行对比, 计算差异
        Pair<List<PermissionRange>, List<PermissionRange>> pair = calculateAddAndDeleteList(
                organizationId,
                projectId,
                targetType,
                targetValue,
                permissionRanges
        );
        // 处理差异
        List<PermissionRange> addList = pair.getFirst();
        List<PermissionRange> deleteList = pair.getSecond();
        // -- 注意先删后插避免UK冲突
        if (CollectionUtils.isNotEmpty(deleteList)) {
            this.permissionRangeKnowledgeObjectSettingRepository.batchDeleteByPrimaryKey(deleteList);
        }
        if (CollectionUtils.isNotEmpty(addList)) {
            for (PermissionRange permissionRange : addList) {
                // 确保所有者标识有值
                if(permissionRange.getOwnerFlag() == null) {
                    permissionRange.setOwnerFlag(Boolean.FALSE);
                }
            }
            this.permissionRangeKnowledgeObjectSettingRepository.batchInsert(addList);
        }

        return permissionDetail;
    }

    private void fillInSourceAndTarget(Long organizationId,
                                       Long projectId,
                                       PermissionDetailVO permissionDetail,
                                       Long targetValue) {
        List<PermissionRange> permissionRanges = permissionDetail.getPermissionRanges();
        if (CollectionUtils.isEmpty(permissionRanges)) {
            return;
        }
        for (PermissionRange permissionRange : permissionRanges) {
            permissionRange.setOrganizationId(organizationId);
            permissionRange.setProjectId(projectId);
            permissionRange.setTargetValue(targetValue);
        }
    }

    /**
     * 根据targetType+targetValue<br/>
     * 自动比对与数据库中的permissionRanges的差异<br/>
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @param targetType        控制对象类型
     * @param targetValue       控制对象
     * @param permissionRanges  待处理的数据
     * @return Pair&lt;addList, deleteList&gt;
     */
    private Pair<List<PermissionRange>, List<PermissionRange>> calculateAddAndDeleteList(
            Long organizationId,
            Long projectId,
            String targetType,
            Long targetValue,
            List<PermissionRange> permissionRanges
    ) {
        // 准备集合
        List<PermissionRange> addList = new ArrayList<>();
        List<PermissionRange> deleteList = new ArrayList<>();
        // 根据targetType+targetValue查询数据库中的现有值
        List<PermissionRange> existedList = permissionRangeKnowledgeObjectSettingRepository.select(PermissionRange.of(
                organizationId,
                projectId,
                targetType,
                targetValue,
                null,
                null,
                null)
        );
        // 计算逻辑:
        // addList = inputList - (inputList ^ dbList)
        // deleteList = dbList - (inputList ^ dbList)
        // 计算交集
        List<PermissionRange> intersection = new ArrayList<>();
        for (PermissionRange permissionRange : permissionRanges) {
            for (PermissionRange existedPermissionRange : existedList) {
                if (permissionRange.equals(existedPermissionRange)) {
                    intersection.add(existedPermissionRange);
                }
            }
        }
        // 计算addList
        for (PermissionRange permissionRange : permissionRanges) {
            if (!intersection.contains(permissionRange)) {
                permissionRange.setOrganizationId(organizationId);
                permissionRange.setProjectId(projectId);
                permissionRange.setId(null);
                permissionRange.setTargetType(targetType);
                permissionRange.setTargetValue(targetValue);
                addList.add(permissionRange);
            }
        }
        // 计算deleteList
        for (PermissionRange permissionRange : existedList) {
            if (!intersection.contains(permissionRange)) {
                deleteList.add(permissionRange);
            }
        }

        return Pair.of(addList, deleteList);
    }

    /**
     * 判断是否需要处理所有者默认权限
     * @param targetType targetType
     * @return 否需要处理所有者默认权限
     */
    private boolean notNeedProcessOwnerPermissionRange(String targetType) {
        Assert.hasText(targetType, BaseConstants.ErrorCode.NOT_NULL);
        // 知识库创建和默认类型不需要, 其他类型均需要
        return PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_SETTING_TARGET_TYPES.contains(targetType);
    }

    /**
     * 根据控制对象获取控制对象创建者ID
     * @param targetType 控制对象类型
     * @param targetValue 控制对象
     * @return 控制对象创建者ID
     */
    private Long findOwnerUserId(
            String targetType,
            Long targetValue
    ) {
        Assert.hasText(targetType, BaseConstants.ErrorCode.NOT_NULL);
        Assert.notNull(targetValue, BaseConstants.ErrorCode.NOT_NULL);
        Long ownerUserId = null;
        // -- 知识库、文件夹、文件的权限保存时需要计算, 其他类型的权限配置不需要
        if(this.notNeedProcessOwnerPermissionRange(targetType)) {
            return ownerUserId;
        }

        if(targetType.startsWith(PermissionConstants.PermissionTargetBaseType.KNOWLEDGE_BASE.toString())) {
            ownerUserId = Optional.ofNullable(this.knowledgeBaseRepository.selectOneOptional(new KnowledgeBaseDTO().setId(targetValue), new Criteria().select(AuditDomain.FIELD_CREATED_BY)))
                    .map(AuditDomain::getCreatedBy)
                    .orElse(null);
        } else if(targetType.startsWith(PermissionConstants.PermissionTargetBaseType.FOLDER.toString()) ||
                targetType.startsWith(PermissionConstants.PermissionTargetBaseType.FILE.toString())) {
            ownerUserId = Optional.ofNullable(this.workSpaceRepository.selectOneOptional(new WorkSpaceDTO().setId(targetValue), new Criteria().select(AuditDomain.FIELD_CREATED_BY)))
                    .map(AuditDomain::getCreatedBy)
                    .orElse(null);
        } else {
            throw new CommonException(BaseConstants.ErrorCode.DATA_INVALID);
        }

        Assert.notNull(ownerUserId, BaseConstants.ErrorCode.DATA_NOT_EXISTS);
        return ownerUserId;
    }

    /**
     * 确保输入数据中包含有拥有者ID
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @param targetType        控制对象类型
     * @param targetValue       控制对象
     * @param inputData         待处理的数据
     * @param ownerId           所有者用户ID
     * @return 处理后的数据
     */
    private List<PermissionRange> makeSureInputDataContainOwner(
            Long organizationId,
            Long projectId,
            String targetType,
            Long targetValue,
            List<PermissionRange> inputData,
            Long ownerId
    ) {
        // -- 知识库、文件夹、文件的权限保存时要求必须传入所有者ID, 其他类型的权限配置不需要
        Assert.hasText(targetType, BaseConstants.ErrorCode.NOT_NULL);
        if(this.notNeedProcessOwnerPermissionRange(targetType)) {
            return inputData;
        }
        // 准备数据
        Assert.notNull(organizationId, BaseConstants.ErrorCode.NOT_NULL);
        Assert.notNull(projectId, BaseConstants.ErrorCode.NOT_NULL);
        Assert.notNull(targetValue, BaseConstants.ErrorCode.NOT_NULL);
        Assert.notNull(ownerId, BaseConstants.ErrorCode.NOT_NULL);
        if(inputData == null) {
            inputData = new ArrayList<>();
        }
        // 构建所有者默认权限
        final PermissionRange ownerPermissionRange = PermissionRange.of(
                organizationId,
                projectId,
                targetType,
                targetValue,
                PermissionConstants.PermissionRangeType.USER.toString(),
                ownerId,
                PermissionConstants.PermissionRole.MANAGER,
                Boolean.TRUE
        );
        // 移除输入数据中, 授权对象是当前所有者但是权限不是MANAGER的数据
        inputData = inputData.stream().filter(pr -> !(
                Objects.equals(pr.getRangeType(), ownerPermissionRange.getRangeType())
                && Objects.equals(pr.getRangeValue(), ownerPermissionRange.getRangeValue())
                && !Objects.equals(pr.getPermissionRoleCode(), ownerPermissionRange.getPermissionRoleCode())
        )).collect(Collectors.toList());
        // 如果输入数据中没有所有者默认权限, 则自动补齐
        if(!inputData.contains(ownerPermissionRange)) {
            inputData.add(ownerPermissionRange);
        }

        return inputData;
    }

}
