package io.choerodon.kb.app.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.dao.PageCreateDTO;
import io.choerodon.kb.api.dao.PageDTO;
import io.choerodon.kb.api.dao.PageUpdateDTO;
import io.choerodon.kb.api.dao.WorkSpaceTreeDTO;
import io.choerodon.kb.api.validator.WorkSpaceValidator;
import io.choerodon.kb.app.service.PageService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.domain.kb.entity.*;
import io.choerodon.kb.domain.kb.repository.*;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.common.enums.PageResourceType;
import io.choerodon.kb.infra.common.utils.Markdown2HtmlUtil;
import io.choerodon.kb.infra.common.utils.RankUtil;
import io.choerodon.kb.infra.common.utils.Version;

/**
 * Created by Zenger on 2019/4/30.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class WorkSpaceServiceImpl implements WorkSpaceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkSpaceServiceImpl.class);

    private WorkSpaceValidator workSpaceValidator;
    private PageService pageService;
    private PageRepository pageRepository;
    private PageVersionRepository pageVersionRepository;
    private PageContentRepository pageContentRepository;
    private WorkSpaceRepository workSpaceRepository;
    private WorkSpacePageRepository workSpacePageRepository;

    public WorkSpaceServiceImpl(WorkSpaceValidator workSpaceValidator,
                                PageService pageService,
                                PageRepository pageRepository,
                                PageVersionRepository pageVersionRepository,
                                PageContentRepository pageContentRepository,
                                WorkSpacePageRepository workSpacePageRepository,
                                WorkSpaceRepository workSpaceRepository) {
        this.workSpaceValidator = workSpaceValidator;
        this.pageService = pageService;
        this.pageRepository = pageRepository;
        this.pageVersionRepository = pageVersionRepository;
        this.pageContentRepository = pageContentRepository;
        this.workSpacePageRepository = workSpacePageRepository;
        this.workSpaceRepository = workSpaceRepository;
    }

    @Override
    public PageDTO create(Long resourceId, PageCreateDTO pageCreateDTO, String type) {
        LOGGER.info("start create page...");

        WorkSpaceE workSpaceE = new WorkSpaceE();
        PageE pageE = new PageE();
        pageE.setTitle(pageCreateDTO.getTitle());
        if (PageResourceType.ORGANIZATION.getResourceType().equals(type)) {
            pageE.setOrganizationId(resourceId);
            workSpaceE.setOrganizationId(resourceId);
        } else {
            pageE.setProjectId(resourceId);
            workSpaceE.setProjectId(resourceId);
        }

        PageE page = this.insertPage(pageE, pageCreateDTO);
        WorkSpaceE workSpace = this.insertWorkSpace(workSpaceE, page, resourceId, pageCreateDTO, type);
        this.insertWorkSpacePage(page.getId(), workSpace.getId());

        return ConvertHelper.convert(workSpaceRepository.queryDetail(workSpace.getId()), PageDTO.class);
    }

    @Override
    public PageDTO queryDetail(Long resourceId, Long id, String type) {
        this.checkWorkSpaceBelong(resourceId, id, type);
        WorkSpacePageE workSpacePageE = workSpacePageRepository.selectByWorkSpaceId(id);
        if (workSpacePageE == null) {
            throw new CommonException("error.workSpacePage.select");
        }
        PageDetailE pageDetailE = new PageDetailE();
        String referenceType = workSpacePageE.getReferenceType();
        switch (referenceType) {
            case BaseStage.REFERENCE_PAGE:
                pageDetailE = workSpaceRepository.queryDetail(id);
                break;
            case BaseStage.REFERENCE_URL:
                pageDetailE = workSpaceRepository.queryReferenceDetail(id);
                break;
            case BaseStage.SELF:
                pageDetailE = workSpaceRepository.queryDetail(id);
                break;
            default:
                break;
        }
        return ConvertHelper.convert(pageDetailE, PageDTO.class);
    }

    @Override
    public PageDTO update(Long resourceId, Long id, PageUpdateDTO pageUpdateDTO, String type) {
        this.checkWorkSpaceBelong(resourceId, id, type);
        WorkSpacePageE workSpacePageE = workSpaceValidator.checkUpdatePage(pageUpdateDTO, id);
        if (BaseStage.SELF.equals(workSpacePageE.getReferenceType())) {
            PageE pageE = pageRepository.selectById(workSpacePageE.getPageId());
            if (pageE == null) {
                throw new CommonException("error.page.select");
            }
            if (!pageE.getLatestVersionId().equals(pageUpdateDTO.getVersionId())) {
                throw new CommonException("error.page.version.different");
            }

            this.updatePageInfo(id, pageUpdateDTO, pageE);
        } else if (BaseStage.REFERENCE_URL.equals(workSpacePageE.getReferenceType())) {
            workSpacePageE.setReferenceUrl(pageUpdateDTO.getReferenceUrl());
            workSpacePageRepository.update(workSpacePageE);
        }

        return ConvertHelper.convert(workSpaceRepository.queryDetail(id), PageDTO.class);
    }

    @Override
    public void delete(Long resourceId, Long id, String type) {
        this.checkWorkSpaceBelong(resourceId, id, type);
        WorkSpaceE workSpaceE = this.selectWorkSpaceById(id);
        WorkSpacePageE workSpacePageE = workSpacePageRepository.selectByWorkSpaceId(id);
        if (workSpacePageE == null) {
            throw new CommonException("error.workSpacePage.select");
        }
        if (!pageService.checkPageCreate(workSpacePageE.getPageId())) {
            throw new CommonException("error.page.creator");
        }

        workSpaceRepository.deleteByRoute(workSpaceE.getRoute());
        workSpacePageRepository.delete(workSpacePageE.getId());
        pageRepository.delete(workSpacePageE.getPageId());
        pageVersionRepository.deleteByPageId(workSpacePageE.getPageId());
        pageContentRepository.deleteByPageId(workSpacePageE.getPageId());
    }

    @Override
    public List<WorkSpaceTreeDTO> queryByTree(Long resourceId, List<Long> parentIds, String type) {
        List<WorkSpaceE> workSpaceEList = workSpaceRepository.workSpaceListByParentIds(resourceId, parentIds, type);
        return getWorkSpaceTreeList(workSpaceEList, resourceId, type, 0L);
    }

    private WorkSpaceE selectWorkSpaceById(Long id) {
        LOGGER.info("select work space by id:{}", id);
        WorkSpaceE workSpaceE = workSpaceRepository.selectById(id);
        if (workSpaceE == null) {
            throw new CommonException("error.work.space.select");
        }
        return workSpaceE;
    }

    private List<WorkSpaceTreeDTO> getWorkSpaceTreeList(List<WorkSpaceE> workSpaceEList, Long resourceId, String type, Long level) {
        List<WorkSpaceTreeDTO> workSpaceTreeDTOList = new ArrayList<>();
        ++level;
        for (WorkSpaceE w : workSpaceEList) {
            WorkSpaceTreeDTO workSpaceTreeDTO = new WorkSpaceTreeDTO();
            WorkSpaceTreeDTO.Data data = new WorkSpaceTreeDTO.Data();
            workSpaceTreeDTO.setId(w.getId());
            data.setTitle(w.getName());
            List<WorkSpaceE> list = workSpaceRepository.workSpaceListByParentId(resourceId, w.getId(), type);
            if (list.isEmpty()) {
                workSpaceTreeDTO.setHasChildren(false);
                workSpaceTreeDTO.setChildren(Collections.emptyList());
            } else {
                workSpaceTreeDTO.setHasChildren(true);
                List<Long> children = list.stream().map(WorkSpaceE::getId).collect(Collectors.toList());
                workSpaceTreeDTO.setChildren(children);
                if (level <= 1) {
                    workSpaceTreeDTOList.addAll(getWorkSpaceTreeList(list, resourceId, type, level));
                }
            }
            workSpaceTreeDTO.setData(data);
            workSpaceTreeDTOList.add(workSpaceTreeDTO);
        }
        return workSpaceTreeDTOList;
    }

    private PageE insertPage(PageE pageE, PageCreateDTO pageCreateDTO) {
        pageE.setLatestVersionId(0L);
        pageE = pageRepository.insert(pageE);

        PageVersionE pageVersionE = new PageVersionE();
        pageVersionE.setName("1.1");
        pageVersionE.setPageId(pageE.getId());
        pageVersionE = pageVersionRepository.insert(pageVersionE);

        PageContentE pageContentE = new PageContentE();
        pageContentE.setPageId(pageE.getId());
        pageContentE.setVersionId(pageVersionE.getId());
        pageContentE.setContent(pageCreateDTO.getContent());
        pageContentE.setDrawContent(Markdown2HtmlUtil.markdown2Html(pageCreateDTO.getContent()));
        pageContentRepository.insert(pageContentE);

        PageE page = pageRepository.selectById(pageE.getId());
        page.setLatestVersionId(pageVersionE.getId());
        return pageRepository.update(page);
    }

    private WorkSpaceE insertWorkSpace(WorkSpaceE workSpaceE,
                                       PageE pageE,
                                       Long resourceId,
                                       PageCreateDTO pageCreateDTO,
                                       String type) {
        workSpaceE.setName(pageE.getTitle());
        Long parentId = 0L;
        String route = "";
        if (pageCreateDTO.getWorkspaceId() != 0) {
            WorkSpaceE parentWorkSpace = this.selectWorkSpaceById(pageCreateDTO.getWorkspaceId());
            parentId = parentWorkSpace.getId();
            route = parentWorkSpace.getRoute();
        }
        if (workSpaceRepository.hasChildWorkSpace(type, resourceId, parentId)) {
            String rank = workSpaceRepository.queryMaxRank(type, resourceId, parentId);
            workSpaceE.setRank(RankUtil.genNext(rank));
        } else {
            workSpaceE.setRank(RankUtil.mid());
        }
        workSpaceE.setParentId(parentId);
        workSpaceE = workSpaceRepository.inset(workSpaceE);

        String realRoute = route.isEmpty() ? workSpaceE.getId().toString() : route + "." + workSpaceE.getId();
        WorkSpaceE workSpace = workSpaceRepository.selectById(workSpaceE.getId());
        workSpace.setRoute(realRoute);
        return workSpaceRepository.update(workSpace);
    }

    private WorkSpacePageE insertWorkSpacePage(Long pageId, Long workSpaceId) {
        WorkSpacePageE workSpacePageE = new WorkSpacePageE();
        workSpacePageE.setReferenceType(BaseStage.SELF);
        workSpacePageE.setPageId(pageId);
        workSpacePageE.setWorkspaceId(workSpaceId);
        return workSpacePageRepository.insert(workSpacePageE);
    }

    private void updatePageInfo(Long id, PageUpdateDTO pageUpdateDTO, PageE pageE) {
        WorkSpaceE workSpaceE = this.selectWorkSpaceById(id);
        if (pageUpdateDTO.getContent() != null) {
            PageVersionE pageVersionE = pageVersionRepository.selectById(pageE.getLatestVersionId());
            String newVersionName = incrementVersion(pageVersionE.getName(), pageUpdateDTO.getMinorEdit());
            PageVersionE newPageVersion = new PageVersionE();
            newPageVersion.setPageId(pageE.getId());
            newPageVersion.setName(newVersionName);
            newPageVersion = pageVersionRepository.insert(newPageVersion);

            PageContentE newPageContent = new PageContentE();
            newPageContent.setPageId(pageE.getId());
            newPageContent.setVersionId(newPageVersion.getId());
            newPageContent.setContent(pageUpdateDTO.getContent());
            newPageContent.setDrawContent(Markdown2HtmlUtil.markdown2Html(pageUpdateDTO.getContent()));
            pageContentRepository.insert(newPageContent);

            pageE.setLatestVersionId(newPageVersion.getId());
        }
        if (pageUpdateDTO.getTitle() != null) {
            pageE.setTitle(pageUpdateDTO.getTitle());
            workSpaceE.setName(pageUpdateDTO.getTitle());
            workSpaceRepository.update(workSpaceE);
        }
        pageRepository.update(pageE);
    }

    private String incrementVersion(String versionName, Boolean isMinorEdit) {
        if (isMinorEdit) {
            return new Version(versionName).next().toString();
        } else {
            return new Version(versionName).getBranchPoint().next().newBranch(1).toString();
        }
    }

    private void checkWorkSpaceBelong(Long resourceId, Long id, String type) {
        WorkSpaceE workSpaceE = this.selectWorkSpaceById(id);
        if (PageResourceType.ORGANIZATION.getResourceType().equals(type) && !workSpaceE.getOrganizationId().equals(resourceId)) {
            throw new CommonException("The workspace not found in the organization");
        } else if (PageResourceType.PROJECT.getResourceType().equals(type) && !workSpaceE.getProjectId().equals(resourceId)) {
            throw new CommonException("The workspace not found in the project");
        }
    }
}
