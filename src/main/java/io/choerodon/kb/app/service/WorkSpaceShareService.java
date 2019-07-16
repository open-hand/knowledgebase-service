package io.choerodon.kb.app.service;

import io.choerodon.kb.api.dao.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Created by Zenger on 2019/6/10.
 */
public interface WorkSpaceShareService {

    WorkSpaceShareVO query(Long workSpaceId);

    WorkSpaceShareVO update(Long id, WorkSpaceShareUpdateVO workSpaceShareUpdateVO);

    Map<String, Object> queryTree(String token);

    PageVO queryPage(Long workSpaceId, String token);

    List<PageAttachmentVO> queryPageAttachment(Long workSpaceId, String token);

    void exportMd2Pdf(Long pageId, String token, HttpServletResponse response);
}
