package io.choerodon.kb.infra.repository.impl;

import org.springframework.stereotype.Service;

import io.choerodon.kb.domain.repository.KnowledgeBaseRepository;
import io.choerodon.kb.infra.dto.KnowledgeBaseDTO;

import org.hzero.mybatis.base.impl.BaseRepositoryImpl;

/**
 * @author superlee
 * @since 2022-09-28
 */
@Service
public class KnowledgeBaseRepositoryImpl extends BaseRepositoryImpl<KnowledgeBaseDTO> implements KnowledgeBaseRepository {
}
