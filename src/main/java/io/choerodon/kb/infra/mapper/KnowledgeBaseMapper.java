package io.choerodon.kb.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.kb.api.vo.*;
import io.choerodon.kb.api.vo.permission.UserInfoVO;
import io.choerodon.kb.infra.dto.KnowledgeBaseDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * @author zhaotianxin
 * @since 2019/12/30
 */
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBaseDTO> {

    List<KnowledgeBaseTreeVO> listSystemTemplateBase(@Param("searchVO") SearchVO searchVO);

    List<KnowledgeBaseListVO> queryKnowledgeBaseList(@Param("projectId") Long projectId, @Param("organizationId") Long organizationId);

    List<RecycleVO> queryAllDelete(@Param("organizationId") Long organizationId,
                                   @Param("projectId") Long projectId,
                                   @Param("searchDTO") SearchDTO searchDTO,
                                   @Param("userInfo") UserInfoVO userInfo,
                                   @Param("permissionFlag") Boolean permissionFlag);

    List<KnowledgeBaseDTO> listKnowledgeBase(Long organizationId, Long projectId);

    KnowledgeBaseDTO findKnowledgeBaseByCondition(KnowledgeBaseDTO queryParam);
}
