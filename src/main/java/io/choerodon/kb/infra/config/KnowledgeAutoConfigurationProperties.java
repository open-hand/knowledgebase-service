package io.choerodon.kb.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = KnowledgeAutoConfigurationProperties.PREFIX)
public class KnowledgeAutoConfigurationProperties {
    public static final String PREFIX = "knowledge";

    /**
     * 项目启动时是否初始化缓存
     */
    private boolean initCache = true;

    /**
     * @return 项目启动时是否初始化缓存
     */
    public boolean isInitCache() {
        return initCache;
    }

    public void setInitCache(boolean initCache) {
        this.initCache = initCache;
    }
}
