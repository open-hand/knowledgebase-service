package io.choerodon.kb.infra.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import io.choerodon.kb.domain.entity.SecurityConfig;
import io.choerodon.kb.domain.repository.SecurityConfigRepository;
import io.choerodon.kb.infra.mapper.SecurityConfigMapper;

import org.hzero.mybatis.base.impl.BaseRepositoryImpl;

/**
 * 知识库安全设置 资源库实现
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
@Repository
public class SecurityConfigRepositoryImpl extends BaseRepositoryImpl<SecurityConfig> implements SecurityConfigRepository {

    @Autowired
    private SecurityConfigMapper securityConfigMapper;

}
