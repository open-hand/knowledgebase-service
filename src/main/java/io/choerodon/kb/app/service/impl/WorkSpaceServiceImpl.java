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
import io.choerodon.kb.app.service.PageVersionService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.domain.kb.repository.*;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.common.enums.PageResourceType;
import io.choerodon.kb.infra.common.utils.RankUtil;
import io.choerodon.kb.infra.common.utils.TypeUtil;
import io.choerodon.kb.infra.dataobject.PageDO;
import io.choerodon.kb.infra.dataobject.PageDetailDO;
import io.choerodon.kb.infra.dataobject.WorkSpaceDO;
import io.choerodon.kb.infra.dataobject.WorkSpacePageDO;
import io.choerodon.kb.infra.dataobject.iam.OrganizationDO;
import io.choerodon.kb.infra.dataobject.iam.ProjectDO;

/**
 * Created by Zenger on 2019/4/30.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class WorkSpaceServiceImpl implements WorkSpaceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkSpaceServiceImpl.class);

    private WorkSpaceValidator workSpaceValidator;
    private PageRepository pageRepository;
    private PageVersionRepository pageVersionRepository;
    private PageContentRepository pageContentRepository;
    private PageCommentRepository pageCommentRepository;
    private PageAttachmentRepository pageAttachmentRepository;
    private PageTagRepository pageTagRepository;
    private WorkSpaceRepository workSpaceRepository;
    private WorkSpacePageRepository workSpacePageRepository;
    private IamRepository iamRepository;
    private PageVersionService pageVersionService;
    private PageLogRepository pageLogRepository;

    public WorkSpaceServiceImpl(WorkSpaceValidator workSpaceValidator,
                                PageRepository pageRepository,
                                PageCommentRepository pageCommentRepository,
                                PageVersionRepository pageVersionRepository,
                                PageContentRepository pageContentRepository,
                                PageAttachmentRepository pageAttachmentRepository,
                                PageTagRepository pageTagRepository,
                                WorkSpacePageRepository workSpacePageRepository,
                                WorkSpaceRepository workSpaceRepository,
                                IamRepository iamRepository,
                                PageVersionService pageVersionService,
                                PageLogRepository pageLogRepository) {
        this.workSpaceValidator = workSpaceValidator;
        this.pageRepository = pageRepository;
        this.pageCommentRepository = pageCommentRepository;
        this.pageVersionRepository = pageVersionRepository;
        this.pageContentRepository = pageContentRepository;
        this.pageAttachmentRepository = pageAttachmentRepository;
        this.pageTagRepository = pageTagRepository;
        this.workSpacePageRepository = workSpacePageRepository;
        this.workSpaceRepository = workSpaceRepository;
        this.iamRepository = iamRepository;
        this.pageVersionService = pageVersionService;
        this.pageLogRepository = pageLogRepository;
    }

    @Override
    public PageDTO create(Long resourceId, PageCreateDTO pageCreateDTO, String type) {
        LOGGER.info("start create page...");

        WorkSpaceDO workSpaceDO = new WorkSpaceDO();
        PageDO pageDO = new PageDO();
        pageDO.setTitle(pageCreateDTO.getTitle());
        if (PageResourceType.ORGANIZATION.getResourceType().equals(type)) {
            pageDO.setOrganizationId(resourceId);
            workSpaceDO.setOrganizationId(resourceId);
        } else {
            pageDO.setProjectId(resourceId);
            workSpaceDO.setProjectId(resourceId);
        }

        PageDO page = this.insertPage(pageDO, pageCreateDTO);
        WorkSpaceDO workSpace = this.insertWorkSpace(workSpaceDO, page, resourceId, pageCreateDTO, type);
        this.insertWorkSpacePage(page.getId(), workSpace.getId());

        return getPageInfo(workSpaceRepository.queryDetail(workSpace.getId()), BaseStage.INSERT);
    }

    @Override
    public PageDTO queryDetail(Long resourceId, Long id, String type) {
        WorkSpacePageDO workSpacePageDO = workSpacePageRepository.selectByWorkSpaceId(id);
        String referenceType = workSpacePageDO.getReferenceType();
        switch (referenceType) {
            case BaseStage.REFERENCE_PAGE:
                return getPageInfo(workSpaceRepository.queryDetail(id), BaseStage.UPDATE);
            case BaseStage.REFERENCE_URL:
                return getReferencePageInfo(workSpaceRepository.queryReferenceDetail(id));
            case BaseStage.SELF:
                return getPageInfo(workSpaceRepository.queryDetail(id), BaseStage.UPDATE);
            default:
                return new PageDTO();
        }
    }

    @Override
    public PageDTO update(Long resourceId, Long id, PageUpdateDTO pageUpdateDTO, String type) {
        this.checkWorkSpaceBelong(resourceId, id, type);
        WorkSpacePageDO workSpacePageDO = workSpaceValidator.checkUpdatePage(pageUpdateDTO, id);
        if (BaseStage.SELF.equals(workSpacePageDO.getReferenceType())) {
            PageDO pageDO = pageRepository.selectById(workSpacePageDO.getPageId());
            pageDO.setObjectVersionNumber(pageUpdateDTO.getObjectVersionNumber());
            this.updatePageInfo(id, pageUpdateDTO, pageDO);
        } else if (BaseStage.REFERENCE_URL.equals(workSpacePageDO.getReferenceType())) {
            workSpacePageDO.setObjectVersionNumber(pageUpdateDTO.getObjectVersionNumber());
            workSpacePageDO.setReferenceUrl(pageUpdateDTO.getReferenceUrl());
            workSpacePageRepository.update(workSpacePageDO);
            return getReferencePageInfo(workSpaceRepository.queryReferenceDetail(id));
        }

        return getPageInfo(workSpaceRepository.queryDetail(id), BaseStage.UPDATE);
    }

    @Override
    public void delete(Long resourceId, Long id, String type) {
        this.checkWorkSpaceBelong(resourceId, id, type);
        WorkSpaceDO workSpaceDO = this.selectWorkSpaceById(id);
        WorkSpacePageDO workSpacePageDO = workSpacePageRepository.selectByWorkSpaceId(id);

        workSpaceRepository.deleteByRoute(workSpaceDO.getRoute());
        workSpacePageRepository.delete(workSpacePageDO.getId());
        pageRepository.delete(workSpacePageDO.getPageId());
        pageVersionRepository.deleteByPageId(workSpacePageDO.getPageId());
        pageContentRepository.deleteByPageId(workSpacePageDO.getPageId());
        pageCommentRepository.deleteByPageId(workSpacePageDO.getPageId());
        pageAttachmentRepository.deleteByPageId(workSpacePageDO.getPageId());
        pageTagRepository.deleteByPageId(workSpacePageDO.getPageId());
        pageLogRepository.deleteByPageId(workSpacePageDO.getPageId());
    }

    @Override
    public WorkSpaceOrganizationTreeDTO queryOrganizationTree(Long projectId) {
        WorkSpaceOrganizationTreeDTO workSpaceProjectTreeDTO = new WorkSpaceOrganizationTreeDTO();
        ProjectDO projectDO = iamRepository.queryIamProject(projectId);
        if (projectDO != null) {
            LOGGER.info("get project info:{}", projectDO);
            OrganizationDO organizationDO = iamRepository.queryOrganizationById(projectDO.getOrganizationId());
            LOGGER.info("get organization info:{}", organizationDO);
            if (organizationDO != null) {
                workSpaceProjectTreeDTO = getWorkSpaceProjectTreeList(organizationDO);
            }
        }
        return workSpaceProjectTreeDTO;
    }

    @Override
    public WorkSpaceFirstTreeDTO queryFirstTree(Long resourceId, String type) {
        Map<Long, WorkSpaceTreeDTO> workSpaceTreeMap = new HashMap<>();
        List<WorkSpaceDO> workSpaceDOList = workSpaceRepository.workSpaceListByParentId(resourceId, 0L, type);
        WorkSpaceFirstTreeDTO workSpaceFirstTreeDTO = new WorkSpaceFirstTreeDTO();
        workSpaceFirstTreeDTO.setRootId(0L);
        workSpaceFirstTreeDTO.setItems(getWorkSpaceTopTreeList(workSpaceDOList, workSpaceTreeMap, resourceId, type));

        return workSpaceFirstTreeDTO;
    }

    @Override
    public Map<Long, WorkSpaceTreeDTO> queryTree(Long resourceId, List<Long> parentIds, String type) {
        Map<Long, WorkSpaceTreeDTO> workSpaceTreeMap = new HashMap<>();
        List<WorkSpaceDO> workSpaceDOList = workSpaceRepository.workSpaceListByParentIds(resourceId, parentIds, type);
        return getWorkSpaceTreeList(workSpaceDOList, workSpaceTreeMap, resourceId, type, 0L, Collections.emptyList());
    }

    @Override
    public Map<Long, WorkSpaceTreeDTO> queryParentTree(Long resourceId, Long id, String type) {
        Map<Long, WorkSpaceTreeDTO> workSpaceTreeMap = new HashMap<>();
        WorkSpaceDO workSpaceDO = this.selectWorkSpaceById(id);
        if (!workSpaceDO.getRoute().isEmpty()) {
            String[] idStr = workSpaceDO.getRoute().split("\\.");
            List<Long> list = new ArrayList<>();
            for (String str : idStr) {
                list.add(TypeUtil.objToLong(str));
            }
            list.add(0L);
            List<WorkSpaceDO> workSpaceDOList = workSpaceRepository.workSpaceListByParentIds(resourceId,
                    list,
                    type);
            workSpaceTreeMap = getWorkSpaceTreeList(workSpaceDOList, workSpaceTreeMap, resourceId, type, 0L, list);
        }
        return workSpaceTreeMap;
    }

    @Override
    public void moveWorkSpace(Long resourceId, Long id, MoveWorkSpaceDTO moveWorkSpaceDTO, String type) {
        if (moveWorkSpaceDTO.getTargetId() != 0) {
            this.checkWorkSpaceBelong(resourceId, moveWorkSpaceDTO.getTargetId(), type);
        }
        WorkSpaceDO sourceWorkSpace = this.checkWorkSpaceBelong(resourceId, moveWorkSpaceDTO.getId(), type);
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
                WorkSpaceDO parent = this.checkWorkSpaceBelong(resourceId, id, type);
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
            return noOutsetBeforeRank(resourceId, type, id);
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

    private String noOutsetBeforeRank(Long resourceId, String type, Long id) {
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

    private WorkSpaceDO selectWorkSpaceById(Long id) {
        LOGGER.info("select work space by id:{}", id);
        return workSpaceRepository.selectById(id);
    }

    private Map<Long, WorkSpaceTreeDTO> getWorkSpaceTopTreeList(List<WorkSpaceDO> workSpaceDOList,
                                                                Map<Long, WorkSpaceTreeDTO> workSpaceTreeMap,
                                                                Long resourceId,
                                                                String type) {
        WorkSpaceTreeDTO workSpaceTreeDTO = new WorkSpaceTreeDTO();
        WorkSpaceTreeDTO.Data data = new WorkSpaceTreeDTO.Data();
        data.setTitle("choerodon");
        workSpaceTreeDTO.setData(data);
        workSpaceTreeDTO.setId(0L);
        workSpaceTreeDTO.setParentId(0L);
        if (workSpaceDOList.isEmpty()) {
            workSpaceTreeDTO.setHasChildren(false);
            workSpaceTreeDTO.setChildren(Collections.emptyList());
        } else {
            workSpaceTreeDTO.setHasChildren(true);
            List<Long> children = workSpaceDOList.stream().map(WorkSpaceDO::getId).collect(Collectors.toList());
            workSpaceTreeDTO.setChildren(children);
            getWorkSpaceTreeList(workSpaceDOList, workSpaceTreeMap, resourceId, type, 0L, Collections.emptyList());
        }
        workSpaceTreeMap.put(workSpaceTreeDTO.getId(), workSpaceTreeDTO);
        return workSpaceTreeMap;
    }

    private Map<Long, WorkSpaceTreeDTO> getWorkSpaceTreeList(List<WorkSpaceDO> workSpaceDOList,
                                                             Map<Long, WorkSpaceTreeDTO> workSpaceTreeMap,
                                                             Long resourceId,
                                                             String type,
                                                             Long level,
                                                             List<Long> routes) {
        ++level;
        Boolean hasChildren = false;
        for (WorkSpaceDO w : workSpaceDOList) {
            WorkSpaceTreeDTO workSpaceTreeDTO = new WorkSpaceTreeDTO();
            WorkSpaceTreeDTO.Data data = new WorkSpaceTreeDTO.Data();
            workSpaceTreeDTO.setId(w.getId());
            workSpaceTreeDTO.setParentId(w.getParentId());
            if (routes.contains(w.getId())) {
                workSpaceTreeDTO.setIsExpanded(true);
            }
            data.setTitle(w.getName());
            List<WorkSpaceDO> list = workSpaceRepository.workSpaceListByParentId(resourceId, w.getId(), type);
            if (list.isEmpty()) {
                workSpaceTreeDTO.setHasChildren(false);
                workSpaceTreeDTO.setChildren(Collections.emptyList());
            } else {
                workSpaceTreeDTO.setHasChildren(true);
                hasChildren = true;
                List<Long> children = list.stream().map(WorkSpaceDO::getId).collect(Collectors.toList());
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

    private PageDO insertPage(PageDO pageDO, PageCreateDTO pageCreateDTO) {
        pageDO.setLatestVersionId(0L);
        pageDO = pageRepository.create(pageDO);
        Long latestVersionId = pageVersionService.createVersionAndContent(pageDO.getId(), pageCreateDTO.getContent(), pageDO.getLatestVersionId(), true, false);
        PageDO page = pageRepository.selectById(pageDO.getId());
        page.setLatestVersionId(latestVersionId);
        return pageRepository.update(page, false);
    }

    private WorkSpaceDO insertWorkSpace(WorkSpaceDO workSpaceDO,
                                        PageDO pageDO,
                                        Long resourceId,
                                        PageCreateDTO pageCreateDTO,
                                        String type) {
        workSpaceDO.setName(pageDO.getTitle());
        Long parentId = 0L;
        String route = "";
        if (pageCreateDTO.getWorkspaceId() != 0) {
            WorkSpaceDO parentWorkSpace = this.selectWorkSpaceById(pageCreateDTO.getWorkspaceId());
            parentId = parentWorkSpace.getId();
            route = parentWorkSpace.getRoute();
        }
        if (workSpaceRepository.hasChildWorkSpace(type, resourceId, parentId)) {
            String rank = workSpaceRepository.queryMaxRank(type, resourceId, parentId);
            workSpaceDO.setRank(RankUtil.genNext(rank));
        } else {
            workSpaceDO.setRank(RankUtil.mid());
        }
        workSpaceDO.setParentId(parentId);
        workSpaceDO = workSpaceRepository.inset(workSpaceDO);

        String realRoute = route.isEmpty() ? workSpaceDO.getId().toString() : route + "." + workSpaceDO.getId();
        WorkSpaceDO workSpace = workSpaceRepository.selectById(workSpaceDO.getId());
        workSpace.setRoute(realRoute);
        return workSpaceRepository.update(workSpace);
    }

    private WorkSpacePageDO insertWorkSpacePage(Long pageId, Long workSpaceId) {
        WorkSpacePageDO workSpacePageDO = new WorkSpacePageDO();
        workSpacePageDO.setReferenceType(BaseStage.SELF);
        workSpacePageDO.setPageId(pageId);
        workSpacePageDO.setWorkspaceId(workSpaceId);
        return workSpacePageRepository.insert(workSpacePageDO);
    }

    private void updatePageInfo(Long id, PageUpdateDTO pageUpdateDTO, PageDO pageDO) {
        WorkSpaceDO workSpaceDO = this.selectWorkSpaceById(id);
        if (pageUpdateDTO.getContent() != null) {
            Long latestVersionId = pageVersionService.createVersionAndContent(pageDO.getId(), pageUpdateDTO.getContent(), pageDO.getLatestVersionId(), false, pageUpdateDTO.getMinorEdit());
            pageDO.setLatestVersionId(latestVersionId);
        }
        if (pageUpdateDTO.getTitle() != null) {
            pageDO.setTitle(pageUpdateDTO.getTitle());
            workSpaceDO.setName(pageUpdateDTO.getTitle());
            workSpaceRepository.update(workSpaceDO);
        }
        pageRepository.update(pageDO, true);
    }


    private WorkSpaceDO checkWorkSpaceBelong(Long resourceId, Long id, String type) {
        WorkSpaceDO workSpaceDO = this.selectWorkSpaceById(id);
        if (PageResourceType.ORGANIZATION.getResourceType().equals(type) && !workSpaceDO.getOrganizationId().equals(resourceId)) {
            throw new CommonException("The workspace not found in the organization");
        } else if (PageResourceType.PROJECT.getResourceType().equals(type) && !workSpaceDO.getProjectId().equals(resourceId)) {
            throw new CommonException("The workspace not found in the project");
        }

        return workSpaceDO;
    }

    private PageDTO getPageInfo(PageDetailDO pageDetailDO, String operationType) {
        PageDTO pageDTO = new PageDTO();
        BeanUtils.copyProperties(pageDetailDO, pageDTO);

        WorkSpaceTreeDTO workSpaceTreeDTO = new WorkSpaceTreeDTO();
        workSpaceTreeDTO.setId(pageDetailDO.getWorkSpaceId());
        workSpaceTreeDTO.setParentId(pageDetailDO.getWorkSpaceParentId());
        workSpaceTreeDTO.setIsExpanded(false);
        if (operationType.equals(BaseStage.INSERT)) {
            workSpaceTreeDTO.setHasChildren(false);
            workSpaceTreeDTO.setChildren(Collections.emptyList());
        } else if (operationType.equals(BaseStage.UPDATE)) {
            List<WorkSpaceDO> list = workSpaceRepository.workSpacesByParentId(pageDetailDO.getWorkSpaceId());
            if (list.isEmpty()) {
                workSpaceTreeDTO.setHasChildren(false);
                workSpaceTreeDTO.setChildren(Collections.emptyList());
            } else {
                workSpaceTreeDTO.setHasChildren(true);
                List<Long> children = list.stream().map(WorkSpaceDO::getId).collect(Collectors.toList());
                workSpaceTreeDTO.setChildren(children);
            }
        }
        WorkSpaceTreeDTO.Data data = new WorkSpaceTreeDTO.Data();
        data.setTitle(pageDetailDO.getTitle());
        workSpaceTreeDTO.setData(data);
        pageDTO.setWorkSpace(workSpaceTreeDTO);

        PageDTO.PageInfo pageInfo = new PageDTO.PageInfo();
        pageInfo.setId(pageDetailDO.getPageId());
        BeanUtils.copyProperties(pageDetailDO, pageInfo);
        pageDTO.setPageInfo(pageInfo);

        return pageDTO;
    }

    private PageDTO getReferencePageInfo(PageDetailDO pageDetailDO) {
        PageDTO pageDTO = new PageDTO();
        BeanUtils.copyProperties(pageDetailDO, pageDTO);

        WorkSpaceTreeDTO workSpaceTreeDTO = new WorkSpaceTreeDTO();
        workSpaceTreeDTO.setId(pageDetailDO.getWorkSpaceId());
        workSpaceTreeDTO.setParentId(pageDetailDO.getWorkSpaceParentId());
        workSpaceTreeDTO.setIsExpanded(false);
        List<WorkSpaceDO> list = workSpaceRepository.workSpacesByParentId(pageDetailDO.getWorkSpaceId());
        if (list.isEmpty()) {
            workSpaceTreeDTO.setHasChildren(false);
            workSpaceTreeDTO.setChildren(Collections.emptyList());
        } else {
            workSpaceTreeDTO.setHasChildren(true);
            List<Long> children = list.stream().map(WorkSpaceDO::getId).collect(Collectors.toList());
            workSpaceTreeDTO.setChildren(children);
        }
        WorkSpaceTreeDTO.Data data = new WorkSpaceTreeDTO.Data();
        data.setTitle(pageDetailDO.getTitle());
        workSpaceTreeDTO.setData(data);
        pageDTO.setWorkSpace(workSpaceTreeDTO);

        pageDTO.setPageInfo(null);

        return pageDTO;
    }

    private WorkSpaceOrganizationTreeDTO getWorkSpaceProjectTreeList(OrganizationDO organizationDO) {
        WorkSpaceOrganizationTreeDTO workSpaceProjectTreeDTO = new WorkSpaceOrganizationTreeDTO();
        if (workSpaceRepository.selectOrganizationId(organizationDO.getId()) > 0) {
            workSpaceProjectTreeDTO.setOrgId(organizationDO.getId());
            workSpaceProjectTreeDTO.setOrgName(organizationDO.getName());
            workSpaceProjectTreeDTO.setWorkSpace(queryFirstTree(organizationDO.getId(),
                    PageResourceType.ORGANIZATION.getResourceType()));
        }
        return workSpaceProjectTreeDTO;
    }
}
