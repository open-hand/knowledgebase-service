package io.choerodon.kb.domain.service;

import io.choerodon.kb.infra.enums.PermissionConstants;

/**
 * @author superlee
 * @since 2022-10-11
 */
public interface PermissionRefreshCacheDomainService {

    /**
     * 根据type刷新redis缓存
     *
     * @param refreshType 类型{@link PermissionConstants.PermissionRefreshType}
     */
    void refreshCache(PermissionConstants.PermissionRefreshType refreshType);
    void refreshCache(String refreshType);
}
