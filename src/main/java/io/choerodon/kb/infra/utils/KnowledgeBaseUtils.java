package io.choerodon.kb.infra.utils;


import io.choerodon.kb.domain.repository.KnowledgeBaseRepository;
import io.choerodon.kb.infra.dto.KnowledgeBaseDTO;
import org.hzero.core.util.AssertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeBaseUtils {

    @Autowired
    private KnowledgeBaseRepository knowledgeBaseRepository;


    public boolean templateFlag(Long baseId) {
        KnowledgeBaseDTO knowledgeBaseDTO = knowledgeBaseRepository.selectByPrimaryKey(baseId);
        AssertUtils.notNull(knowledgeBaseDTO, "error.data.not.exist");
        return knowledgeBaseDTO.getTemplateFlag();
    }

}
