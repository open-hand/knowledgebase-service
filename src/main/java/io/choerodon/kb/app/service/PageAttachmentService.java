package io.choerodon.kb.app.service;

import java.util.List;

import io.choerodon.kb.api.dao.AttachmentSearchVO;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.kb.api.dao.PageAttachmentVO;
import io.choerodon.kb.infra.dto.PageAttachmentDTO;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageAttachmentService {

    List<PageAttachmentVO> create(Long pageId,
                                  List<MultipartFile> files);

    List<String> uploadForAddress(List<MultipartFile> files);

    List<PageAttachmentVO> queryByList(Long pageId);

    PageAttachmentDTO insertPageAttachment(String name, Long pageId, Long size, String url);

    String dealUrl(String url);

    void delete(Long id);

    void deleteFile(String url);

    List<PageAttachmentVO> searchAttachment(AttachmentSearchVO attachmentSearchVO);
}
