package io.choerodon.kb.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.kb.infra.dataobject.PageContentDO;
import io.choerodon.mybatis.common.Mapper;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageContentMapper extends Mapper<PageContentDO> {

    void deleteByPageId(@Param("pageId") Long pageId);
}
