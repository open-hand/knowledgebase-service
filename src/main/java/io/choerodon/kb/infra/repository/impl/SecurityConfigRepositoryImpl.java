package io.choerodon.kb.infra.repository.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import io.choerodon.kb.api.vo.permission.PermissionSearchVO;
import io.choerodon.kb.domain.entity.SecurityConfig;
import io.choerodon.kb.domain.repository.SecurityConfigRepository;
import io.choerodon.kb.infra.mapper.SecurityConfigMapper;

import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.hzero.mybatis.domian.Condition;

/**
 * 知识库安全设置 资源库实现
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
@Repository
public class SecurityConfigRepositoryImpl extends BaseRepositoryImpl<SecurityConfig> implements SecurityConfigRepository {

    @Autowired
    private SecurityConfigMapper securityConfigMapper;

    @Override
    public List<SecurityConfig> selectByTarget(Long organizationId, Long projectId, PermissionSearchVO searchVO) {
        Condition condition = getCondition();
        Condition.Criteria criteria = condition.createCriteria();
        criteria.andEqualTo(SecurityConfig.FIELD_ORGANIZATION_ID, organizationId);
        criteria.andEqualTo(SecurityConfig.FIELD_PROJECT_ID, projectId);
        criteria.andEqualTo(SecurityConfig.FIELD_TARGET_TYPE, searchVO.getTargetType());
        criteria.andEqualTo(SecurityConfig.FIELD_TARGET_VALUE, searchVO.getTargetValue());
        return selectByCondition(condition);
    }
}
