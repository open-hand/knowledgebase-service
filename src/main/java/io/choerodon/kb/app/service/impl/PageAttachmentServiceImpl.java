package io.choerodon.kb.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.api.vo.PageAttachmentVO;
import io.choerodon.kb.app.service.PageAttachmentService;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.dto.PageAttachmentDTO;
import io.choerodon.kb.infra.dto.PageDTO;
import io.choerodon.kb.infra.utils.ExpandFileClient;
import io.choerodon.kb.infra.mapper.PageAttachmentMapper;
import io.choerodon.kb.infra.repository.PageAttachmentRepository;
import io.choerodon.kb.infra.repository.PageRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
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

    private PageRepository pageRepository;
    private PageAttachmentRepository pageAttachmentRepository;
    private ModelMapper modelMapper;
    private PageAttachmentMapper pageAttachmentMapper;
    private ExpandFileClient expandFileClient;

    public PageAttachmentServiceImpl(ExpandFileClient expandFileClient,
                                     PageRepository pageRepository,
                                     PageAttachmentRepository pageAttachmentRepository,
                                     ModelMapper modelMapper,
                                     PageAttachmentMapper pageAttachmentMapper) {
        this.expandFileClient = expandFileClient;
        this.pageRepository = pageRepository;
        this.pageAttachmentRepository = pageAttachmentRepository;
        this.modelMapper = modelMapper;
        this.pageAttachmentMapper = pageAttachmentMapper;
    }

    @Override
    public List<PageAttachmentVO> create(Long organizationId, Long projectId, Long pageId,
                                         List<MultipartFile> files) {
        List<Long> ids = new ArrayList<>();
        List<PageAttachmentVO> list = new ArrayList<>();
        PageDTO pageDTO = pageRepository.baseQueryById(organizationId, projectId, pageId);
        if (!(files != null && !files.isEmpty())) {
            throw new CommonException("error.attachment.exits");
        }
        for (MultipartFile multipartFile : files) {
            String fileName = multipartFile.getOriginalFilename();
            String url = expandFileClient.uploadFile(organizationId, BaseStage.BACKETNAME, null, fileName, multipartFile);
            ids.add(this.insertPageAttachment(organizationId, projectId, fileName, pageDTO.getId(), multipartFile.getSize(), dealUrl(url)).getId());
        }
        if (!ids.isEmpty()) {
            String urlSlash = attachmentUrl.endsWith("/") ? "" : "/";
            list = modelMapper.map(pageAttachmentMapper.selectIn(ids), new TypeToken<List<PageAttachmentVO>>() {
            }.getType());
            list.stream().forEach(p -> p.setUrl(attachmentUrl + urlSlash + p.getUrl()));
        }

        return list;
    }

    @Override
    public List<String> uploadForAddress(Long organizationId, List<MultipartFile> files) {
        if (!(files != null && !files.isEmpty())) {
            throw new CommonException("error.attachment.exits");
        }
        List<String> result = new ArrayList<>();
        String urlSlash = attachmentUrl.endsWith("/") ? "" : "/";
        for (MultipartFile multipartFile : files) {
            String fileName = multipartFile.getOriginalFilename();
            String url = expandFileClient.uploadFile(organizationId, BaseStage.BACKETNAME, null, fileName, multipartFile);
            result.add(attachmentUrl + urlSlash + dealUrl(url));
        }
        return result;
    }

    @Override
    public List<PageAttachmentVO> queryByList(Long organizationId, Long projectId, Long pageId) {
        pageRepository.baseQueryByIdWithOrg(organizationId, projectId, pageId);
        List<PageAttachmentDTO> pageAttachments = pageAttachmentMapper.selectByPageId(pageId);
        if (pageAttachments != null && !pageAttachments.isEmpty()) {
            String urlSlash = attachmentUrl.endsWith("/") ? "" : "/";
            pageAttachments.stream().forEach(pageAttachmentDO -> pageAttachmentDO.setUrl(attachmentUrl + urlSlash + pageAttachmentDO.getUrl()));
        }
        return modelMapper.map(pageAttachments, new TypeToken<List<PageAttachmentVO>>() {
        }.getType());
    }

    @Override
    public void delete(Long organizationId, Long projectId, Long id) {
        PageAttachmentDTO pageAttachmentDTO = pageAttachmentRepository.baseQueryById(id);
        pageRepository.checkById(organizationId, projectId, pageAttachmentDTO.getPageId());
        // 查询是否有引用，没有就删除文件服务器上面的文件，有就不删除
        PageAttachmentDTO pageAttachment = new PageAttachmentDTO();
        pageAttachment.setUrl(pageAttachmentDTO.getUrl());
        pageAttachment.setName(pageAttachmentDTO.getName());
        List<PageAttachmentDTO> attachmentDTOS = pageAttachmentMapper.select(pageAttachment);
        if (!CollectionUtils.isEmpty(attachmentDTOS) && attachmentDTOS.size() > 1) {
            pageAttachmentRepository.baseDelete(id);
            return;
        }
        pageAttachmentRepository.baseDelete(id);
        // 彻底删除文件服务器上面的文件
        String urlSlash = attachmentUrl.endsWith("/") ? "" : "/";
        String url = null;
        try {
            url = attachmentUrl + urlSlash + URLDecoder.decode(pageAttachmentDTO.getUrl(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new CommonException("error.url.decode", e);
        }
        expandFileClient.deleteFileByUrlWithDbOptional(organizationId, BaseStage.BACKETNAME, Arrays.asList(url));
    }

    @Override
    public PageAttachmentDTO insertPageAttachment(Long organizationId, Long projectId, String name, Long pageId, Long size, String url) {
        pageRepository.checkById(organizationId, projectId, pageId);
        PageAttachmentDTO pageAttachmentDTO = new PageAttachmentDTO();
        pageAttachmentDTO.setName(name);
        pageAttachmentDTO.setPageId(pageId);
        pageAttachmentDTO.setSize(size);
        pageAttachmentDTO.setUrl(url);
        return pageAttachmentRepository.baseCreate(pageAttachmentDTO);
    }

    @Override
    public String dealUrl(String url) {
        String dealUrl;
        try {
            URL netUrl = new URL(url);
            dealUrl = netUrl.getFile().substring(BaseStage.BACKETNAME.length() + 2);
        } catch (MalformedURLException e) {
            throw new CommonException(e.getMessage());
        }
        return dealUrl;
    }

    @Override
    public void deleteFile(Long organizationId, String url) {
        String urlSlash = attachmentUrl.endsWith("/") ? "" : "/";
        String fileUrl = null;
        try {
            fileUrl = attachmentUrl + urlSlash + URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new CommonException("error.url.decode", e);
        }
        expandFileClient.deleteFileByUrlWithDbOptional(organizationId, BaseStage.BACKETNAME, Arrays.asList(fileUrl));
    }

    @Override
    public PageAttachmentVO queryByFileName(Long organizationId, Long projectId, String fileName) {
        List<PageAttachmentDTO> result = pageAttachmentMapper.queryByFileName(organizationId, projectId, fileName, attachmentUrl);
        if (!result.isEmpty()) {
            return modelMapper.map(result.get(0), PageAttachmentVO.class);
        }
        return null;
    }

    @Override
    public void batchDelete(Long organizationId, Long projectId, List<Long> ids) {
        for (Long id : ids) {
            this.delete(organizationId, projectId, id);
        }
    }

    @Override
    public List<PageAttachmentDTO> batchInsert(List<PageAttachmentDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            throw new CommonException("error.batch.insert.attach.is.empty");
        }
        pageAttachmentMapper.batchInsert(list);
        return list;
    }

    @Override
    public List<PageAttachmentVO> copyAttach(Long pageId, List<PageAttachmentDTO> pageAttachmentDTOS) {
        if (!CollectionUtils.isEmpty(pageAttachmentDTOS)) {
            Long userId = DetailsHelper.getUserDetails().getUserId();
            pageAttachmentDTOS.forEach(attach -> {
                attach.setId(null);
                attach.setPageId(pageId);
                attach.setCreatedBy(userId);
                attach.setLastUpdatedBy(userId);
            });
            List<PageAttachmentDTO> attachmentDTOS = batchInsert(pageAttachmentDTOS);
            return modelMapper.map(attachmentDTOS, new TypeToken<List<PageAttachmentVO>>() {
            }.getType());
        }
        return new ArrayList<>();
    }
}
