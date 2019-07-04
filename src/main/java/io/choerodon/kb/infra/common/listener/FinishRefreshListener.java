package io.choerodon.kb.infra.common.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * @author shinan.chen
 * @since 2019/7/4
 */
@Component
public class FinishRefreshListener implements ApplicationListener<ContextRefreshedEvent> {
    /**
     * 接收到ContextRefreshedEvent事件
     *
     * @param event
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        System.out.println("FinishRefreshListener接收到event：" + event);
    }
}