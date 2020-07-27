package io.choerodon.kb.app.service.impl;

import com.vladsch.flexmark.convert.html.FlexmarkHtmlParser;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.api.vo.*;
import io.choerodon.kb.app.service.*;
import io.choerodon.kb.infra.dto.PageAttachmentDTO;
import io.choerodon.kb.infra.dto.PageContentDTO;
import io.choerodon.kb.infra.dto.PageDTO;
import io.choerodon.kb.infra.mapper.PageAttachmentMapper;
import io.choerodon.kb.infra.mapper.PageContentMapper;
import io.choerodon.kb.infra.repository.PageRepository;
import io.choerodon.kb.infra.utils.PdfUtil;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.util.Charsets;
import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author shinan.chen
 * @since 2019/7/17
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
    private PageContentService pageContentService;
    @Autowired
    private PageContentMapper pageContentMapper;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PageAttachmentMapper pageAttachmentMapper;
    @Autowired
    private PageAttachmentService pageAttachmentService;
    @Autowired
    private PageVersionService pageVersionService;

    @Override
    public PageDTO createPage(Long organizationId, Long projectId, PageCreateWithoutContentVO pageCreateVO) {
        PageDTO pageDTO = new PageDTO();
        pageDTO.setTitle(pageCreateVO.getTitle());
        pageDTO.setOrganizationId(organizationId);
        pageDTO.setProjectId(projectId);
        pageDTO.setLatestVersionId(0L);
        pageDTO = pageRepository.baseCreate(pageDTO);
        Long latestVersionId = pageVersionService.createVersionAndContent(pageDTO.getId(), pageCreateVO.getTitle(), "", null, true, false);
        PageDTO page = pageRepository.selectById(pageDTO.getId());
        page.setLatestVersionId(latestVersionId);
        return pageRepository.baseUpdate(page, false);
    }

    @Override
    public WorkSpaceInfoVO createPageWithContent(Long organizationId, Long projectId, PageCreateVO create) {
        //创建页面及空间("第一次创建内容为空")
        PageUpdateVO pageUpdateVO = new PageUpdateVO();
        pageUpdateVO.setContent(create.getContent());
        WorkSpaceInfoVO workSpaceInfoVO = workSpaceService.createWorkSpaceAndPage(organizationId, projectId, modelMapper.map(create, PageCreateWithoutContentVO.class));
        // 创建新页面附件
        if (Objects.nonNull(create.getSourcePageId())){
            List<PageAttachmentDTO> attachmentList = pageAttachmentMapper.selectByPageId(create.getSourcePageId());
            createTargetAttachement(workSpaceInfoVO, attachmentList);
        }
        //更新页面内容
        pageUpdateVO.setMinorEdit(false);
        pageUpdateVO.setDescription(create.getDescription());
        pageUpdateVO.setObjectVersionNumber(workSpaceInfoVO.getPageInfo().getObjectVersionNumber());
        return workSpaceService.updateWorkSpaceAndPage(organizationId, projectId, workSpaceInfoVO.getId(), null, pageUpdateVO);
    }

    private void createTargetAttachement(WorkSpaceInfoVO workSpaceInfoVO, List<PageAttachmentDTO> attachmentList) {
        if (CollectionUtils.isNotEmpty(attachmentList)) {
            for (PageAttachmentDTO attachmentDTO : attachmentList) {
                attachmentDTO.setId(null);
                attachmentDTO.setPageId(workSpaceInfoVO.getPageInfo().getId());
            }
            pageAttachmentMapper.batchInsert(attachmentList);
        }
    }

    @Override
    public void exportMd2Pdf(Long organizationId, Long projectId, Long pageId, HttpServletResponse response) {
        PageInfoVO pageInfoVO = pageRepository.queryInfoById(organizationId, projectId, pageId);
        PdfUtil.markdown2Pdf(pageInfoVO.getTitle(), pageInfoVO.getContent(), response);
    }

    @Override
    public String importDocx2Md(Long organizationId, Long projectId, MultipartFile file) {
        if (!file.getOriginalFilename().endsWith(SUFFIX_DOCX)) {
            throw new CommonException(FILE_ILLEGAL);
        }
        WordprocessingMLPackage wordMLPackage;
        try {
            wordMLPackage = Docx4J.load(file.getInputStream());
            HTMLSettings htmlSettings = Docx4J.createHTMLSettings();
            htmlSettings.setWmlPackage(wordMLPackage);
            ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
            Docx4jProperties.setProperty("docx4j.openpackaging.parts.WordprocessingML.ObfuscatedFontPart.tmpFontDir","docx4TempFonts");
            Docx4J.toHTML(htmlSettings, swapStream, Docx4J.FLAG_EXPORT_PREFER_XSL);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(swapStream.toByteArray());
            String html = IOUtils.toString(inputStream, String.valueOf(Charsets.UTF_8));
            String markdown = FlexmarkHtmlParser.parse(html);
            return markdown;
        } catch (Docx4JException e) {
            String emptyWordMsg = "Error reading from the stream (no bytes available)";
            if (emptyWordMsg.equals(e.getMessage())) {
                throw new CommonException("error.import.word.empty", e);
            } else {
                throw new CommonException("error.import.docx2md",e);
            }
        } catch (Exception e) {
            throw new CommonException("error.import.docx2md",e);
        }
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
            pageContentService.baseCreate(pageContent);
        } else {
            //修改草稿内容
            pageContent.setContent(autoSave.getContent());
            pageContentService.baseUpdate(pageContent);
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
        if (userDetails != null) {
            Long userId = userDetails.getUserId();
            PageContentDTO pageContent = new PageContentDTO();
            pageContent.setPageId(pageId);
            pageContent.setVersionId(0L);
            pageContent.setCreatedBy(userId);
            pageContentMapper.delete(pageContent);
        }
    }

    @Override
    public void createByTemplate(Long organizationId, Long projectId,Long id, Long templateBaseId) {
      // 查询模板知识库下面所有的文件
      List<PageCreateVO> listTemplatePage = pageContentMapper.listTemplatePageByBaseId(0L,0L,templateBaseId);
      if(CollectionUtils.isEmpty(listTemplatePage)){
        return;
      }
      List<PageCreateVO> collect = listTemplatePage.stream().map(v -> {
            v.setBaseId(id);
            return v;
        }).collect(Collectors.toList());
      LinkedHashMap<Long, PageCreateVO> pageMap = collect.stream().collect(Collectors.toMap(PageCreateVO::getId, d -> d, (oldValue, newValue) -> newValue, LinkedHashMap::new));
      LinkedHashMap<Long, List<PageCreateVO>> parentMap = collect.stream().collect(Collectors.groupingBy(PageCreateVO::getParentWorkspaceId, LinkedHashMap::new, Collectors.toList()));
      List<PageCreateVO> pageCreateVOS = parentMap.get(0L);
      cycleInsert(organizationId,projectId,pageMap,parentMap,pageCreateVOS);
    }

    @Override
    public WorkSpaceInfoVO createPageByTemplate(Long organizationId, Long projectId, PageCreateVO pageCreateVO, Long templateWorkSpaceId) {
        if(templateWorkSpaceId == null){
            return workSpaceService.createWorkSpaceAndPage(organizationId, projectId, modelMapper.map(pageCreateVO, PageCreateWithoutContentVO.class));
        }
        else {
            PageContentDTO pageContentDTO = pageContentMapper.selectLatestByWorkSpaceId(templateWorkSpaceId);
            pageCreateVO.setContent(pageContentDTO.getContent());
            WorkSpaceInfoVO pageWithContent = createPageWithContent(organizationId, projectId, pageCreateVO);
            // 创建附件并返回
            List<PageAttachmentDTO> pageAttachmentDTOS = pageAttachmentMapper.selectByPageId(pageContentDTO.getPageId());
            if(!CollectionUtils.isEmpty(pageAttachmentDTOS)){
                pageWithContent.setPageAttachments(pageAttachmentService.copyAttach(pageWithContent.getPageInfo().getId(),pageAttachmentDTOS));
            }
            return pageWithContent;
        }
    }

    private void cycleInsert(Long organizationId, Long projectId, LinkedHashMap<Long, PageCreateVO> pageMap, LinkedHashMap<Long, List<PageCreateVO>> parentMap, List<PageCreateVO> pageCreateVOS) {
      if(!CollectionUtils.isEmpty(pageCreateVOS)){
          pageCreateVOS.forEach(v -> {
              Long id = v.getId();
              v.setId(null);
              WorkSpaceInfoVO pageWithContent = createPageWithContent(organizationId, projectId, v);
              List<PageCreateVO> list = parentMap.get(id);
              if(!CollectionUtils.isEmpty(list)){
                  List<PageCreateVO> collect = list.stream().map(pageCreateVO -> {
                      pageCreateVO.setParentWorkspaceId(pageWithContent.getId());
                      return pageCreateVO;
                  }).collect(Collectors.toList());
                  cycleInsert(organizationId,projectId,pageMap,parentMap,collect);
              }
          });
      }
    }


}
