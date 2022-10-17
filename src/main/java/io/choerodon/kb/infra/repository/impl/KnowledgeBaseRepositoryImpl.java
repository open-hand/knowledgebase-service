package io.choerodon.kb.infra.repository.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.kb.domain.repository.KnowledgeBaseRepository;
import io.choerodon.kb.infra.dto.KnowledgeBaseDTO;
import io.choerodon.kb.infra.mapper.KnowledgeBaseMapper;

import org.hzero.mybatis.base.impl.BaseRepositoryImpl;

/**
 * @author superlee
 * @since 2022-09-28
 */
@Service
public class KnowledgeBaseRepositoryImpl extends BaseRepositoryImpl<KnowledgeBaseDTO> implements KnowledgeBaseRepository {

    @Autowired
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Override
    public KnowledgeBaseDTO findKnowledgeBaseByCondition(KnowledgeBaseDTO queryParam) {
        return this.knowledgeBaseMapper.findKnowledgeBaseByCondition(queryParam);
    }

    @Override
    public List<KnowledgeBaseDTO> listKnowledgeBase(Long organizationId, Long projectId) {
        if(organizationId == null || projectId == null) {
            return Collections.emptyList();
        }
        return this.knowledgeBaseMapper.listKnowledgeBase(organizationId, projectId);
    }
}
