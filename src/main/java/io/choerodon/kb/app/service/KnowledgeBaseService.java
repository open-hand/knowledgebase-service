package io.choerodon.kb.app.service;

import java.util.List;

import io.choerodon.kb.api.vo.KnowledgeBaseInfoVO;
import io.choerodon.kb.api.vo.KnowledgeBaseInitProgress;
import io.choerodon.kb.api.vo.KnowledgeBaseListVO;
import io.choerodon.kb.infra.dto.KnowledgeBaseDTO;

/**
 * @author zhaotianxin
 * @since 2019/12/30
 */
public interface KnowledgeBaseService {

    KnowledgeBaseDTO queryById(Long id);

    KnowledgeBaseDTO baseInsert(KnowledgeBaseDTO knowledgeBaseDTO);

    KnowledgeBaseDTO baseUpdate(KnowledgeBaseDTO knowledgeBaseDTO);


    /**
     * 创建知识库
     *
     * @param organizationId
     * @param projectId
     * @param knowledgeBaseInfoVO
     * @param initFlag            是否为sagaTask初始化
     * @return
     */
    KnowledgeBaseInfoVO create(Long organizationId,
                               Long projectId,
                               KnowledgeBaseInfoVO knowledgeBaseInfoVO,
                               boolean initFlag);

    /**
     * 创建知识库，不初始化模版
     *
     * @param organizationId
     * @param projectId
     * @param knowledgeBaseInfoVO
     * @param initFlag
     * @return
     */
    KnowledgeBaseInfoVO createBase(Long organizationId,
                                   Long projectId,
                                   KnowledgeBaseInfoVO knowledgeBaseInfoVO,
                                   boolean initFlag);

    void createDefaultFolder(Long organizationId,
                             Long projectId,
                             KnowledgeBaseDTO knowledgeBase,
                             boolean initFlag);

    /**
     * 新增知识库
     *
     * @param organizationId
     * @param projectId
     * @param knowledgeBaseInfoVO
     * @return
     */
    KnowledgeBaseInfoVO update(Long organizationId, Long projectId, KnowledgeBaseInfoVO knowledgeBaseInfoVO);

    /**
     * 将知识库移到回收站
     *
     * @param organizationId
     * @param projectId
     * @param baseId
     */
    void removeKnowledgeBase(Long organizationId, Long projectId, Long baseId);

    /**
     * 删除回收站下的知识库
     *
     * @param organizationId
     * @param projectId
     * @param baseId
     */
    void deleteKnowledgeBase(Long organizationId, Long projectId, Long baseId);

    /**
     * 将知识库恢复到项目下
     *
     * @param organizationId
     * @param projectId
     * @param baseId
     */
    void restoreKnowledgeBase(Long organizationId, Long projectId, Long baseId);

    /**
     * 查询项目下的知识库
     *
     * @param organizationId 组织ID
     * @param projectId      项目ID
     * @return [[projectList], [otherProjectList]]
     */
    List<List<KnowledgeBaseListVO>> queryKnowledgeBaseWithRecent(Long organizationId, Long projectId, boolean templateFlag,
                                                                 String params);


    KnowledgeBaseDTO createKnowledgeBaseTemplate(KnowledgeBaseDTO knowledgeBaseDTO);

    void publishKnowledgeBaseTemplate(Long organizationId, Long knowledgeBaseId);

    void unPublishKnowledgeBaseTemplate(Long organizationId, Long knowledgeBaseId);

    void updateKnowledgeBaseTemplate(Long organizationId, KnowledgeBaseInfoVO knowledgeBaseInfoVO);

    /**
     * 查询知识库是否初始化完成
     *
     * @param id
     * @return
     */
    Boolean queryInitCompleted(Long id);

    /**
     * 基于模版创建文档
     *
     * @param organizationId
     * @param projectId
     * @param id
     */
    void createBaseTemplate(Long organizationId,
                            Long projectId,
                            Long id,
                            KnowledgeBaseInfoVO knowledgeBaseInfoVO);

    KnowledgeBaseInfoVO queryKnowledgeBaseById(Long organizationId, Long projectId, Long id, String category);

    /**
     * 根据uuid查询进度
     *
     * @param uuid
     * @return
     */
    KnowledgeBaseInitProgress queryProgressByUuid(String uuid);
}
