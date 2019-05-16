package io.choerodon.kb.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.kb.infra.dataobject.PageVersionDO;
import io.choerodon.mybatis.common.Mapper;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageVersionMapper extends Mapper<PageVersionDO> {

    void deleteByPageId(@Param("pageId") Long pageId);
}
