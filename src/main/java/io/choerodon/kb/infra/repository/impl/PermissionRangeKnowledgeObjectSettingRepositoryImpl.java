package io.choerodon.kb.infra.repository.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import io.choerodon.kb.api.vo.permission.PermissionSearchVO;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.entity.UserInfo;
import io.choerodon.kb.domain.repository.PermissionRangeKnowledgeObjectSettingRepository;
import io.choerodon.kb.infra.enums.PermissionConstants;
import io.choerodon.kb.infra.mapper.PermissionRangeMapper;

import org.hzero.mybatis.domian.Condition;

/**
 * 权限范围知识对象设置 领域资源库实现
 *
 * @author zongqi.hao@zknow.com 2022-09-23
 */
@Repository
public class PermissionRangeKnowledgeObjectSettingRepositoryImpl extends PermissionRangeBaseRepositoryImpl implements PermissionRangeKnowledgeObjectSettingRepository {

    @Autowired
    private PermissionRangeMapper permissionRangeMapper;

    @Override
    public List<PermissionRange> queryObjectSettingCollaborator(Long organizationId, Long projectId, PermissionSearchVO searchVO) {
        Condition condition = getCondition();
        Condition.Criteria criteria = condition.createCriteria();
        criteria.andEqualTo(PermissionRange.FIELD_ORGANIZATION_ID, organizationId);
        criteria.andEqualTo(PermissionRange.FIELD_PROJECT_ID, projectId);
        criteria.andEqualTo(PermissionRange.FIELD_TARGET_TYPE, searchVO.getTargetType());
        criteria.andEqualTo(PermissionRange.FIELD_TARGET_VALUE, searchVO.getTargetValue());
        List<PermissionRange> select = selectByCondition(condition);
        assemblyRangeData(organizationId, select);
        return select;
    }

    @Override
    public void clear(Long organizationId, Long projectId, Long targetValue) {
        permissionRangeMapper.clearByTarget(organizationId,
                projectId == null ? 0L : projectId,
                PermissionConstants.PermissionTargetType.OBJECT_SETTING_TARGET_TYPES,
                targetValue);
    }

    @Override
    public List<PermissionRange> selectFolderAndFileByTargetValues(Long organizationId, Long projectId, HashSet<PermissionConstants.PermissionTargetType> resourceTargetTypes, Set<String> workspaceIds) {

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
}
