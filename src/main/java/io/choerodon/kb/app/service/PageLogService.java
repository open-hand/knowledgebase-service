package io.choerodon.kb.app.service;

import io.choerodon.kb.api.dao.PageLogVO;
import io.choerodon.kb.infra.dto.PageLogDTO;

import java.util.List;

/**
 * Created by Zenger on 2019/5/17.
 */
public interface PageLogService {

    PageLogDTO baseCreate(PageLogDTO pageLogDTO);

    void deleteByPageId(Long pageId);

    List<PageLogVO> listByPageId(Long organizationId, Long projectId, Long pageId);

}
