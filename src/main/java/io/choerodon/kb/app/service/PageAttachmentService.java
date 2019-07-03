package io.choerodon.kb.app.service;

import java.util.List;

import io.choerodon.kb.api.dao.AttachmentSearchDTO;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.kb.api.dao.PageAttachmentDTO;
import io.choerodon.kb.infra.dataobject.PageAttachmentDO;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageAttachmentService {

    List<PageAttachmentDTO> create(Long pageId,
                                   List<MultipartFile> files);

    List<String> uploadForAddress(List<MultipartFile> files);

    List<PageAttachmentDTO> queryByList(Long pageId);

    PageAttachmentDO insertPageAttachment(String name, Long pageId, Long size, String url);

    String dealUrl(String url);

    void delete(Long id);

    void deleteFile(String url);

    List<PageAttachmentDTO> searchAttachment(AttachmentSearchDTO attachmentSearchDTO);
}
