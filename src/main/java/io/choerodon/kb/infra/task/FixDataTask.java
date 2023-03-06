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

    @JobTask(maxRetryCount = 3,
            code = "fixRouteAndPermission",
            description = "2.2.0知识库路由错误修复和权限修复")
    @TimedTask(name = "fixRouteAndPermission",
            description = "2.2.0知识库路由错误修复和权限修复",
            oneExecution = true,
            repeatCount = 0,
            repeatInterval = 1,
            repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.HOURS,
            params = {})
    public void fixRouteAndPermission(Map<String, Object> map) {
        LOGGER.info("======================开始修复v2.2.0知识库路由和权限=====================");
        dataFixService.fixWorkspaceRoute();
        dataFixService.fixPermission();
        LOGGER.info("======================v2.2.0知识库路由和权限修复完成=====================");
    }


    // TODO: 2023/3/1 写入好逻辑后才放开  合并到2.3要加productSource = ZKnowDetailsHelper.VALUE_CHOERODON
    @JobTask(maxRetryCount = 3,
            code = "fixWorkSpaceTemplate",
            description = "2.3修复知识库模板数据")
    @TimedTask(name = "fixWorkSpaceTemplate",
            description = "2.3修复知识库模板数据",
            oneExecution = true,
            repeatCount = 0,
            repeatInterval = 1,
            repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.HOURS,
            params = {})
    public void fixWorkSpaceTemplate(Map<String, Object> map) {
        LOGGER.info("======================开始修复2.3知识库模板数据=====================");
        dataFixService.fixWorkSpaceTemplate();
        LOGGER.info("======================2.3修复知识库模板数据修复完成=====================");
    }
}
