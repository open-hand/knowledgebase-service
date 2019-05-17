package io.choerodon.kb.app.service.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.dao.PageAttachmentDTO;
import io.choerodon.kb.app.service.PageAttachmentService;
import io.choerodon.kb.app.service.PageService;
import io.choerodon.kb.domain.kb.repository.PageAttachmentRepository;
import io.choerodon.kb.domain.kb.repository.PageVersionRepository;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.dataobject.PageAttachmentDO;
import io.choerodon.kb.infra.dataobject.PageVersionDO;
import io.choerodon.kb.infra.feign.FileFeignClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zenger on 2019/4/30.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class PageAttachmentServiceImpl implements PageAttachmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PageAttachmentServiceImpl.class);

    @Value("${services.attachment.url}")
    private String attachmentUrl;

    private PageService pageService;
    private FileFeignClient fileFeignClient;
    private PageVersionRepository pageVersionRepository;
    private PageAttachmentRepository pageAttachmentRepository;

    public PageAttachmentServiceImpl(PageService pageService,
                                     FileFeignClient fileFeignClient,
                                     PageVersionRepository pageVersionRepository,
                                     PageAttachmentRepository pageAttachmentRepository) {
        this.pageService = pageService;
        this.fileFeignClient = fileFeignClient;
        this.pageVersionRepository = pageVersionRepository;
        this.pageAttachmentRepository = pageAttachmentRepository;
    }

    @Override
    public List<PageAttachmentDTO> create(Long resourceId,
                                          String type,
                                          Long pageId,
                                          Long versionId,
                                          HttpServletRequest request) {
        this.checkPageVersion(pageId, versionId);
        List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("file");
        if (!(files != null && !files.isEmpty())) {
            throw new CommonException("error.attachment.exits");
        }
        for (MultipartFile multipartFile : files) {
            String fileName = multipartFile.getOriginalFilename();
            ResponseEntity<String> response = fileFeignClient.uploadFile(BaseStage.BACKETNAME, fileName, multipartFile);
            if (response == null || response.getStatusCode() != HttpStatus.OK) {
                throw new CommonException("error.attachment.upload");
            }
            this.insertPageAttachment(fileName,
                    pageId,
                    multipartFile.getSize(),
                    versionId,
                    dealUrl(response.getBody()));
        }
        String urlSlash = attachmentUrl.endsWith("/") ? "" : "/";
        List<PageAttachmentDTO> list = ConvertHelper.convertList(pageAttachmentRepository.selectByPageId(pageId), PageAttachmentDTO.class);
        list.stream().forEach(p -> p.setUrl(attachmentUrl + urlSlash + p.getUrl()));
        return list;
    }

    @Override
    public List<String> uploadForAddress(Long resourceId, String type, HttpServletRequest request) {
        List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("file");
        if (!(files != null && !files.isEmpty())) {
            throw new CommonException("error.attachment.exits");
        }
        List<String> result = new ArrayList<>();
        for (MultipartFile multipartFile : files) {
            String fileName = multipartFile.getOriginalFilename();
            ResponseEntity<String> response = fileFeignClient.uploadFile(BaseStage.BACKETNAME, fileName, multipartFile);
            if (response == null || response.getStatusCode() != HttpStatus.OK) {
                throw new CommonException("error.attachment.upload");
            }
            result.add(dealUrl(response.getBody()));
        }
        return result;
    }

    @Override
    public void delete(Long id) {
        PageAttachmentDO pageAttachmentDO = pageAttachmentRepository.selectById(id);
        if (pageAttachmentDO == null) {
            throw new CommonException("error.page.attachment.get");
        }
        if (!pageService.checkPageCreate(pageAttachmentDO.getPageId())) {
            throw new CommonException("error.page.creator");
        }
        String urlSlash = attachmentUrl.endsWith("/") ? "" : "/";
        pageAttachmentRepository.delete(id);
        try {
            fileFeignClient.deleteFile(BaseStage.BACKETNAME, attachmentUrl + urlSlash + URLDecoder.decode(pageAttachmentDO.getUrl(), "UTF-8"));
        } catch (Exception e) {
            LOGGER.error("error.attachment.delete", e);
        }
    }

    private void insertPageAttachment(String title, Long pageId, Long size, Long versionId, String url) {
        PageAttachmentDO pageAttachmentDO = new PageAttachmentDO();
        pageAttachmentDO.setTitle(title);
        pageAttachmentDO.setPageId(pageId);
        pageAttachmentDO.setSize(size);
        pageAttachmentDO.setVersion(versionId);
        pageAttachmentDO.setUrl(url);
        pageAttachmentRepository.insert(pageAttachmentDO);
    }

    private void checkPageVersion(Long pageId, Long versionId) {
        PageVersionDO pageVersionDO = new PageVersionDO();
        pageVersionDO.setPageId(pageId);
        pageVersionDO.setId(versionId);
        if (pageVersionRepository.selectOne(pageVersionDO) == null) {
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
