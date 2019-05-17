package io.choerodon.kb.infra.mapper;

import io.choerodon.kb.infra.dataobject.PageContentDO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageContentMapper extends Mapper<PageContentDO> {

    void deleteByPageId(@Param("pageId") Long pageId);

    void deleteByPageIdAndVsersionId(@Param("pageId") Long pageId);

    PageContentDO selectByVersionId(@Param("versionId") Long versionId);

    PageContentDO selectLatestByPageId(@Param("pageId") Long pageId);

    List<PageContentDO> queryByPageId(@Param("pageId") Long pageId);
}
