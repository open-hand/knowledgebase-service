package io.choerodon.kb.infra.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import org.hzero.core.message.MessageAccessor;

/**
 * 初始化配置
 *
 * @author gaokuo.dai@zknow.com 2022-09-26
 */
@Component
public class KnowledgeBaseInitializeConfig implements ApplicationListener<ApplicationReadyEvent> {


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
    }
}
