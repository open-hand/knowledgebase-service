package io.choerodon.kb.domain.kb.repository;

import io.choerodon.kb.infra.dto.PageLogDTO;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface PageLogRepository {

    PageLogDTO baseCreate(PageLogDTO pageLogDTO);

    void deleteByPageId(Long pageId);
}
