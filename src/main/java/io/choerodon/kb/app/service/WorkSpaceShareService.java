package io.choerodon.kb.app.service;

import io.choerodon.kb.api.dao.PageDTO;
import io.choerodon.kb.api.dao.WorkSpaceFirstTreeDTO;
import io.choerodon.kb.api.dao.WorkSpaceShareDTO;

/**
 * Created by Zenger on 2019/6/10.
 */
public interface WorkSpaceShareService {

    WorkSpaceShareDTO create(Long workSpaceId, Boolean isContain);

    WorkSpaceShareDTO query(Long id);

    WorkSpaceFirstTreeDTO queryTree(String token);

    PageDTO queryPage(String token);
}
