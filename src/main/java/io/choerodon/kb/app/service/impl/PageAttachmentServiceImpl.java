package io.choerodon.kb.app.service.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.dao.PageAttachmentDTO;
import io.choerodon.kb.api.dao.PageCreateAttachmentDTO;
import io.choerodon.kb.app.service.PageAttachmentService;
import io.choerodon.kb.domain.kb.entity.PageAttachmentE;
import io.choerodon.kb.domain.kb.entity.PageVersionE;
import io.choerodon.kb.domain.kb.repository.PageAttachmentRepository;
import io.choerodon.kb.domain.kb.repository.PageVersionRepository;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.feign.FileFeignClient;

/**
 * Created by Zenger on 2019/4/30.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class PageAttachmentServiceImpl implements PageAttachmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PageAttachmentServiceImpl.class);

    @Value("${services.attachment.url}")
    private String attachmentUrl;

    private FileFeignClient fileFeignClient;
    private PageVersionRepository pageVersionRepository;
    private PageAttachmentRepository pageAttachmentRepository;

    public PageAttachmentServiceImpl(FileFeignClient fileFeignClient,
                                     PageVersionRepository pageVersionRepository,
                                     PageAttachmentRepository pageAttachmentRepository) {
        this.fileFeignClient = fileFeignClient;
        this.pageVersionRepository = pageVersionRepository;
        this.pageAttachmentRepository = pageAttachmentRepository;
    }

    @Override
    public List<PageAttachmentDTO> create(Long pageId, PageCreateAttachmentDTO pageCreateAttachmentDTO, HttpServletRequest request) {
        this.checkPageVersion(pageId, pageCreateAttachmentDTO.getVersion());
        List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("file");
        if (files != null && !files.isEmpty()) {
            for (MultipartFile multipartFile : files) {
                String fileName = multipartFile.getOriginalFilename();
                ResponseEntity<String> response = fileFeignClient.uploadFile(BaseStage.BACKETNAME, fileName, multipartFile);
                if (response == null || response.getStatusCode() != HttpStatus.OK) {
                    throw new CommonException("error.attachment.upload");
                }
                this.insertPageAttachment(fileName, pageId, multipartFile.getSize(), pageCreateAttachmentDTO, dealUrl(response.getBody()));
            }
        }
        return ConvertHelper.convertList(pageAttachmentRepository.selectByPageId(pageId), PageAttachmentDTO.class);
    }

    @Override
    public void delete(Long id) {
        PageAttachmentE pageAttachmentE = pageAttachmentRepository.selectById(id);
        if (pageAttachmentE == null) {
            throw new CommonException("error.page.attachment.get");
        }
        pageAttachmentRepository.delete(id);
        try {
            fileFeignClient.deleteFile(BaseStage.BACKETNAME, attachmentUrl + URLDecoder.decode(pageAttachmentE.getUrl(), "UTF-8"));
        } catch (Exception e) {
            LOGGER.error("error.attachment.delete", e);
        }
    }

    private void insertPageAttachment(String title, Long pageId, Long size, PageCreateAttachmentDTO pageCreateAttachmentDTO, String url) {
        PageAttachmentE pageAttachmentE = new PageAttachmentE();
        pageAttachmentE.setTitle(title);
        pageAttachmentE.setPageId(pageId);
        pageAttachmentE.setSize(size);
        pageAttachmentE.setAttachmentComment(pageCreateAttachmentDTO.getComment());
        pageAttachmentE.setVersion(pageCreateAttachmentDTO.getVersion());
        pageAttachmentE.setUrl(url);
        pageAttachmentRepository.insert(pageAttachmentE);
    }

    private void checkPageVersion(Long pageId, Long versionId) {
        PageVersionE pageVersionE = new PageVersionE();
        pageVersionE.setPageId(pageId);
        pageVersionE.setId(versionId);
        if (pageVersionRepository.selectOne(pageVersionE) == null) {
            throw new CommonException("page.version.not.exist");
        }
    }

    private String dealUrl(String url) {
        String dealUrl = null;
        try {
            URL netUrl = new URL(url);
            dealUrl = netUrl.getFile().substring(BaseStage.BACKETNAME.length() + 2);
        } catch (MalformedURLException e) {
            throw new CommonException(e.getMessage());
        }
        return dealUrl;
    }
}
