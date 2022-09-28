package io.choerodon.kb.domain.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.repository.PermissionRangeKnowledgeObjectSettingRepository;
import io.choerodon.kb.infra.enums.PermissionConstants;

import org.hzero.core.util.Pair;

public abstract class PermissionRangeBaseDomainServiceImpl {

    @Autowired
    // 这里至少要继承自PermissionRangeBaseRepository的Bean都行, 就随便挑了一个 gaokuo.dai@zknow.com 2022-09-28
    private PermissionRangeKnowledgeObjectSettingRepository permissionRangeKnowledgeObjectSettingRepository;


    /**
     * todo 需要转换targetType
     */
    protected PermissionDetailVO commonSave(
            Long organizationId,
            Long projectId,
            PermissionDetailVO permissionDetailVO
    ) {
        String targetType = permissionDetailVO.getTargetType();
        Long targetValue = permissionDetailVO.getTargetValue();
        if (organizationId == null) {
            organizationId = PermissionConstants.EMPTY_ID_PLACEHOLDER;
        }
        if (projectId == null) {
            projectId = PermissionConstants.EMPTY_ID_PLACEHOLDER;
        }
        List<PermissionRange> permissionRanges = permissionDetailVO.getPermissionRanges();
        if (permissionRanges == null) {
            permissionRanges = Collections.emptyList();
        }
        Pair<List<PermissionRange>, List<PermissionRange>> pair =
                processAddAndDeleteList(organizationId, projectId, targetType, targetValue, permissionRanges);
        List<PermissionRange> addList = pair.getFirst();
        List<PermissionRange> deleteList = pair.getSecond();
        if (CollectionUtils.isNotEmpty(deleteList)) {
            permissionRangeKnowledgeObjectSettingRepository.batchDeleteByPrimaryKey(deleteList);
        }
        if(CollectionUtils.isNotEmpty(addList)) {
            permissionRangeKnowledgeObjectSettingRepository.batchInsert(addList);
        }
        return permissionDetailVO;
    }

    private Pair<List<PermissionRange>, List<PermissionRange>> processAddAndDeleteList(
            Long organizationId,
            Long projectId,
            String targetType,
            Long targetValue,
            List<PermissionRange> permissionRanges
    ) {
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
                permissionRange.setId(null);
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
