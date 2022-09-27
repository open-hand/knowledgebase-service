package io.choerodon.kb.infra.repository.impl;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Repository;

import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.repository.PermissionRangeKnowledgeObjectSettingRepository;

import org.hzero.mybatis.domian.Condition;

/**
 * 权限范围知识对象设置 领域资源库实现
 * @author zongqi.hao@zknow.com 2022-09-23
 */
@Repository
public class PermissionRangeKnowledgeObjectSettingRepositoryImpl extends PermissionRangeBaseRepositoryImpl implements PermissionRangeKnowledgeObjectSettingRepository {
    @Override
    public List<PermissionRange> queryFolderOrFileCollaborator(Long organizationId, Long projectId, Set<String> targetTypes, Long targetValue) {
        Condition condition = getCondition();
        Condition.Criteria criteria = condition.createCriteria();
        criteria.andEqualTo(PermissionRange.FIELD_ORGANIZATION_ID, organizationId);
        criteria.andEqualTo(PermissionRange.FIELD_PROJECT_ID, projectId);
        criteria.andIn(PermissionRange.FIELD_TARGET_TYPE, targetTypes);
        criteria.andEqualTo(PermissionRange.FIELD_TARGET_VALUE, targetValue);
        List<PermissionRange> select = selectByCondition(condition);
        assemblyRangeData(organizationId, select);
        return select;
    }
}
