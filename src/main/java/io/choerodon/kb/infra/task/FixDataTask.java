//package io.choerodon.kb.infra.task;
//
//import io.choerodon.asgard.schedule.QuartzDefinition;
//import io.choerodon.asgard.schedule.annotation.JobTask;
//import io.choerodon.asgard.schedule.annotation.TimedTask;
//import io.choerodon.kb.app.service.DataFixService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.Map;
//
///**
// * @author zhaotianxin
// * @date 2020-02-20 20:37
// */
//@Component
//public class FixDataTask {
//    @Autowired
//    private DataFixService dataFixService;
//
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
//}
