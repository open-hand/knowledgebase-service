package io.choerodon.kb.infra.mapper;

import io.choerodon.kb.api.dao.PageInfoVO;
import io.choerodon.kb.api.dao.PageSyncVO;
import io.choerodon.kb.infra.dataobject.PageDO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageMapper extends Mapper<PageDO> {
    PageInfoVO queryInfoById(@Param("pageId") Long pageId);

    void updateBaseData(@Param("pageId") Long pageId, @Param("base") PageDO base);

    List<PageSyncVO> querySync2EsPage();

    void updateSyncEs();

    void updateSyncEsByPageId(@Param("pageId") Long pageId, @Param("isSyncEs") Boolean isSyncEs);
}
