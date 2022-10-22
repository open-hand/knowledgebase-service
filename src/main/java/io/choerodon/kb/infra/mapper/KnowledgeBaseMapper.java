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

    List<KnowledgeBaseDTO> listKnowledgeBase(Long organizationId, Long projectId);

    KnowledgeBaseDTO findKnowledgeBaseByCondition(KnowledgeBaseDTO queryParam);

    /**
     * 查回收站里的知识库，文档，模版等
     *
     * @param organizationId
     * @param projectId
     * @param searchType
     * @param searchDTO
     * @param userInfo
     * @param permissionFlag
     * @param rowNums
     * @return
     */
    List<RecycleVO> listRecycleData(@Param("organizationId") Long organizationId,
                                    @Param("projectId") Long projectId,
                                    @Param("searchType") String searchType,
                                    @Param("searchDTO") SearchDTO searchDTO,
                                    @Param("userInfo") UserInfoVO userInfo,
                                    @Param("permissionFlag") Boolean permissionFlag,
                                    @Param("rowNums") List<Integer> rowNums);

    /**
     * 查回收站里的知识库
     *
     * @param organizationId
     * @param projectId
     * @param searchDTO
     * @param userInfo
     * @param permissionFlag
     * @return
     */
    List<RecycleVO> listRecycleKnowledgeBase(@Param("organizationId") Long organizationId,
                                             @Param("projectId") Long projectId,
                                             @Param("searchDTO") SearchDTO searchDTO,
                                             @Param("userInfo") UserInfoVO userInfo,
                                             @Param("permissionFlag") Boolean permissionFlag);

    /**
     * 查回收站里的文档，模版等
     *
     * @param organizationId
     * @param projectId
     * @param searchType
     * @param searchDTO
     * @param userInfo
     * @param permissionFlag
     * @param rowNums
     * @return
     */
    List<RecycleVO> listRecycleWorkSpace(@Param("organizationId") Long organizationId,
                                         @Param("projectId") Long projectId,
                                         @Param("searchType") String searchType,
                                         @Param("searchDTO") SearchDTO searchDTO,
                                         @Param("userInfo") UserInfoVO userInfo,
                                         @Param("permissionFlag") Boolean permissionFlag,
                                         @Param("rowNums") List<Integer> rowNums);
}
