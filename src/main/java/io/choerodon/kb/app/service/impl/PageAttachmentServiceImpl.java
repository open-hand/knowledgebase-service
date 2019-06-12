package io.choerodon.kb.app.service.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.dao.PageAttachmentDTO;
import io.choerodon.kb.app.service.PageAttachmentService;
import io.choerodon.kb.domain.kb.repository.PageAttachmentRepository;
import io.choerodon.kb.domain.kb.repository.PageRepository;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.dataobject.PageAttachmentDO;
import io.choerodon.kb.infra.dataobject.PageDO;
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
    private PageRepository pageRepository;
    private PageAttachmentRepository pageAttachmentRepository;

    public PageAttachmentServiceImpl(FileFeignClient fileFeignClient,
                                     PageRepository pageRepository,
                                     PageAttachmentRepository pageAttachmentRepository) {
        this.fileFeignClient = fileFeignClient;
        this.pageRepository = pageRepository;
        this.pageAttachmentRepository = pageAttachmentRepository;
    }

    @Override
    public List<PageAttachmentDTO> create(Long pageId,
                                          List<MultipartFile> files) {
        List<Long> ids = new ArrayList<>();
        List<PageAttachmentDTO> list = new ArrayList<>();
        PageDO pageDO = pageRepository.selectById(pageId);
        if (!(files != null && !files.isEmpty())) {
            throw new CommonException("error.attachment.exits");
        }
        for (MultipartFile multipartFile : files) {
            String fileName = multipartFile.getOriginalFilename();
            ResponseEntity<String> response = fileFeignClient.uploadFile(BaseStage.BACKETNAME, fileName, multipartFile);
            if (response == null || response.getStatusCode() != HttpStatus.OK) {
                throw new CommonException("error.attachment.upload");
            }
            ids.add(this.insertPageAttachment(fileName,
                    pageDO.getId(),
                    multipartFile.getSize(),
                    dealUrl(response.getBody())).getId());
        }
        if (!ids.isEmpty()) {
            String urlSlash = attachmentUrl.endsWith("/") ? "" : "/";
            list = ConvertHelper.convertList(pageAttachmentRepository.selectByIds(ids), PageAttachmentDTO.class);
            list.stream().forEach(p -> p.setUrl(attachmentUrl + urlSlash + p.getUrl()));
        }

        return list;
    }

    @Override
    public List<String> uploadForAddress(List<MultipartFile> files) {
        if (!(files != null && !files.isEmpty())) {
            throw new CommonException("error.attachment.exits");
        }
        List<String> result = new ArrayList<>();
        String urlSlash = attachmentUrl.endsWith("/") ? "" : "/";
        for (MultipartFile multipartFile : files) {
            String fileName = multipartFile.getOriginalFilename();
            ResponseEntity<String> response = fileFeignClient.uploadFile(BaseStage.BACKETNAME, fileName, multipartFile);
            if (response == null || response.getStatusCode() != HttpStatus.OK) {
                throw new CommonException("error.attachment.upload");
            }
            result.add(attachmentUrl + urlSlash + dealUrl(response.getBody()));
        }
        return result;
    }

    @Override
    public List<PageAttachmentDTO> queryByList(Long pageId) {
        List<PageAttachmentDO> pageAttachments = pageAttachmentRepository.selectByPageId(pageId);
        if (pageAttachments != null && !pageAttachments.isEmpty()) {
            String urlSlash = attachmentUrl.endsWith("/") ? "" : "/";
            pageAttachments.stream().forEach(pageAttachmentDO -> pageAttachmentDO.setUrl(attachmentUrl + urlSlash + pageAttachmentDO.getUrl()));
        }
        return ConvertHelper.convertList(pageAttachments, PageAttachmentDTO.class);
    }

    @Override
    public void delete(Long id) {
        PageAttachmentDO pageAttachmentDO = pageAttachmentRepository.selectById(id);

        String urlSlash = attachmentUrl.endsWith("/") ? "" : "/";
        pageAttachmentRepository.delete(id);
        try {
            fileFeignClient.deleteFile(BaseStage.BACKETNAME, attachmentUrl + urlSlash + URLDecoder.decode(pageAttachmentDO.getUrl(), "UTF-8"));
        } catch (Exception e) {
            LOGGER.error("error.attachment.delete", e);
        }
    }

    @Override
    public PageAttachmentDO insertPageAttachment(String name, Long pageId, Long size, String url) {
        PageAttachmentDO pageAttachmentDO = new PageAttachmentDO();
        pageAttachmentDO.setName(name);
        pageAttachmentDO.setPageId(pageId);
        pageAttachmentDO.setSize(size);
        pageAttachmentDO.setUrl(url);
        return pageAttachmentRepository.insert(pageAttachmentDO);
    }

    @Override
    public String dealUrl(String url) {
        String dealUrl = null;
        try {
            URL netUrl = new URL(url);
            dealUrl = netUrl.getFile().substring(BaseStage.BACKETNAME.length() + 2);
        } catch (MalformedURLException e) {
            throw new CommonException(e.getMessage());
        }
        return dealUrl;
    }

    @Override
    public void deleteFile(String url) {
        try {
            String urlSlash = attachmentUrl.endsWith("/") ? "" : "/";
            fileFeignClient.deleteFile(BaseStage.BACKETNAME, attachmentUrl + urlSlash + URLDecoder.decode(url, "UTF-8"));
        } catch (Exception e) {
            LOGGER.error("error.attachment.delete", e);
        }
    }
}
