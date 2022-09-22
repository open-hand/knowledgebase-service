package io.choerodon.kb.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.kb.app.service.SecurityConfigService;
import io.choerodon.kb.domain.entity.SecurityConfig;
import io.choerodon.kb.domain.repository.SecurityConfigRepository;

import org.hzero.core.base.BaseAppService;
import org.hzero.mybatis.helper.SecurityTokenHelper;

/**
 * 知识库安全设置应用服务默认实现
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
@Service
public class SecurityConfigServiceImpl extends BaseAppService implements SecurityConfigService {

    @Autowired
    private SecurityConfigRepository securityConfigRepository;

    @Override
    public SecurityConfig create(Long tenantId, SecurityConfig securityConfig) {
        validObject(securityConfig);
        securityConfigRepository.insertSelective(securityConfig);
        return securityConfig;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SecurityConfig update(Long tenantId, SecurityConfig securityConfig) {
        SecurityTokenHelper.validToken(securityConfig);
        securityConfigRepository.updateByPrimaryKeySelective(securityConfig);
        return securityConfig;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(SecurityConfig securityConfig) {
        SecurityTokenHelper.validToken(securityConfig);
        securityConfigRepository.deleteByPrimaryKey(securityConfig);
    }
}
