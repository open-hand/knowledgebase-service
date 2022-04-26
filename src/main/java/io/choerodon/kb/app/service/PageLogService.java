package io.choerodon.kb.app.service;

import java.util.List;

import io.choerodon.kb.api.vo.PageLogVO;
import io.choerodon.kb.infra.dto.PageLogDTO;

/**
 * Created by Zenger on 2019/5/17.
 */
public interface PageLogService {

    PageLogDTO baseCreate(PageLogDTO pageLogDTO);

    void deleteByPageId(Long pageId);

    List<PageLogVO> listByPageId(Long organizationId, Long projectId, Long pageId);

}
