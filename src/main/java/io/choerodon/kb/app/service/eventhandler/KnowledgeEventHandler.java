package io.choerodon.kb.app.service.eventhandler;

import com.alibaba.fastjson.JSONObject;
import io.choerodon.asgard.saga.annotation.SagaTask;
import io.choerodon.kb.api.vo.event.OrganizationCreateEventPayload;
import io.choerodon.kb.api.vo.event.ProjectEvent;
import io.choerodon.kb.app.service.KnowledgeBaseService;
import io.choerodon.kb.infra.dto.KnowledgeBaseDTO;
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
    private static final String TASK_PROJECT_CREATE = "kb-create-project";
    private static final String ORG_CREATE = "org-create-organization";
    private static final String TASK_ORG_CREATE = "kb-create-organization";
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @SagaTask(code = TASK_ORG_CREATE,
            description = "knowledge_base消费创建组织",
            sagaCode = ORG_CREATE, seq = 1)
    public String handleOrganizationCreateByConsumeSagaTask(String data) {
        LOGGER.info("消费创建组织消息{}", data);
        OrganizationCreateEventPayload organizationEventPayload = JSONObject.parseObject(data, OrganizationCreateEventPayload.class);
        KnowledgeBaseDTO knowledgeBaseDTO = new KnowledgeBaseDTO(organizationEventPayload.getName(),"组织下默认知识库","range_private",null,organizationEventPayload.getId());
        knowledgeBaseDTO.setCreatedBy(organizationEventPayload.getUserId());
        knowledgeBaseDTO.setLastUpdatedBy(organizationEventPayload.getUserId());
        knowledgeBaseService.baseInsert(knowledgeBaseDTO);
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
        KnowledgeBaseDTO knowledgeBaseDTO = new KnowledgeBaseDTO(projectEvent.getProjectName(),"项目下默认知识库","range_private",projectEvent.getProjectId(),projectEvent.getOrganizationId());
        knowledgeBaseDTO.setCreatedBy(projectEvent.getUserId());
        knowledgeBaseDTO.setLastUpdatedBy(projectEvent.getUserId());
        knowledgeBaseService.baseInsert(knowledgeBaseDTO);
        return message;
    }
}
