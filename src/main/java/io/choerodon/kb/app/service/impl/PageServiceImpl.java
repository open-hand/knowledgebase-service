package io.choerodon.kb.app.service.impl;

import static io.choerodon.kb.infra.enums.PermissionConstants.PermissionTargetBaseType.KNOWLEDGE_BASE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;

import com.vladsch.flexmark.convert.html.FlexmarkHtmlParser;
import org.apache.commons.collections4.CollectionUtils;
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
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.api.vo.*;
import io.choerodon.kb.app.service.*;
import io.choerodon.kb.domain.repository.IamRemoteRepository;
import io.choerodon.kb.domain.repository.PageRepository;
import io.choerodon.kb.domain.repository.WorkSpaceRepository;
import io.choerodon.kb.domain.service.PermissionCheckDomainService;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.dto.PageAttachmentDTO;
import io.choerodon.kb.infra.dto.PageContentDTO;
import io.choerodon.kb.infra.dto.PageDTO;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.enums.PermissionConstants;
import io.choerodon.kb.infra.enums.WorkSpaceType;
import io.choerodon.kb.infra.mapper.PageAttachmentMapper;
import io.choerodon.kb.infra.mapper.PageContentMapper;
import io.choerodon.kb.infra.utils.EsRestUtil;
import io.choerodon.kb.infra.utils.PdfUtil;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import org.hzero.core.base.BaseConstants;

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
    private WorkSpaceRepository workSpaceRepository;
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
    @Autowired
    private IamRemoteRepository iamRemoteRepository;
    @Autowired
    private PermissionCheckDomainService permissionCheckDomainService;
    @Autowired
    private EsRestUtil esRestUtil;

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
    public WorkSpaceInfoVO createPageWithContent(Long organizationId, Long projectId, PageCreateVO create, boolean checkPermission) {
        //创建页面及空间("第一次创建内容为空")
        PageUpdateVO pageUpdateVO = new PageUpdateVO();
        pageUpdateVO.setContent(create.getContent());
        WorkSpaceInfoVO workSpaceInfoVO = workSpaceService.createWorkSpaceAndPage(organizationId, projectId, modelMapper.map(create, PageCreateWithoutContentVO.class), checkPermission);
        // 创建新页面附件
        if (Objects.nonNull(create.getSourcePageId())) {
            List<PageAttachmentDTO> attachmentList = pageAttachmentMapper.selectByPageId(create.getSourcePageId());
            createTargetAttachment(workSpaceInfoVO, attachmentList);
        }
        //更新页面内容
        pageUpdateVO.setMinorEdit(false);
        pageUpdateVO.setDescription(create.getDescription());
        pageUpdateVO.setObjectVersionNumber(workSpaceInfoVO.getPageInfo().getObjectVersionNumber());
        workSpaceService.updateWorkSpaceAndPage(organizationId, projectId, workSpaceInfoVO.getId(), null, pageUpdateVO, true);
        return workSpaceInfoVO;
    }

    private void createTargetAttachment(WorkSpaceInfoVO workSpaceInfoVO, List<PageAttachmentDTO> attachmentList) {
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
        WatermarkVO waterMark = queryWaterMarkConfigFromIam(organizationId);
        PdfUtil.markdown2Pdf(pageInfoVO.getTitle(), pageInfoVO.getContent(), response, waterMark);
    }

    private WatermarkVO queryWaterMarkConfigFromIam(Long organizationId) {
        WatermarkVO waterMark = iamRemoteRepository.getWaterMarkConfig(organizationId);
        if (waterMark == null) {
            waterMark = new WatermarkVO();
        }
        boolean doWaterMark = Boolean.TRUE.equals(waterMark.getEnable());
        waterMark.setDoWaterMark(doWaterMark);
        return waterMark;
    }

    @Override
    public String importDocx2Md(Long organizationId, Long projectId, Long baseId, Long parentWorkSpaceId, MultipartFile file,
                                boolean templateFlag) {
        if (!Objects.requireNonNull(file.getOriginalFilename()).endsWith(SUFFIX_DOCX)) {
            throw new CommonException(FILE_ILLEGAL);
        }
        PermissionConstants.PermissionTargetBaseType permissionTargetBaseType = KNOWLEDGE_BASE;
        if (parentWorkSpaceId != null && !parentWorkSpaceId.equals(0L)) {
            WorkSpaceDTO parentWorkSpace = this.workSpaceRepository.baseQueryById(organizationId, projectId, parentWorkSpaceId);
            permissionTargetBaseType = PermissionConstants.PermissionTargetBaseType.ofWorkSpaceType(WorkSpaceType.of(parentWorkSpace.getType()));
        }
        // 鉴定是否含有上级的管理权限
        if (!templateFlag) {
            Assert.isTrue(permissionCheckDomainService.checkPermission(organizationId,
                    projectId,
                    permissionTargetBaseType.toString(),
                    null,
                    permissionTargetBaseType == KNOWLEDGE_BASE ? baseId : parentWorkSpaceId,
                    PermissionConstants.ActionPermission.DOCUMENT_CREATE.getCode()), BaseConstants.ErrorCode.FORBIDDEN);
        }

        WordprocessingMLPackage wordMLPackage;
        try {
            wordMLPackage = Docx4J.load(file.getInputStream());
            HTMLSettings htmlSettings = Docx4J.createHTMLSettings();
            htmlSettings.setWmlPackage(wordMLPackage);
            ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
            Docx4jProperties.setProperty("docx4j.openpackaging.parts.WordprocessingML.ObfuscatedFontPart.tmpFontDir", "docx4TempFonts");
            Docx4J.toHTML(htmlSettings, swapStream, Docx4J.FLAG_EXPORT_PREFER_XSL);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(swapStream.toByteArray());
            String html = IOUtils.toString(inputStream, String.valueOf(Charsets.UTF_8));
            return FlexmarkHtmlParser.parse(html);
        } catch (Docx4JException e) {
            String emptyWordMsg = "Error reading from the stream (no bytes available)";
            if (emptyWordMsg.equals(e.getMessage())) {
                throw new CommonException("error.import.word.empty", e);
            } else {
                throw new CommonException("error.import.docx2md", e);
            }
        } catch (Exception e) {
            throw new CommonException("error.import.docx2md", e);
        }
    }

    @Override
    public void autoSavePage(Long organizationId, Long projectId, Long pageId, PageAutoSaveVO autoSave) {
        PageContentDTO pageContent = this.pageRepository.queryDraftContent(organizationId, projectId, pageId);
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
    public void createByTemplate(Long organizationId, Long projectId, Long id, Long templateBaseId, boolean checkPermission) {
        // 查询模板知识库下面所有的文件
        List<PageCreateVO> listTemplatePage = pageContentMapper.listTemplatePageByBaseId(0L, 0L, templateBaseId);
        if (CollectionUtils.isEmpty(listTemplatePage)) {
            return;
        }
        List<PageCreateVO> collect = listTemplatePage.stream().peek(v -> v.setBaseId(id)).collect(Collectors.toList());
        LinkedHashMap<Long, List<PageCreateVO>> parentMap = collect.stream().collect(Collectors.groupingBy(PageCreateVO::getParentWorkspaceId, LinkedHashMap::new, Collectors.toList()));
        List<PageCreateVO> pageCreateVOS = parentMap.get(0L);
        cycleInsert(organizationId, projectId, parentMap, pageCreateVOS, checkPermission);
    }

    @Override
    public WorkSpaceInfoVO createPageByTemplate(Long organizationId, Long projectId, PageCreateVO pageCreateVO, Long templateWorkSpaceId) {
        if (templateWorkSpaceId == null) {
            return workSpaceService.createWorkSpaceAndPage(organizationId, projectId, modelMapper.map(pageCreateVO, PageCreateWithoutContentVO.class), true);
        } else {
            PageContentDTO pageContentDTO = pageContentMapper.selectLatestByWorkSpaceId(templateWorkSpaceId);
            pageCreateVO.setContent(pageContentDTO.getContent());
            WorkSpaceInfoVO pageWithContent = createPageWithContent(organizationId, projectId, pageCreateVO, true);
            // 创建附件并返回
            List<PageAttachmentDTO> pageAttachmentDTOS = pageAttachmentMapper.selectByPageId(pageContentDTO.getPageId());
            if (CollectionUtils.isNotEmpty(pageAttachmentDTOS)) {
                pageWithContent.setPageAttachments(pageAttachmentService.copyAttach(pageWithContent.getPageInfo().getId(), pageAttachmentDTOS));
            }
            return pageWithContent;
        }
    }


    @Override
    public List<FullTextSearchResultVO> fullTextSearch(PageRequest pageRequest, Long organizationId, Long projectId, Long baseId, String searchStr) {
        // 鉴定是否含有此知识库的阅读权，如果有阅读权，则可搜索，否则不可搜索
        Assert.isTrue(permissionCheckDomainService.checkPermission(organizationId,
                projectId,
                PermissionConstants.PermissionTargetBaseType.KNOWLEDGE_BASE.toString(),
                null,
                baseId,
                PermissionConstants.ActionPermission.KNOWLEDGE_BASE_READ.getCode()), BaseConstants.ErrorCode.FORBIDDEN);
        return esRestUtil.fullTextSearch(organizationId, projectId, BaseStage.ES_PAGE_INDEX, searchStr, baseId, pageRequest);
    }

    private void cycleInsert(Long organizationId,
                             Long projectId,
                             LinkedHashMap<Long, List<PageCreateVO>> parentMap,
                             List<PageCreateVO> pageCreateVOS,
                             boolean checkPermission) {
        if (CollectionUtils.isNotEmpty(pageCreateVOS)) {
            for (PageCreateVO pageCreateVO : pageCreateVOS) {
                Long id = pageCreateVO.getId();
                pageCreateVO.setId(null);
                pageCreateVO.setType(WorkSpaceType.DOCUMENT.getValue());
                WorkSpaceInfoVO pageWithContent = createPageWithContent(organizationId, projectId, pageCreateVO, checkPermission);
                List<PageCreateVO> list = parentMap.get(id);
                if (CollectionUtils.isNotEmpty(list)) {
                    List<PageCreateVO> collect = list.stream().peek(vo -> vo.setParentWorkspaceId(pageWithContent.getId())).collect(Collectors.toList());
                    cycleInsert(organizationId, projectId, parentMap, collect, checkPermission);
                }
            }
        }
    }


}
