package io.choerodon.kb.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import io.choerodon.kb.api.vo.KnowledgeBaseListVO;
import io.choerodon.kb.api.vo.KnowledgeBaseTreeVO;
import io.choerodon.kb.api.vo.SearchVO;
import io.choerodon.kb.api.vo.RecycleVO;
import io.choerodon.kb.api.vo.SearchDTO;
import io.choerodon.kb.infra.dto.KnowledgeBaseDTO;
import io.choerodon.mybatis.common.Mapper;

/**
 * @author zhaotianxin
 * @since 2019/12/30
 */
public interface KnowledgeBaseMapper extends Mapper<KnowledgeBaseDTO> {

    List<KnowledgeBaseTreeVO> listSystemTemplateBase(@Param("searchVO") SearchVO searchVO);

    List<KnowledgeBaseListVO> queryKnowledgeBaseList(@Param("projectId")Long projectId,@Param("organizationId")Long organizationId);

    List<RecycleVO> queryAllDetele(@Param("organizationId") Long organizationId, @Param("projectId") Long projectId, @Param("searchDTO") SearchDTO searchDTO);
}
