package io.choerodon.kb.app.service;

import java.util.List;

import io.choerodon.kb.api.dao.PageCreateDTO;
import io.choerodon.kb.api.dao.PageDTO;
import io.choerodon.kb.api.dao.PageUpdateDTO;
import io.choerodon.kb.api.dao.WorkSpaceTreeDTO;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface WorkSpaceService {

    PageDTO create(Long resourceId, PageCreateDTO pageCreateDTO, String type);

    PageDTO queryDetail(Long resourceId, Long id, String type);

    PageDTO update(Long resourceId, Long id, PageUpdateDTO pageUpdateDTO, String type);

    void delete(Long resourceId, Long id, String type);

    List<WorkSpaceTreeDTO> queryByTree(Long resourceId, List<Long> parentIds, String type);
}
