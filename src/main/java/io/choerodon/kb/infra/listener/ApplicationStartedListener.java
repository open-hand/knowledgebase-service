package io.choerodon.kb.infra.listener;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.choerodon.kb.api.vo.PageSyncVO;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.mapper.PageMapper;
import io.choerodon.kb.infra.utils.EsRestUtil;

/**
 * @author shinan.chen
 * @since 2019/7/4
 */
@Component
public class ApplicationStartedListener implements ApplicationListener<ApplicationStartedEvent> {

    public static final Logger LOGGER = LoggerFactory.getLogger(io.choerodon.kb.infra.listener.ApplicationStartedListener.class);
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
        List<PageSyncVO> pages;
        //判断是否存在index
        if (!esRestUtil.indexExist(BaseStage.ES_PAGE_INDEX)) {
            //不存在，则创建index，并批量同步所有数据
            esRestUtil.createIndex(BaseStage.ES_PAGE_INDEX);
            pages = pageMapper.querySync2EsPage(null);
        } else {
            //存在，则批量同步未被同步的数据
            pages = pageMapper.querySync2EsPage(false);
        }
        if (!pages.isEmpty()) {
            LOGGER.info("ApplicationStartedListener,sync page count:{}", pages.size());
            esRestUtil.batchCreatePage(BaseStage.ES_PAGE_INDEX, pages);
        } else {
            LOGGER.info("ApplicationStartedListener,There is no page needs to be synchronized");
        }
    }
}
