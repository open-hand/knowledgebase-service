package io.choerodon.kb.infra.repository.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import io.choerodon.kb.api.vo.permission.PermissionSearchVO;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.repository.PermissionRangeKnowledgeObjectSettingRepository;

import org.hzero.mybatis.domian.Condition;

/**
 * 权限范围知识对象设置 领域资源库实现
 *
 * @author zongqi.hao@zknow.com 2022-09-23
 */
@Repository
public class PermissionRangeKnowledgeObjectSettingRepositoryImpl extends PermissionRangeBaseRepositoryImpl implements PermissionRangeKnowledgeObjectSettingRepository {
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

//        Condition condition = getCondition();
//        Condition.Criteria criteria = condition.createCriteria();
//        criteria.andEqualTo(PermissionRange.FIELD_ORGANIZATION_ID, organizationId);
//        criteria.andEqualTo(PermissionRange.FIELD_PROJECT_ID, projectId != null ? projectId : BaseConstants.Digital.ZERO);
//        criteria.andIn(PermissionRange.FIELD_TARGET_TYPE, PermissionConstants.PermissionTargetType.WORKSPACE_AND_BASE_TARGET_TYPES);
//        criteria.andEqualTo(PermissionRange.FIELD_TARGET_VALUE, targetValue);
//        deleteOptional(new PermissionRange(), criteria);

    }
}
