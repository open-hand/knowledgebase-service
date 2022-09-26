package io.choerodon.kb.infra.repository.impl;

import java.util.List;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Repository;

import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.repository.PermissionRangeTenantSettingRepository;
import io.choerodon.kb.infra.enums.PermissionRangeType;
import io.choerodon.kb.infra.enums.PermissionTargetType;

import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.hzero.mybatis.domian.Condition;

/**
 * Copyright (c) 2022. Zknow Enterprise Solution. All right reserved.
 *
 * @author zongqi.hao@zknow.com
 * @since 2022/9/23
 */
@Repository
public class PermissionRangeOrgSettingRepositoryImpl extends BaseRepositoryImpl<PermissionRange> implements PermissionRangeTenantSettingRepository {

    @Override
    public List<PermissionRange> selectOrgSetting(Long organizationId) {
        Condition condition = getCondition();
        Condition.Criteria criteria = condition.createCriteria();
        criteria.andEqualTo(PermissionRange.FIELD_ORGANIZATION_ID, organizationId);
        criteria.andIn(PermissionRange.FIELD_TARGET_TYPE, PermissionTargetType.CREATE_SETTING_TYPES);
        return selectByCondition(condition);
    }

    @Override
    public void initSetting(Long organizationId, List<PermissionRange> defaultRanges) {
        List<PermissionRange> initData = getInitData(organizationId);
        initData.addAll(defaultRanges);
        batchInsertSelective(initData);
    }

    private List<PermissionRange> getInitData(Long orgId) {
        return Lists.newArrayList(
                // 组织层创建默认为组织管理员
                PermissionRange.of(orgId, 0L, PermissionTargetType.KNOWLEDGE_CREATE_ORG.name(), 0L, PermissionRangeType.MANAGER.name(), 0L, "NULL"),
                // 项目层创建默认为项目成员
                PermissionRange.of(orgId, 0L, PermissionTargetType.KNOWLEDGE_CREATE_PROJECT.name(), 0L, PermissionRangeType.MEMBER.name(), 0L, "NULL")
        );
    }
}
