package io.choerodon.kb.app.service;

import io.choerodon.kb.domain.entity.SecurityConfig;

/**
 * 知识库安全设置应用服务
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
public interface SecurityConfigService {

    /**
     * 创建知识库安全设置
     *
     * @param tenantId       租户ID
     * @param securityConfig 知识库安全设置
     * @return 知识库安全设置
     */
    SecurityConfig create(Long tenantId, SecurityConfig securityConfig);

    /**
     * 更新知识库安全设置
     *
     * @param tenantId       租户ID
     * @param securityConfig 知识库安全设置
     * @return 知识库安全设置
     */
    SecurityConfig update(Long tenantId, SecurityConfig securityConfig);

    /**
     * 删除知识库安全设置
     *
     * @param securityConfig 知识库安全设置
     */
    void remove(SecurityConfig securityConfig);
}
