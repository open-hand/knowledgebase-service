package io.choerodon.kb.app.service;

import io.choerodon.kb.api.dao.PageInfoDTO;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageService {

    Boolean checkPageCreate(Long id);

    PageInfoDTO queryPageInfo(Long id);
}
