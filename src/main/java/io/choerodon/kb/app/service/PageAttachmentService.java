package io.choerodon.kb.app.service;

import io.choerodon.kb.api.vo.PageAttachmentVO;
import io.choerodon.kb.infra.dto.PageAttachmentDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageAttachmentService {

    List<PageAttachmentVO> create(Long organizationId, Long projectId, Long pageId,
                                  List<MultipartFile> files);

    List<String> uploadForAddress(Long organizationId, List<MultipartFile> files);

    List<PageAttachmentVO> queryByList(Long organizationId, Long projectId, Long pageId);

    PageAttachmentDTO insertPageAttachment(Long organizationId, Long projectId, String name, Long pageId, Long size, String url);

    String dealUrl(String url);

    void delete(Long organizationId, Long projectId, Long id);

    void deleteFile(Long organizationId, String url);

    PageAttachmentVO queryByFileName(Long organizationId, Long projectId, String fileName);

    void batchDelete(Long organizationId, Long projectId, List<Long> ids);

    List<PageAttachmentDTO> batchInsert(List<PageAttachmentDTO> list);

    List<PageAttachmentVO> copyAttach(Long pageId, List<PageAttachmentDTO> pageAttachmentDTOS);
}
