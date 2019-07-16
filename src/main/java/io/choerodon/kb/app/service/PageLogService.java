package io.choerodon.kb.app.service;

import java.util.List;

import io.choerodon.kb.api.dao.PageLogVO;

/**
 * Created by Zenger on 2019/5/17.
 */
public interface PageLogService {

    List<PageLogVO> listByPageId(Long pageId);

}
