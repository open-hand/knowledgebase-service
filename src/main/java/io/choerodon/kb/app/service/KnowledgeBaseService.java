package io.choerodon.kb.app.service;

import java.util.List;

import io.choerodon.kb.api.vo.KnowledgeBaseInfoVO;
import io.choerodon.kb.api.vo.KnowledgeBaseListVO;
import io.choerodon.kb.infra.dto.KnowledgeBaseDTO;

/**
 * @author zhaotianxin
 * @since 2019/12/30
 */
public interface KnowledgeBaseService {

    KnowledgeBaseDTO baseInsert(KnowledgeBaseDTO knowledgeBaseDTO);

    KnowledgeBaseDTO baseUpdate(KnowledgeBaseDTO knowledgeBaseDTO);


    /**
     * 创建知识库
     * @param organizationId
     * @param projectId
     * @param knowledgeBaseInfoVO
     * @return
     */
    KnowledgeBaseInfoVO create(Long organizationId, Long projectId, KnowledgeBaseInfoVO knowledgeBaseInfoVO);

    /**
     * 新增知识库
     * @param organizationId
     * @param projectId
     * @param knowledgeBaseInfoVO
     * @return
     */
    KnowledgeBaseInfoVO update(Long organizationId, Long projectId, KnowledgeBaseInfoVO knowledgeBaseInfoVO);

    /**
     * 将知识库移到回收站
     * @param organizationId
     * @param projectId
     * @param baseId
     */
    void removeKnowledgeBase(Long organizationId, Long projectId, Long baseId);

    /**
     * 删除回收站下的知识库
     * @param organizationId
     * @param projectId
     * @param baseId
     */
    void deleteKnowledgeBase(Long organizationId, Long projectId, Long baseId);

    /**
     * 将知识库恢复到项目下
     * @param organizationId
     * @param projectId
     * @param baseId
     */
    void restoreKnowledgeBase(Long organizationId, Long projectId, Long baseId);

    /**
     * 查询项目下的知识库
     * @param organizationId
     * @param projectId
     * @return
     */
    List<List<KnowledgeBaseListVO>> queryKnowledgeBaseWithRecent(Long organizationId, Long projectId);

}
