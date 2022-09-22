package io.choerodon.kb.infra.task;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.asgard.schedule.QuartzDefinition;
import io.choerodon.asgard.schedule.annotation.JobTask;
import io.choerodon.asgard.schedule.annotation.TimedTask;
import io.choerodon.kb.app.service.DataFixService;

@Component
public class FixDataTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(FixDataTask.class);

    @Autowired
    private DataFixService dataFixService;

    //    @JobTask(maxRetryCount = 3,
//            code = "fixKnowledgeBaseData021",
//            description = "升级到0.21.0,修复知识管理服务数据")
//    @TimedTask(name = "fixKnowledgeBaseData021",
//            description = "升级到0.21.0,修复知识管理服务数据",
//            oneExecution = true,
//            repeatCount = 0,
//            repeatInterval = 1,
//            repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.HOURS,
//            params = {})
//    public void fixKnowledgeBaseData021(Map<String, Object> map) {
//        dataFixService.fixData();
//    }

    @JobTask(maxRetryCount = 3,
            code = "fixKnowledgeWorkspaceRoute",
            description = "知识库路由错误修复")
    @TimedTask(name = "fixKnowledgeWorkspaceRoute",
            description = "知识库路由错误修复",
            oneExecution = true,
            repeatCount = 0,
            repeatInterval = 1,
            repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.HOURS,
            params = {})
    public void fixWorkspaceRoute(Map<String, Object> map) {
        LOGGER.info("======================进行知识库路由错误数据修复=====================");
        dataFixService.fixWorkspaceRoute();
        LOGGER.info("======================进行知识库路由错误数据完成=====================");
    }
}
