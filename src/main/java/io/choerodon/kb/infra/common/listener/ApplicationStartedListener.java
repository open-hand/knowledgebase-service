package io.choerodon.kb.infra.common.listener;

import io.choerodon.kb.api.dao.PageSyncDTO;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.common.utils.EsRestUtil;
import io.choerodon.kb.infra.mapper.PageMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author shinan.chen
 * @since 2019/7/4
 */
@Component
public class ApplicationStartedListener implements ApplicationListener<ApplicationStartedEvent> {

    public static final Logger LOGGER = LoggerFactory.getLogger(ApplicationStartedListener.class);
    @Autowired
    private EsRestUtil esRestUtil;
    @Autowired
    private PageMapper pageMapper;

    /**
     * 接收到ContextRefreshedEvent事件
     *
     * @param event
     */
    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        LOGGER.info("ApplicationStartedListener:{}", event.toString());
        //判断是否存在index，否则创建
        if (!esRestUtil.indexExist(BaseStage.ES_PAGE_INDEX)) {
            esRestUtil.createIndex(BaseStage.ES_PAGE_INDEX);
        }
        //批量同步mysql数据到es中
        List<PageSyncDTO> pages = pageMapper.querySync2EsPage();
        if (!pages.isEmpty()) {
            LOGGER.info("ApplicationStartedListener,sync page count:{}", pages.size());
            esRestUtil.batchCreatePage(BaseStage.ES_PAGE_INDEX, pages);
        } else {
            LOGGER.info("ApplicationStartedListener,There is no page needs to be synchronized");
        }
    }
}