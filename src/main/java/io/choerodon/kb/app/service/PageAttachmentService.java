package io.choerodon.kb.app.service;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import io.choerodon.kb.api.dao.PageAttachmentDTO;
import io.choerodon.kb.api.dao.PageCreateAttachmentDTO;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageAttachmentService {

    List<PageAttachmentDTO> create(Long pageId, PageCreateAttachmentDTO pageCreateAttachmentDTO, HttpServletRequest request);

    void delete(Long id);
}
