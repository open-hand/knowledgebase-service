package io.choerodon.kb.app.service;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import io.choerodon.kb.api.dao.PageAttachmentDTO;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageAttachmentService {

    List<PageAttachmentDTO> create(Long resourceId,
                                   String type,
                                   Long pageId,
                                   HttpServletRequest request);

    List<String> uploadForAddress(Long resourceId,
                                  String type,
                                  HttpServletRequest request);

    List<PageAttachmentDTO> queryByList(Long pageId);

    void delete(Long id);
}
