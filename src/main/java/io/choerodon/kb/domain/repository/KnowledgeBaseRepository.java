package io.choerodon.kb.domain.repository;

import java.util.List;

import io.choerodon.kb.infra.dto.KnowledgeBaseDTO;

import org.hzero.mybatis.base.BaseRepository;

/**
 * @author superlee
 * @date 2022-09-28
 */
public interface KnowledgeBaseRepository extends BaseRepository<KnowledgeBaseDTO> {

    /**
     * 条件查询知识库
     * @param queryParam        查询条件
     * @return                  查询结果
     */
    KnowledgeBaseDTO findKnowledgeBaseByCondition(KnowledgeBaseDTO queryParam);

    /**
     * 查询所有知识库
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @return                  查询结果
     */
    List<KnowledgeBaseDTO> listKnowledgeBase(Long organizationId, Long projectId);

}
