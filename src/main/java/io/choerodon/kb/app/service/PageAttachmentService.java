package io.choerodon.kb.app.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import io.choerodon.kb.api.dao.PageAttachmentDTO;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageAttachmentService {

    List<PageAttachmentDTO> create(Long pageId,
                                   List<MultipartFile> files);

    List<String> uploadForAddress(List<MultipartFile> files);

    List<PageAttachmentDTO> queryByList(Long pageId);

    void delete(Long id);
}
