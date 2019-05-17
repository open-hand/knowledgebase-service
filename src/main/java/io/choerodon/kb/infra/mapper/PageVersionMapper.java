package io.choerodon.kb.infra.mapper;

import io.choerodon.kb.infra.dataobject.PageVersionDO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageVersionMapper extends Mapper<PageVersionDO> {

    void deleteByPageId(@Param("pageId") Long pageId);

    List<PageVersionDO> queryByPageId(@Param("pageId") Long pageId);

    String selectMaxVersionByPageId(@Param("pageId") Long pageId);
}
