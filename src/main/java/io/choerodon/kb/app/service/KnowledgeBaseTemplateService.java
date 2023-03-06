package io.choerodon.kb.app.service;

import io.choerodon.kb.api.vo.InitKnowledgeBaseTemplateVO;

import java.util.List;
import java.util.Set;

/**
 * @author zhaotianxin
 * @date 2021/11/23 10:00
 */
public interface KnowledgeBaseTemplateService {

    void initWorkSpaceTemplate();

    List<InitKnowledgeBaseTemplateVO> buildInitData();

    /**
     * 如果平台预置文档模版不存在，初始化平台预置文档模版
     */
    void initPlatformDocTemplate();

    /**
     * 从模版库复制文件，不需要鉴权
     *
     * @param organizationId
     * @param projectId
     * @param templateBaseIds
     * @param knowledgeBaseId
     * @param uuid
     * @param deleteKnowledgeBase
     */
    void copyKnowledgeBaseFromTemplate(Long organizationId,
                                       Long projectId,
                                       Set<Long> templateBaseIds,
                                       Long knowledgeBaseId,
                                       String uuid,
                                       boolean deleteKnowledgeBase);
}