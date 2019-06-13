package io.choerodon.kb.app.service;

import java.util.List;

import io.choerodon.kb.api.dao.*;

/**
 * Created by Zenger on 2019/6/10.
 */
public interface WorkSpaceShareService {

    WorkSpaceShareDTO query(Long workSpaceId);

    WorkSpaceShareDTO update(Long id, WorkSpaceShareUpdateDTO workSpaceShareUpdateDTO);

    WorkSpaceFirstTreeDTO queryTree(String token);

    PageDTO queryPage(Long workSpaceId, String token);

    List<PageAttachmentDTO> queryPageAttachment(Long workSpaceId, String token);
}
