package io.choerodon.kb.infra.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.choerodon.kb.domain.service.PermissionRefreshCacheDomainService;
import io.choerodon.kb.infra.enums.PermissionConstants;
import io.choerodon.kb.infra.utils.EsRestUtil;

import org.hzero.core.message.MessageAccessor;

/**
 * 初始化配置
 *
 * @author gaokuo.dai@zknow.com 2022-09-26
 */
@Component
public class KnowledgeBaseInitializeConfig implements ApplicationListener<ApplicationReadyEvent> {

    public static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseInitializeConfig.class);

    @Autowired
    private KnowledgeAutoConfigurationProperties properties;
    @Autowired
    private EsRestUtil esRestUtil;

    @Autowired
    private PermissionRefreshCacheDomainService permissionRefreshCacheDomainService;


    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        this.loadMessageI18N();
        if(properties.isInitCache()) {
            this.loadDocToElasticsearch();
            this.loadPermissionCache();
        }
    }

    /**
     * 加载权限缓存
     */
    private void loadPermissionCache() {
        this.permissionRefreshCacheDomainService.refreshCache(PermissionConstants.PermissionRefreshType.ROLE_CONFIG);
        this.permissionRefreshCacheDomainService.refreshCache(PermissionConstants.PermissionRefreshType.RANGE);
        this.permissionRefreshCacheDomainService.refreshCache(PermissionConstants.PermissionRefreshType.SECURITY_CONFIG);
        this.permissionRefreshCacheDomainService.refreshCache(PermissionConstants.PermissionRefreshType.TARGET_PARENT);
    }

    /**
     * 加载消息多语言文件
     */
    private void loadMessageI18N() {
        // 加入消息文件
        MessageAccessor.addBasenames("classpath:messages/messages");
        MessageAccessor.addBasenames("classpath:messages/permission");
    }

    /**
     * 加载ES搜索数据, 异步, 忽略异常
     */
    private void loadDocToElasticsearch() {
        new Thread(() -> {
            try {
                esRestUtil.manualSyncPageData2Es();
            } catch (Throwable throwable) {
                logger.warn("加载文档数据到elasticsearch失败");
                logger.warn(throwable.getMessage(), throwable);
            }
        }).start();
    }
}
