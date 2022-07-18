package io.choerodon.kb.app.service;

import io.choerodon.kb.api.vo.InitKnowledgeBaseTemplateVO;

import java.util.List;

/**
 * @author zhaotianxin
 * @date 2021/11/23 10:00
 */
public interface KnowledgeBaseTemplateService {

    void initWorkSpaceTemplate();

    List<InitKnowledgeBaseTemplateVO> buildInitData();
}