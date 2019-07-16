package io.choerodon.kb.app.service;

import io.choerodon.kb.api.dao.PageAttachmentVO;
import io.choerodon.kb.api.dao.PageVO;
import io.choerodon.kb.api.dao.WorkSpaceShareUpdateVO;
import io.choerodon.kb.api.dao.WorkSpaceShareVO;
import io.choerodon.kb.infra.dto.WorkSpaceShareDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Created by Zenger on 2019/6/10.
 */
public interface WorkSpaceShareService {

    WorkSpaceShareDTO baseCreate(WorkSpaceShareDTO workSpaceShareDTO);

    WorkSpaceShareDTO baseUpdate(WorkSpaceShareDTO workSpaceShareDTO);

    WorkSpaceShareDTO baseQueryById(Long id);

    void deleteByWorkSpaceId(Long workSpaceId);

    WorkSpaceShareDTO selectByWorkSpaceId(Long workSpaceId);

    WorkSpaceShareVO queryShare(Long organizationId, Long projectId, Long workSpaceId);

    WorkSpaceShareVO updateShare(Long organizationId, Long projectId, Long id, WorkSpaceShareUpdateVO workSpaceShareUpdateVO);

    Map<String, Object> queryTree(String token);

    PageVO queryPage(Long workSpaceId, String token);

    List<PageAttachmentVO> queryPageAttachment(Long workSpaceId, String token);

    void exportMd2Pdf(Long pageId, String token, HttpServletResponse response);
}
