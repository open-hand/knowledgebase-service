package io.choerodon.kb.app.service;

import io.choerodon.kb.api.dao.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Created by Zenger on 2019/6/10.
 */
public interface WorkSpaceShareService {

    WorkSpaceShareDTO query(Long workSpaceId);

    WorkSpaceShareDTO update(Long id, WorkSpaceShareUpdateDTO workSpaceShareUpdateDTO);

    Map<String, Object> queryTree(String token);

    PageDTO queryPage(Long workSpaceId, String token);

    List<PageAttachmentDTO> queryPageAttachment(Long workSpaceId, String token);

    String pageToc(Long pageId, String token);

    void exportMd2Pdf(Long pageId, String token, HttpServletResponse response);
}
