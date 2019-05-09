package io.choerodon.kb.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.kb.infra.dataobject.PageVersionDO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageVersionMapper extends BaseMapper<PageVersionDO> {

    void deleteByPageId(@Param("pageId") Long pageId);
}
