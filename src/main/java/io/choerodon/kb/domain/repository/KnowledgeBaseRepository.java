package io.choerodon.kb.domain.repository;

import java.util.List;

import io.choerodon.kb.api.vo.KnowledgeBaseListVO;
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

    /**
     * 校验当前用户是否可以访问当前知识库
     *
     * @param organizationId    组织ID
     * @param knowledgeBaseId   知识库ID
     * @return 当前用户是否可以访问当前知识库
     */
    boolean checkOpenRangeCanAccess(Long organizationId, Long knowledgeBaseId);

    List<KnowledgeBaseListVO> queryKnowledgeBaseList(Long projectId,
                                                     Long organizationId,
                                                     boolean templateFlag,
                                                     String params);

    boolean isTemplate(Long baseId);

    boolean isTemplate(KnowledgeBaseDTO knowledgeBase);
}
