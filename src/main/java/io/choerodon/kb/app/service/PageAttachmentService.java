package io.choerodon.kb.app.service;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartFile;

import io.choerodon.kb.api.dao.PageAttachmentDTO;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageAttachmentService {

    List<PageAttachmentDTO> create(Long resourceId,
                                   String type,
                                   Long pageId,
                                   Long versionId,
                                   HttpServletRequest request);

    List<String> uploadForAddress(Long resourceId,
                                  String type,
                                  HttpServletRequest request);

    void delete(Long id);
}
