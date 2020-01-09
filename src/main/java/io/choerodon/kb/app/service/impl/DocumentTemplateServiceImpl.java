package io.choerodon.kb.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.choerodon.kb.api.vo.*;
import io.choerodon.kb.app.service.DocumentTemplateService;
import io.choerodon.kb.app.service.PageAttachmentService;
import io.choerodon.kb.app.service.PageService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.app.service.assembler.DocumentTemplateAssembler;
import io.choerodon.kb.infra.dto.PageContentDTO;
import io.choerodon.kb.infra.feign.vo.UserDO;
import io.choerodon.kb.infra.mapper.KnowledgeBaseMapper;
import io.choerodon.kb.infra.mapper.PageContentMapper;
import io.choerodon.kb.infra.mapper.WorkSpaceMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author zhaotianxin
 * @since 2020/1/2
 */
@Service
public class DocumentTemplateServiceImpl implements DocumentTemplateService {
    private static final String CUSTOM = "custom";

    private static final String SYS_PRESET = "sys_preset";
    @Autowired
    private WorkSpaceService workSpaceService;

    @Autowired
    private WorkSpaceMapper workSpaceMapper;

    @Autowired
    private DocumentTemplateAssembler documentTemplateAssembler;

    @Autowired
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Autowired
    private PageAttachmentService pageAttachmentService;

    @Autowired
    private PageContentMapper pageContentMapper;
    
    @Autowired
    private PageService pageService;
    
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public DocumentTemplateInfoVO createTemplate(Long projectId, Long organizationId, PageCreateWithoutContentVO pageCreateVO,Long baseTemplateId) {
        if(baseTemplateId == null){
            WorkSpaceInfoVO workSpaceAndPage = workSpaceService.createWorkSpaceAndPage(organizationId, projectId, pageCreateVO);
            List<Long> userIds = new ArrayList<>();
            userIds.add(workSpaceAndPage.getCreatedBy());
            userIds.add(workSpaceAndPage.getPageInfo().getLastUpdatedBy());
            Map<Long, UserDO> users = documentTemplateAssembler.findUsers(userIds);
            DocumentTemplateInfoVO documentTemplateInfoVO = new DocumentTemplateInfoVO(workSpaceAndPage.getId(),workSpaceAndPage.getPageInfo().getTitle()
                    ,workSpaceAndPage.getDescription(),workSpaceAndPage.getCreatedBy(),workSpaceAndPage.getPageInfo().getLastUpdatedBy()
                    ,CUSTOM,workSpaceAndPage.getCreationDate(),workSpaceAndPage.getPageInfo().getLastUpdateDate(),workSpaceAndPage.getObjectVersionNumber());
            return documentTemplateAssembler.toTemplateInfoVO(users,documentTemplateInfoVO);
        }
        else {
            return createByTemplate(projectId,organizationId,pageCreateVO,baseTemplateId);
        }
    }

    @Override
    public WorkSpaceInfoVO updateTemplate(Long organizationId, Long projectId, Long id, String searchStr, PageUpdateVO pageUpdateVO) {
        return workSpaceService.updateWorkSpaceAndPage(organizationId, projectId, id, searchStr, pageUpdateVO);
    }

    @Override
    public PageInfo<DocumentTemplateInfoVO> listTemplate(Long organizationId, Long projectId,Long baseId, Pageable pageable, SearchVO searchVO) {
        PageInfo<DocumentTemplateInfoVO> pageInfo = PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize()).doSelectPageInfo(() -> workSpaceMapper.listDocumentTemplate(organizationId, projectId, baseId, searchVO));

        if (CollectionUtils.isEmpty(pageInfo.getList())) {
            return new PageInfo<DocumentTemplateInfoVO>();
        }
        List<DocumentTemplateInfoVO> list = pageInfo.getList();
        List<Long> userIds = new ArrayList<>();
        list.forEach(v -> {
            userIds.add(v.getCreatedBy());
            userIds.add(v.getLastUpdatedBy());
        });
        Map<Long, UserDO> users = documentTemplateAssembler.findUsers(userIds);
        list.forEach(v -> documentTemplateAssembler.toTemplateInfoVO(users,v));
        pageInfo.setList(list);
        return pageInfo;
    }

    @Override
    public List<KnowledgeBaseTreeVO> listSystemTemplate(Long organizationId, Long projectId, SearchVO searchVO) {
            List<KnowledgeBaseTreeVO> knowledgeBaseTreeVOS = knowledgeBaseMapper.listSystemTemplateBase(searchVO);
            if (CollectionUtils.isEmpty(knowledgeBaseTreeVOS)) {
                return new ArrayList<>();
            }
            List<Long> baseIds = knowledgeBaseTreeVOS.stream().map(v -> v.getId()).collect(Collectors.toList());
            List<KnowledgeBaseTreeVO> childrenWorkSpace = workSpaceService.listSystemTemplateBase(baseIds);
            knowledgeBaseTreeVOS.addAll(childrenWorkSpace);
            return knowledgeBaseTreeVOS;
    }

    @Override
    public List<PageAttachmentVO> createAttachment(Long organizationId, Long projectId, Long pageId, List<MultipartFile> file) {
        return pageAttachmentService.create(organizationId,projectId,pageId,file);
    }

    @Override
    public void removeWorkSpaceAndPage(Long organizationId, Long projectId, Long id, boolean isAdmin) {
        workSpaceService.removeWorkSpaceAndPage(organizationId,projectId,id,isAdmin);
    }

    @Override
    public void deleteAttachment(long organizationId, Long projectId, Long id) {
        pageAttachmentService.delete(organizationId,projectId,id);
    }

    @Override
    public DocumentTemplateInfoVO createByTemplate(Long projectId, Long organizationId, PageCreateWithoutContentVO pageCreateVO, Long templateId) {
        PageCreateVO map = modelMapper.map(pageCreateVO, PageCreateVO.class);
        PageContentDTO pageContentDTO = pageContentMapper.selectLatestByWorkSpaceId(templateId);
        map.setContent(pageContentDTO.getContent());
        WorkSpaceInfoVO pageWithContent = pageService.createPageWithContent(organizationId,projectId, map);
        DocumentTemplateInfoVO documentTemplateInfoVO = new DocumentTemplateInfoVO(pageWithContent.getId(),pageWithContent.getPageInfo().getTitle()
                ,pageWithContent.getDescription(),pageWithContent.getCreatedBy(),pageWithContent.getPageInfo().getLastUpdatedBy()
                ,pageWithContent.getCreateUser(),pageWithContent.getPageInfo().getLastUpdatedUser()
                ,CUSTOM,pageWithContent.getCreationDate(),pageWithContent.getPageInfo().getLastUpdateDate(),pageWithContent.getObjectVersionNumber());
        return documentTemplateInfoVO;
    }
}
