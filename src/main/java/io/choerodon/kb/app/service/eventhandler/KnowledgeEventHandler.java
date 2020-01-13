package io.choerodon.kb.app.service.eventhandler;

import java.util.Arrays;
import com.alibaba.fastjson.JSONObject;
import io.choerodon.asgard.saga.annotation.SagaTask;
import io.choerodon.kb.api.vo.KnowledgeBaseInfoVO;
import io.choerodon.kb.api.vo.ProjectDTO;
import io.choerodon.kb.api.vo.event.OrganizationCreateEventPayload;
import io.choerodon.kb.api.vo.event.ProjectEvent;
import io.choerodon.kb.app.service.KnowledgeBaseService;
import io.choerodon.kb.infra.feign.BaseFeignClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zhaotianxin
 * @since 2020/1/13
 */
@Component
public class KnowledgeEventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(KnowledgeEventHandler.class);
    private static final String PROJECT_CREATE = "iam-create-project";
    private static final String TASK_PROJECT_CREATE = "knowledgeBase-create-project";
    private static final String ORG_CREATE = "org-create-organization";
    private static final String TASK_ORG_CREATE = "knowledgeBase-create-organization";
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;
    @Autowired
    private BaseFeignClient baseFeignClient;

    @SagaTask(code = TASK_ORG_CREATE,
            description = "knowledge_base消费创建组织",
            sagaCode = ORG_CREATE, seq = 1)
    public String handleOrganizationCreateByConsumeSagaTask(String data) {
        LOGGER.info("消费创建组织消息{}", data);
        OrganizationCreateEventPayload organizationEventPayload = JSONObject.parseObject(data, OrganizationCreateEventPayload.class);
        KnowledgeBaseInfoVO knowledgeBaseInfoVO = new KnowledgeBaseInfoVO();
        knowledgeBaseInfoVO.setName(organizationEventPayload.getName());
        knowledgeBaseInfoVO.setOpenRange("range_private");
        knowledgeBaseInfoVO.setDescription("组织下默认知识库");
        knowledgeBaseService.create(organizationEventPayload.getId(),null,knowledgeBaseInfoVO);
        return data;
    }

    /**
     * 创建项目事件
     *
     * @param message message
     */
    @SagaTask(code = TASK_PROJECT_CREATE,
            description = "knowledge_base消费创建项目事件初始化项目数据",
            sagaCode = PROJECT_CREATE,
            seq = 2)
    public String handleProjectInitByConsumeSagaTask(String message) {
        ProjectEvent projectEvent = JSONObject.parseObject(message, ProjectEvent.class);
        ProjectDTO project = baseFeignClient.queryProject(projectEvent.getProjectId()).getBody();
        KnowledgeBaseInfoVO knowledgeBaseInfoVO = new KnowledgeBaseInfoVO();
        knowledgeBaseInfoVO.setName(projectEvent.getProjectName());
        knowledgeBaseInfoVO.setOpenRange("range_private");
        knowledgeBaseInfoVO.setDescription("项目下默认知识库");
        knowledgeBaseService.create(project.getOrganizationId(),projectEvent.getProjectId(),knowledgeBaseInfoVO);
        return message;
    }
}
