package io.choerodon.kb.infra.repository;

import io.choerodon.kb.api.vo.PageInfoVO;
import io.choerodon.kb.infra.dto.PageDTO;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface PageRepository {

    PageDTO selectById(Long id);

    PageDTO baseUpdate(PageDTO pageDTO, Boolean flag);

    void baseDelete(Long id);

    PageDTO baseCreate(PageDTO create);

    PageDTO baseQueryById(Long organizationId, Long projectId, Long pageId);

    PageInfoVO queryInfoById(Long organizationId, Long projectId, Long pageId);

    PageInfoVO queryShareInfoById(Long pageId);

    void checkById(Long organizationId, Long projectId, Long pageId);
}
