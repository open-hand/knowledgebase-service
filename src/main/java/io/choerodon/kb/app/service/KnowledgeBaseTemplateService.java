package io.choerodon.kb.app.service;

import io.choerodon.kb.api.vo.InitKnowledgeBaseTemplateVO;
import io.choerodon.kb.api.vo.KnowledgeBaseInfoVO;

import java.util.List;

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
     * @param knowledgeBaseInfoVO
     * @param knowledgeBaseId
     * @param createKnowledgeBase
     */
    void copyKnowledgeBaseFromTemplate(Long organizationId,
                                       Long projectId,
                                       KnowledgeBaseInfoVO knowledgeBaseInfoVO,
                                       Long knowledgeBaseId,
                                       Long targetWorkSpaceId,
                                       boolean createKnowledgeBase);
}