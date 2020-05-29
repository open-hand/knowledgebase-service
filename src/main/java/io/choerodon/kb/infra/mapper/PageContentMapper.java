package io.choerodon.kb.infra.mapper;

import io.choerodon.kb.api.vo.PageCreateVO;
import io.choerodon.kb.infra.dto.PageContentDTO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageContentMapper extends BaseMapper<PageContentDTO> {

    void deleteByPageId(@Param("pageId") Long pageId);

    PageContentDTO selectByVersionId(@Param("versionId") Long versionId);

    PageContentDTO selectLatestByPageId(@Param("pageId") Long pageId);

    PageContentDTO selectLatestByWorkSpaceId(@Param("workSpaceId") Long workSpaceId);

    List<PageContentDTO> queryByPageId(@Param("pageId") Long pageId);

    List<PageCreateVO> listTemplatePageByBaseId(@Param("origanizationId") Long origanizationId, @Param("projectId") Long projectId, @Param("templateBaseId") Long templateBaseId);
}
