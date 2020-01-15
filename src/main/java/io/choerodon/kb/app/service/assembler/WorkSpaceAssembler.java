package io.choerodon.kb.app.service.assembler;

import io.choerodon.kb.api.vo.KnowledgeBaseTreeVO;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import org.springframework.stereotype.Component;
/**
 * @author zhaotianxin
 * @since 2020/1/3
 */
@Component
public class WorkSpaceAssembler {

    public KnowledgeBaseTreeVO dtoToTreeVO(WorkSpaceDTO workSpaceDTO) {
        KnowledgeBaseTreeVO knowledgeBaseTreeVO = new KnowledgeBaseTreeVO();
        knowledgeBaseTreeVO.setId(workSpaceDTO.getId());
        knowledgeBaseTreeVO.setName(workSpaceDTO.getName());
        if (workSpaceDTO.getParentId() == 0) {
            knowledgeBaseTreeVO.setParentId(workSpaceDTO.getBaseId());
        } else {
            knowledgeBaseTreeVO.setParentId(workSpaceDTO.getParentId());
        }
        knowledgeBaseTreeVO.setTopLeavl(false);
        return knowledgeBaseTreeVO;
    }
}
