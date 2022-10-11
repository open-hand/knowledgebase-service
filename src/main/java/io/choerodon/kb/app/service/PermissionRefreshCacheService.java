package io.choerodon.kb.app.service;

/**
 * @author superlee
 * @since 2022-10-11
 */
public interface PermissionRefreshCacheService {

    /**
     * 根据type刷新redis缓存
     *
     * @param type 类型{@link io.choerodon.kb.infra.enums.PermissionRefreshType}
     */
    void refreshCache(String type);
}
