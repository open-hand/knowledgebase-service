package io.choerodon.kb.domain.kb.repository;

import io.choerodon.kb.api.dao.PageInfoVO;
import io.choerodon.kb.infra.dto.PageDTO;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface PageRepository {

    PageDTO selectById(Long id);

    PageDTO update(PageDTO pageDTO, Boolean flag);

    void delete(Long id);

    PageDTO create(PageDTO create);

    PageDTO queryById(Long organizationId, Long projectId, Long pageId);

    PageInfoVO queryInfoById(Long organizationId, Long projectId, Long pageId);

    PageInfoVO queryShareInfoById(Long pageId);

    void checkById(Long organizationId, Long projectId, Long pageId);
}
