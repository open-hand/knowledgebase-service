package io.choerodon.kb.app.service;

import io.choerodon.kb.infra.enums.PermissionConstants;

/**
 * @author superlee
 * @since 2022-10-11
 */
public interface PermissionRefreshCacheService {

    /**
     * 根据type刷新redis缓存
     *
     * @param type 类型{@link PermissionConstants.PermissionRefreshType}
     */
    void refreshCache(PermissionConstants.PermissionRefreshType refreshType);
    void refreshCache(String refreshType);
}
