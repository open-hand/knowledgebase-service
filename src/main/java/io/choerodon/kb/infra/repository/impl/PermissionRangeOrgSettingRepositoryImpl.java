package io.choerodon.kb.infra.repository.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.repository.PermissionRangeTenantSettingRepository;
import io.choerodon.kb.infra.enums.PermissionRangeTargetType;

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
        criteria.andIn(PermissionRange.FIELD_TARGET_TYPE, PermissionRangeTargetType.CREATE_SETTING_TYPES);
        return selectByCondition(condition);
    }
}
