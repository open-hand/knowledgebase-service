package io.choerodon.kb.app.service.impl;

import com.vladsch.flexmark.convert.html.FlexmarkHtmlParser;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.api.dao.*;
import io.choerodon.kb.app.service.PageService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.domain.kb.repository.PageContentRepository;
import io.choerodon.kb.domain.kb.repository.PageRepository;
import io.choerodon.kb.infra.dto.PageContentDTO;
import io.choerodon.kb.infra.dto.PageDTO;
import io.choerodon.kb.infra.mapper.PageContentMapper;
import io.choerodon.kb.infra.utils.PdfUtil;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.util.Charsets;
import org.docx4j.Docx4J;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Created by Zenger on 2019/4/30.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class PageServiceImpl implements PageService {

    public static final Logger LOGGER = LoggerFactory.getLogger(PageServiceImpl.class);
    public static final String SUFFIX_DOCX = ".docx";
    public static final String FILE_ILLEGAL = "error.importDocx2Md.fileIllegal";
    @Value("${services.attachment.url}")
    private String attachmentUrl;
    @Autowired
    private WorkSpaceService workSpaceService;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private PageContentRepository pageContentRepository;
    @Autowired
    private PageContentMapper pageContentMapper;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public Boolean checkPageCreate(Long id) {
        PageDTO pageDTO = pageRepository.selectById(id);
        CustomUserDetails customUserDetails = DetailsHelper.getUserDetails();
        return pageDTO.getCreatedBy().equals(customUserDetails.getUserId());
    }

    @Override
    public void exportMd2Pdf(Long organizationId, Long projectId, Long pageId, HttpServletResponse response) {
        PageInfoVO pageInfoVO = pageRepository.queryInfoById(organizationId, projectId, pageId);
        PdfUtil.markdown2Pdf(pageInfoVO.getTitle(), pageInfoVO.getContent(), response);
    }

    @Override
    public String importDocx2Md(Long organizationId, Long projectId, MultipartFile file, String type) {
        if (!file.getOriginalFilename().endsWith(SUFFIX_DOCX)) {
            throw new CommonException(FILE_ILLEGAL);
        }
        WordprocessingMLPackage wordMLPackage;
        try {
            wordMLPackage = Docx4J.load(file.getInputStream());
            HTMLSettings htmlSettings = Docx4J.createHTMLSettings();
            htmlSettings.setWmlPackage(wordMLPackage);
            ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
            Docx4J.toHTML(htmlSettings, swapStream, Docx4J.FLAG_EXPORT_PREFER_XSL);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(swapStream.toByteArray());
            String html = IOUtils.toString(inputStream, String.valueOf(Charsets.UTF_8));
            String markdown = FlexmarkHtmlParser.parse(html);
            return markdown;
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public PageVO createPage(Long resourceId, PageCreateVO create, String type) {
        //创建页面及空间("第一次创建版本为空")
        PageUpdateVO pageUpdateVO = new PageUpdateVO();
        pageUpdateVO.setContent(create.getContent());
        create.setContent("");
        PageVO pageVO = workSpaceService.create(resourceId, create, type);
        //更新页面内容
        pageUpdateVO.setMinorEdit(false);
        pageUpdateVO.setObjectVersionNumber(pageVO.getObjectVersionNumber());
        return workSpaceService.update(resourceId, pageVO.getWorkSpace().getId(), pageUpdateVO, type);
    }

    @Override
    public void autoSavePage(Long organizationId, Long projectId, Long pageId, PageAutoSaveVO autoSave) {
        PageContentDTO pageContent = queryDraftContent(organizationId, projectId, pageId);
        if (pageContent == null) {
            //创建草稿内容
            pageContent = new PageContentDTO();
            pageContent.setPageId(pageId);
            pageContent.setVersionId(0L);
            pageContent.setContent(autoSave.getContent());
            pageContentRepository.create(pageContent);
        } else {
            //修改草稿内容
            pageContent.setContent(autoSave.getContent());
            pageContentRepository.update(pageContent);
        }
    }

    @Override
    public PageContentDTO queryDraftContent(Long organizationId, Long projectId, Long pageId) {
        pageRepository.checkById(organizationId, projectId, pageId);
        CustomUserDetails userDetails = DetailsHelper.getUserDetails();
        if (userDetails == null) {
            return null;
        }
        Long userId = userDetails.getUserId();
        PageContentDTO pageContent = new PageContentDTO();
        pageContent.setPageId(pageId);
        pageContent.setVersionId(0L);
        pageContent.setCreatedBy(userId);
        List<PageContentDTO> contents = pageContentMapper.select(pageContent);
        return contents.isEmpty() ? null : contents.get(0);
    }

    @Override
    public void deleteDraftContent(Long organizationId, Long projectId, Long pageId) {
        pageRepository.checkById(organizationId, projectId, pageId);
        CustomUserDetails userDetails = DetailsHelper.getUserDetails();
        Long userId = userDetails.getUserId();
        PageContentDTO pageContent = new PageContentDTO();
        pageContent.setPageId(pageId);
        pageContent.setVersionId(0L);
        pageContent.setCreatedBy(userId);
        pageContentMapper.delete(pageContent);
    }
}
