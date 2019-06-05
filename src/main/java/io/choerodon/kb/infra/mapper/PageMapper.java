package io.choerodon.kb.infra.mapper;

import io.choerodon.kb.api.dao.PageInfo;
import io.choerodon.kb.infra.dataobject.PageDO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageMapper extends Mapper<PageDO> {
    PageInfo queryInfoById(@Param("pageId") Long pageId);
}
