package io.choerodon.kb.app.service;

import java.util.List;

import io.choerodon.kb.api.dao.PageAttachmentDTO;
import io.choerodon.kb.api.dao.PageDTO;
import io.choerodon.kb.api.dao.WorkSpaceFirstTreeDTO;
import io.choerodon.kb.api.dao.WorkSpaceShareDTO;

/**
 * Created by Zenger on 2019/6/10.
 */
public interface WorkSpaceShareService {

    WorkSpaceShareDTO query(Long workSpaceId);

    WorkSpaceShareDTO update(Long id, String type);

    WorkSpaceFirstTreeDTO queryTree(String token);

    PageDTO queryPage(Long workSpaceId, String token);

    List<PageAttachmentDTO> queryPageAttachment(Long workSpaceId, String token);
}
