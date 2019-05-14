package io.choerodon.kb.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.dao.*;
import io.choerodon.kb.api.validator.WorkSpaceValidator;
import io.choerodon.kb.app.service.PageService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.domain.kb.entity.*;
import io.choerodon.kb.domain.kb.repository.*;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.common.enums.PageResourceType;
import io.choerodon.kb.infra.common.utils.Markdown2HtmlUtil;
import io.choerodon.kb.infra.common.utils.RankUtil;
import io.choerodon.kb.infra.common.utils.TypeUtil;
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
    private PageCommentRepository pageCommentRepository;
    private PageAttachmentRepository pageAttachmentRepository;
    private PageTagRepository pageTagRepository;
    private WorkSpaceRepository workSpaceRepository;
    private WorkSpacePageRepository workSpacePageRepository;

    public WorkSpaceServiceImpl(WorkSpaceValidator workSpaceValidator,
                                PageService pageService,
                                PageRepository pageRepository,
                                PageCommentRepository pageCommentRepository,
                                PageVersionRepository pageVersionRepository,
                                PageContentRepository pageContentRepository,
                                PageAttachmentRepository pageAttachmentRepository,
                                PageTagRepository pageTagRepository,
                                WorkSpacePageRepository workSpacePageRepository,
                                WorkSpaceRepository workSpaceRepository) {
        this.workSpaceValidator = workSpaceValidator;
        this.pageService = pageService;
        this.pageRepository = pageRepository;
        this.pageCommentRepository = pageCommentRepository;
        this.pageVersionRepository = pageVersionRepository;
        this.pageContentRepository = pageContentRepository;
        this.pageAttachmentRepository = pageAttachmentRepository;
        this.pageTagRepository = pageTagRepository;
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

        return getPageInfo(workSpaceRepository.queryDetail(workSpace.getId()), resourceId, BaseStage.INSERT, type);
    }

    @Override
    public PageDTO queryDetail(Long resourceId, Long id, String type) {
        this.checkWorkSpaceBelong(resourceId, id, type);
        WorkSpacePageE workSpacePageE = workSpacePageRepository.selectByWorkSpaceId(id);
        if (workSpacePageE == null) {
            throw new CommonException("error.workSpacePage.select");
        }
        String referenceType = workSpacePageE.getReferenceType();
        switch (referenceType) {
            case BaseStage.REFERENCE_PAGE:
                return getPageInfo(workSpaceRepository.queryDetail(id), resourceId, BaseStage.UPDATE, type);
            case BaseStage.REFERENCE_URL:
                return getReferencePageInfo(workSpaceRepository.queryReferenceDetail(id), resourceId, type);
            case BaseStage.SELF:
                return getPageInfo(workSpaceRepository.queryDetail(id), resourceId, BaseStage.UPDATE, type);
            default:
                return new PageDTO();
        }
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
            this.updatePageInfo(id, pageUpdateDTO, pageE);
        } else if (BaseStage.REFERENCE_URL.equals(workSpacePageE.getReferenceType())) {
            workSpacePageE.setReferenceUrl(pageUpdateDTO.getReferenceUrl());
            workSpacePageRepository.update(workSpacePageE);
            return getReferencePageInfo(workSpaceRepository.queryReferenceDetail(id), resourceId, type);
        }

        return getPageInfo(workSpaceRepository.queryDetail(id), resourceId, BaseStage.UPDATE, type);
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
        pageCommentRepository.deleteByPageId(workSpacePageE.getPageId());
        pageAttachmentRepository.deleteByPageId(workSpacePageE.getPageId());
        pageTagRepository.deleteByPageId(workSpacePageE.getPageId());
    }

    @Override
    public WorkSpaceFirstTreeDTO queryFirstTree(Long resourceId, String type) {
        Map<Long, WorkSpaceTreeDTO> workSpaceTreeMap = new HashMap<>();
        List<WorkSpaceE> workSpaceEList = workSpaceRepository.workSpaceListByParentId(resourceId, 0L, type);
        WorkSpaceFirstTreeDTO workSpaceFirstTreeDTO = new WorkSpaceFirstTreeDTO();
        workSpaceFirstTreeDTO.setRootId(0L);
        workSpaceFirstTreeDTO.setItems(getWorkSpaceTopTreeList(workSpaceEList, workSpaceTreeMap, resourceId, type));

        return workSpaceFirstTreeDTO;
    }

    @Override
    public Map<Long, WorkSpaceTreeDTO> queryTree(Long resourceId, List<Long> parentIds, String type) {
        Map<Long, WorkSpaceTreeDTO> workSpaceTreeMap = new HashMap<>();
        List<WorkSpaceE> workSpaceEList = workSpaceRepository.workSpaceListByParentIds(resourceId, parentIds, type);
        return getWorkSpaceTreeList(workSpaceEList, workSpaceTreeMap, resourceId, type, 0L, Collections.emptyList());
    }

    @Override
    public Map<Long, WorkSpaceTreeDTO> queryParentTree(Long resourceId, Long id, String type) {
        Map<Long, WorkSpaceTreeDTO> workSpaceTreeMap = new HashMap<>();
        WorkSpaceE workSpaceE = this.selectWorkSpaceById(id);
        if (!workSpaceE.getRoute().isEmpty()) {
            String[] idStr = workSpaceE.getRoute().split("\\.");
            List<Long> list = new ArrayList<>();
            for (String str : idStr) {
                list.add(TypeUtil.objToLong(str));
            }
            list.add(0L);
            List<WorkSpaceE> workSpaceEList = workSpaceRepository.workSpaceListByParentIds(resourceId,
                    list,
                    type);
            workSpaceTreeMap = getWorkSpaceTreeList(workSpaceEList, workSpaceTreeMap, resourceId, type, 0L, list);
        }
        return workSpaceTreeMap;
    }

    @Override
    public void moveWorkSpace(Long resourceId, Long id, MoveWorkSpaceDTO moveWorkSpaceDTO, String type) {
        if (moveWorkSpaceDTO.getTargetId() != 0) {
            this.checkWorkSpaceBelong(resourceId, moveWorkSpaceDTO.getTargetId(), type);
        }
        WorkSpaceE sourceWorkSpace = this.checkWorkSpaceBelong(resourceId, moveWorkSpaceDTO.getId(), type);
        String oldRoute = sourceWorkSpace.getRoute();
        String rank = "";
        if (moveWorkSpaceDTO.getBefore()) {
            rank = beforeRank(resourceId, type, id, moveWorkSpaceDTO);
        } else {
            rank = afterRank(resourceId, type, id, moveWorkSpaceDTO);
        }

        sourceWorkSpace.setRank(rank);
        if (sourceWorkSpace.getParentId().equals(id)) {
            workSpaceRepository.update(sourceWorkSpace);
        } else {
            if (id == 0) {
                sourceWorkSpace.setParentId(0L);
                sourceWorkSpace.setRoute(TypeUtil.objToString(sourceWorkSpace.getId()));
            } else {
                WorkSpaceE parent = this.checkWorkSpaceBelong(resourceId, id, type);
                sourceWorkSpace.setParentId(parent.getId());
                sourceWorkSpace.setRoute(parent.getRoute() + "." + sourceWorkSpace.getId());
            }
            sourceWorkSpace = workSpaceRepository.update(sourceWorkSpace);

            if (workSpaceRepository.hasChildWorkSpace(type, resourceId, sourceWorkSpace.getId())) {
                String newRoute = sourceWorkSpace.getRoute();
                workSpaceRepository.updateByRoute(type, resourceId, oldRoute, newRoute);
            }
        }
    }

    private String beforeRank(Long resourceId, String type, Long id, MoveWorkSpaceDTO moveWorkSpaceDTO) {
        if (Objects.equals(moveWorkSpaceDTO.getTargetId(), 0L)) {
            return noOutsetBeforeRank(resourceId, type, id, moveWorkSpaceDTO);
        } else {
            return outsetBeforeRank(resourceId, type, id, moveWorkSpaceDTO);
        }
    }

    private String afterRank(Long resourceId, String type, Long id, MoveWorkSpaceDTO moveWorkSpaceDTO) {
        String leftRank = workSpaceRepository.queryRank(type, resourceId, moveWorkSpaceDTO.getTargetId());
        String rightRank = workSpaceRepository.queryRightRank(type, resourceId, id, leftRank);
        if (rightRank == null) {
            return RankUtil.genNext(leftRank);
        } else {
            return RankUtil.between(leftRank, rightRank);
        }
    }

    private String noOutsetBeforeRank(Long resourceId, String type, Long id, MoveWorkSpaceDTO moveWorkSpaceDTO) {
        String minRank = workSpaceRepository.queryMinRank(type, resourceId, id);
        if (minRank == null) {
            return RankUtil.mid();
        } else {
            return RankUtil.genPre(minRank);
        }
    }

    private String outsetBeforeRank(Long resourceId, String type, Long id, MoveWorkSpaceDTO moveWorkSpaceDTO) {
        String rightRank = workSpaceRepository.queryRank(type, resourceId, moveWorkSpaceDTO.getTargetId());
        String leftRank = workSpaceRepository.queryLeftRank(type, resourceId, id, rightRank);
        if (leftRank == null) {
            return RankUtil.genPre(rightRank);
        } else {
            return RankUtil.between(leftRank, rightRank);
        }
    }

    private WorkSpaceE selectWorkSpaceById(Long id) {
        LOGGER.info("select work space by id:{}", id);
        WorkSpaceE workSpaceE = workSpaceRepository.selectById(id);
        if (workSpaceE == null) {
            throw new CommonException("error.work.space.select");
        }
        return workSpaceE;
    }

    private Map<Long, WorkSpaceTreeDTO> getWorkSpaceTopTreeList(List<WorkSpaceE> workSpaceEList,
                                                                Map<Long, WorkSpaceTreeDTO> workSpaceTreeMap,
                                                                Long resourceId,
                                                                String type) {
        WorkSpaceTreeDTO workSpaceTreeDTO = new WorkSpaceTreeDTO();
        WorkSpaceTreeDTO.Data data = new WorkSpaceTreeDTO.Data();
        data.setTitle("choerodon");
        workSpaceTreeDTO.setData(data);
        workSpaceTreeDTO.setId(0L);
        workSpaceTreeDTO.setParentId(0L);
        if (workSpaceEList.isEmpty()) {
            workSpaceTreeDTO.setHasChildren(false);
            workSpaceTreeDTO.setChildren(Collections.emptyList());
        } else {
            workSpaceTreeDTO.setHasChildren(true);
            List<Long> children = workSpaceEList.stream().map(WorkSpaceE::getId).collect(Collectors.toList());
            workSpaceTreeDTO.setChildren(children);
            getWorkSpaceTreeList(workSpaceEList, workSpaceTreeMap, resourceId, type, 0L, Collections.emptyList());
        }
        workSpaceTreeMap.put(workSpaceTreeDTO.getId(), workSpaceTreeDTO);
        return workSpaceTreeMap;
    }

    private Map<Long, WorkSpaceTreeDTO> getWorkSpaceTreeList(List<WorkSpaceE> workSpaceEList,
                                                             Map<Long, WorkSpaceTreeDTO> workSpaceTreeMap,
                                                             Long resourceId,
                                                             String type,
                                                             Long level,
                                                             List<Long> routes) {
        ++level;
        Boolean hasChildren = false;
        for (WorkSpaceE w : workSpaceEList) {
            WorkSpaceTreeDTO workSpaceTreeDTO = new WorkSpaceTreeDTO();
            WorkSpaceTreeDTO.Data data = new WorkSpaceTreeDTO.Data();
            workSpaceTreeDTO.setId(w.getId());
            workSpaceTreeDTO.setParentId(w.getParentId());
            if (routes.contains(w.getId())) {
                workSpaceTreeDTO.setIsExpanded(true);
            }
            data.setTitle(w.getName());
            List<WorkSpaceE> list = workSpaceRepository.workSpaceListByParentId(resourceId, w.getId(), type);
            if (list.isEmpty()) {
                workSpaceTreeDTO.setHasChildren(false);
                workSpaceTreeDTO.setChildren(Collections.emptyList());
            } else {
                workSpaceTreeDTO.setHasChildren(true);
                hasChildren = true;
                List<Long> children = list.stream().map(WorkSpaceE::getId).collect(Collectors.toList());
                workSpaceTreeDTO.setChildren(children);
            }
            workSpaceTreeDTO.setData(data);
            workSpaceTreeMap.put(workSpaceTreeDTO.getId(), workSpaceTreeDTO);
            if (level <= 1 && hasChildren) {
                getWorkSpaceTreeList(list, workSpaceTreeMap, resourceId, type, level, routes);
            }
        }
        return workSpaceTreeMap;
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

    private WorkSpaceE checkWorkSpaceBelong(Long resourceId, Long id, String type) {
        WorkSpaceE workSpaceE = this.selectWorkSpaceById(id);
        if (PageResourceType.ORGANIZATION.getResourceType().equals(type) && !workSpaceE.getOrganizationId().equals(resourceId)) {
            throw new CommonException("The workspace not found in the organization");
        } else if (PageResourceType.PROJECT.getResourceType().equals(type) && !workSpaceE.getProjectId().equals(resourceId)) {
            throw new CommonException("The workspace not found in the project");
        }

        return workSpaceE;
    }

    private PageDTO getPageInfo(PageDetailE pageDetailE, Long resourceId, String operationType, String type) {
        PageDTO pageDTO = new PageDTO();
        BeanUtils.copyProperties(pageDetailE, pageDTO);

        WorkSpaceTreeDTO workSpaceTreeDTO = new WorkSpaceTreeDTO();
        workSpaceTreeDTO.setId(pageDetailE.getWorkSpaceId());
        workSpaceTreeDTO.setParentId(pageDetailE.getWorkSpaceParentId());
        workSpaceTreeDTO.setIsExpanded(false);
        if (operationType.equals(BaseStage.INSERT)) {
            workSpaceTreeDTO.setHasChildren(false);
            workSpaceTreeDTO.setChildren(Collections.emptyList());
        } else if (operationType.equals(BaseStage.UPDATE)) {
            List<WorkSpaceE> list = workSpaceRepository.workSpaceListByParentId(resourceId, pageDetailE.getWorkSpaceId(), type);
            if (list.isEmpty()) {
                workSpaceTreeDTO.setHasChildren(false);
                workSpaceTreeDTO.setChildren(Collections.emptyList());
            } else {
                workSpaceTreeDTO.setHasChildren(true);
                List<Long> children = list.stream().map(WorkSpaceE::getId).collect(Collectors.toList());
                workSpaceTreeDTO.setChildren(children);
            }
        }
        WorkSpaceTreeDTO.Data data = new WorkSpaceTreeDTO.Data();
        data.setTitle(pageDetailE.getTitle());
        workSpaceTreeDTO.setData(data);
        pageDTO.setWorkSpace(workSpaceTreeDTO);

        PageDTO.PageInfo pageInfo = new PageDTO.PageInfo();
        pageInfo.setId(pageDetailE.getPageId());
        BeanUtils.copyProperties(pageDetailE, pageInfo);
        pageDTO.setPageInfo(pageInfo);

        return pageDTO;
    }

    private PageDTO getReferencePageInfo(PageDetailE pageDetailE, Long resourceId, String type) {
        PageDTO pageDTO = new PageDTO();
        BeanUtils.copyProperties(pageDetailE, pageDTO);

        WorkSpaceTreeDTO workSpaceTreeDTO = new WorkSpaceTreeDTO();
        workSpaceTreeDTO.setId(pageDetailE.getWorkSpaceId());
        workSpaceTreeDTO.setParentId(pageDetailE.getWorkSpaceParentId());
        workSpaceTreeDTO.setIsExpanded(false);
        List<WorkSpaceE> list = workSpaceRepository.workSpaceListByParentId(resourceId, pageDetailE.getWorkSpaceId(), type);
        if (list.isEmpty()) {
            workSpaceTreeDTO.setHasChildren(false);
            workSpaceTreeDTO.setChildren(Collections.emptyList());
        } else {
            workSpaceTreeDTO.setHasChildren(true);
            List<Long> children = list.stream().map(WorkSpaceE::getId).collect(Collectors.toList());
            workSpaceTreeDTO.setChildren(children);
        }
        WorkSpaceTreeDTO.Data data = new WorkSpaceTreeDTO.Data();
        data.setTitle(pageDetailE.getTitle());
        workSpaceTreeDTO.setData(data);
        pageDTO.setWorkSpace(workSpaceTreeDTO);

        pageDTO.setPageInfo(null);

        return pageDTO;
    }
}
