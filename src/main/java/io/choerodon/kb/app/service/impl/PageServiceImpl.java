package io.choerodon.kb.app.service.impl;

import com.vladsch.flexmark.convert.html.FlexmarkHtmlParser;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.api.dao.PageCreateDTO;
import io.choerodon.kb.api.dao.PageDTO;
import io.choerodon.kb.api.dao.PageInfo;
import io.choerodon.kb.api.dao.PageUpdateDTO;
import io.choerodon.kb.app.service.PageService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.domain.kb.repository.PageContentRepository;
import io.choerodon.kb.domain.kb.repository.PageRepository;
import io.choerodon.kb.infra.common.utils.Markdown2HtmlUtil;
import io.choerodon.kb.infra.common.utils.PdfUtil;
import io.choerodon.kb.infra.dataobject.PageDO;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.util.Charsets;
import org.docx4j.Docx4J;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
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

    private PageRepository pageRepository;
    private PageContentRepository pageContentRepository;

    public PageServiceImpl(PageRepository pageRepository,
                           PageContentRepository pageContentRepository) {
        this.pageRepository = pageRepository;
        this.pageContentRepository = pageContentRepository;
    }

    @Override
    public Boolean checkPageCreate(Long id) {
        PageDO pageDO = pageRepository.selectById(id);
        CustomUserDetails customUserDetails = DetailsHelper.getUserDetails();
        return pageDO.getCreatedBy().equals(customUserDetails.getUserId());
    }

    @Override
    public String pageToc(Long id) {
        PageDO pageDO = pageRepository.selectById(id);
        return Markdown2HtmlUtil.toc(pageContentRepository.selectByVersionId(pageDO.getLatestVersionId(), pageDO.getId()).getContent());
    }

    @Override
    public void exportMd2Pdf(Long organizationId, Long projectId, Long pageId, HttpServletResponse response) {
        PageInfo pageInfo = pageRepository.queryInfoById(organizationId, projectId, pageId);
        PdfUtil.markdown2Pdf(pageInfo.getTitle(), pageInfo.getContent(), response);
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
    public PageDTO createPage(Long resourceId, PageCreateDTO create, String type) {
        //创建页面及空间
        PageUpdateDTO pageUpdateDTO = new PageUpdateDTO();
        pageUpdateDTO.setContent(create.getContent());
        create.setContent("");
        PageDTO pageDTO = workSpaceService.create(resourceId, create, type);
        //更新页面内容
        pageUpdateDTO.setMinorEdit(false);
        pageUpdateDTO.setObjectVersionNumber(pageDTO.getObjectVersionNumber());
        return workSpaceService.update(resourceId, pageDTO.getWorkSpace().getId(), pageUpdateDTO, type);
    }
}
