package io.choerodon.kb.app.service;

import java.util.List;

import io.choerodon.kb.api.vo.InitKnowledgeBaseTemplateVO;

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
}
