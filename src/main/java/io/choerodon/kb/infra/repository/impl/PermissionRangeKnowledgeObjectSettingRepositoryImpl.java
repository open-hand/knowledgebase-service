package io.choerodon.kb.infra.repository.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import io.choerodon.kb.api.vo.permission.PermissionSearchVO;
import io.choerodon.kb.domain.entity.PermissionRange;
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
}
