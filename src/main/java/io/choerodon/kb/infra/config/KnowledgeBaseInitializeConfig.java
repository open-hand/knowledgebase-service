package io.choerodon.kb.infra.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.choerodon.kb.app.service.WorkSpaceService;
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
    private EsRestUtil esRestUtil;

    @Autowired
    private WorkSpaceService workSpaceService;


    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 加入消息文件
        MessageAccessor.addBasenames("classpath:messages/messages");
        MessageAccessor.addBasenames("classpath:messages/permission");
        loadDocToElasticsearch();
        workSpaceService.reloadTargetParentMappingToRedis();
    }

    private void loadDocToElasticsearch() {
        try {
            esRestUtil.manualSyncPageData2Es();
        } catch (Throwable throwable) {
            logger.error("加载文档数据到elasticsearch失败，异常：{}", throwable);
        }
    }
}
