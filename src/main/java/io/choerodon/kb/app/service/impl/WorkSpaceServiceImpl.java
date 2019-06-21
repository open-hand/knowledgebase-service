package io.choerodon.kb.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.api.dao.*;
import io.choerodon.kb.api.validator.WorkSpaceValidator;
import io.choerodon.kb.app.service.PageAttachmentService;
import io.choerodon.kb.app.service.PageVersionService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.domain.kb.repository.*;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.common.enums.PageResourceType;
import io.choerodon.kb.infra.common.utils.RankUtil;
import io.choerodon.kb.infra.common.utils.TypeUtil;
import io.choerodon.kb.infra.dataobject.*;
import io.choerodon.kb.infra.dataobject.iam.ProjectDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Zenger on 2019/4/30.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class WorkSpaceServiceImpl implements WorkSpaceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkSpaceServiceImpl.class);
    private static final String ILLEGAL_ERROR = "error.delete.illegal";
    private static final String ROOT_ID = "rootId";
    private static final String ITEMS = "items";
    private static final String TOP_TITLE = "choerodon";

    @Autowired
    private WorkSpaceValidator workSpaceValidator;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private PageVersionRepository pageVersionRepository;
    @Autowired
    private PageContentRepository pageContentRepository;
    @Autowired
    private PageCommentRepository pageCommentRepository;
    @Autowired
    private PageAttachmentRepository pageAttachmentRepository;
    @Autowired
    private PageTagRepository pageTagRepository;
    @Autowired
    private WorkSpaceRepository workSpaceRepository;
    @Autowired
    private WorkSpacePageRepository workSpacePageRepository;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private PageVersionService pageVersionService;
    @Autowired
    private PageLogRepository pageLogRepository;
    @Autowired
    private PageAttachmentService pageAttachmentService;
    @Autowired
    private WorkSpaceShareRepository workSpaceShareRepository;

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
            ProjectDO projectDO = iamRepository.queryIamProject(resourceId);
            LOGGER.info("projectId:{},get project info:{}", resourceId, projectDO.toString());
            pageDO.setOrganizationId(projectDO.getOrganizationId());
            pageDO.setProjectId(resourceId);
            workSpaceDO.setOrganizationId(projectDO.getOrganizationId());
            workSpaceDO.setProjectId(resourceId);
        }

        PageDO page = this.insertPage(pageDO, pageCreateDTO);
        WorkSpaceDO workSpace = this.insertWorkSpace(workSpaceDO, page, resourceId, pageCreateDTO, type);
        this.insertWorkSpacePage(page.getId(), workSpace.getId());

        return getPageInfo(workSpaceRepository.queryDetail(workSpace.getId()), BaseStage.INSERT);
    }

    @Override
    public PageDTO queryDetail(Long id) {
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
    public void delete(Long resourceId, Long id, String type, Boolean isAdmin) {
        this.checkWorkSpaceBelong(resourceId, id, type);
        WorkSpaceDO workSpaceDO = this.selectWorkSpaceById(id);
        WorkSpacePageDO workSpacePageDO = workSpacePageRepository.selectByWorkSpaceId(id);
        if (!isAdmin) {
            Long currentUserId = DetailsHelper.getUserDetails().getUserId();
            if (!workSpacePageDO.getCreatedBy().equals(currentUserId)) {
                throw new CommonException(ILLEGAL_ERROR);
            }
        }
        workSpaceRepository.deleteByRoute(workSpaceDO.getRoute());
        workSpacePageRepository.delete(workSpacePageDO.getId());
        pageRepository.delete(workSpacePageDO.getPageId());
        pageVersionRepository.deleteByPageId(workSpacePageDO.getPageId());
        pageContentRepository.deleteByPageId(workSpacePageDO.getPageId());
        pageCommentRepository.deleteByPageId(workSpacePageDO.getPageId());
        List<PageAttachmentDO> pageAttachmentDOList = pageAttachmentRepository.selectByPageId(workSpacePageDO.getPageId());
        for (PageAttachmentDO pageAttachment : pageAttachmentDOList) {
            pageAttachmentRepository.delete(pageAttachment.getId());
            pageAttachmentService.deleteFile(pageAttachment.getUrl());
        }
        pageTagRepository.deleteByPageId(workSpacePageDO.getPageId());
        pageLogRepository.deleteByPageId(workSpacePageDO.getPageId());
        workSpaceShareRepository.deleteByWorkSpaceId(id);
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
        return workSpaceRepository.selectById(id);
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
        if (pageCreateDTO.getParentWorkspaceId() != 0) {
            WorkSpaceDO parentWorkSpace = this.selectWorkSpaceById(pageCreateDTO.getParentWorkspaceId());
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
        workSpaceDO = workSpaceRepository.insert(workSpaceDO);

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
        workSpaceTreeDTO.setCreatedBy(pageDetailDO.getCreatedBy());
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

    @Override
    public Map<String, Object> queryAllChildTreeByWorkSpaceId(Long workSpaceId, Boolean isNeedChild) {
        if (isNeedChild) {
            return buildTreeMap(workSpaceRepository.queryAllChildByWorkSpaceId(workSpaceId), null);
        } else {
            WorkSpaceDO workSpaceDO = workSpaceRepository.selectById(workSpaceId);
            return buildTreeMap(Arrays.asList(workSpaceDO), null);
        }
    }

    @Override
    public Map<String, Object> queryAllTree(Long resourceId, Long expandWorkSpaceId, String type) {
        return buildTreeMap(workSpaceRepository.queryAll(resourceId, type), expandWorkSpaceId);
    }

    /**
     * 构建树形Map
     *
     * @param workSpaceDOList
     * @param expandWorkSpaceId
     * @return
     */
    private Map<String, Object> buildTreeMap(List<WorkSpaceDO> workSpaceDOList, Long expandWorkSpaceId) {
        Map<String, Object> result = new HashMap<>(2);
        Map<Long, WorkSpaceTreeDTO> workSpaceTreeMap = new HashMap<>(workSpaceDOList.size());
        Map<Long, List<Long>> groupMap = workSpaceDOList.stream().collect(Collectors.
                groupingBy(WorkSpaceDO::getParentId, Collectors.mapping(WorkSpaceDO::getId, Collectors.toList())));
        //创建topTreeDTO
        WorkSpaceDO topSpace = new WorkSpaceDO();
        topSpace.setName(TOP_TITLE);
        topSpace.setParentId(0L);
        topSpace.setId(0L);
        List<Long> topChildIds = groupMap.get(0L);
        workSpaceTreeMap.put(0L, buildTreeDTO(topSpace, topChildIds));
        for (WorkSpaceDO workSpaceDO : workSpaceDOList) {
            WorkSpaceTreeDTO treeDTO = buildTreeDTO(workSpaceDO, groupMap.get(workSpaceDO.getId()));
            workSpaceTreeMap.put(workSpaceDO.getId(), treeDTO);
        }
        //设置展开的工作空间，并设置点击当前
        if (expandWorkSpaceId != null) {
            WorkSpaceDO workSpaceDO = workSpaceRepository.selectById(expandWorkSpaceId);
            List<Long> expandIds = Stream.of(workSpaceDO.getRoute().split("\\.")).map(Long::parseLong).collect(Collectors.toList());
            for (Long expandId : expandIds) {
                WorkSpaceTreeDTO treeDTO = workSpaceTreeMap.get(expandId);
                if (treeDTO != null) {
                    treeDTO.setIsExpanded(true);
                }
            }
            WorkSpaceTreeDTO treeDTO = workSpaceTreeMap.get(expandWorkSpaceId);
            if (treeDTO != null) {
                treeDTO.setIsExpanded(false);
                treeDTO.setIsClick(true);
            }
        }
        result.put(ROOT_ID, 0L);
        result.put(ITEMS, workSpaceTreeMap);
        return result;
    }

    /**
     * 构建treeDTO
     *
     * @param workSpaceDO
     * @param childIds
     * @return
     */
    private WorkSpaceTreeDTO buildTreeDTO(WorkSpaceDO workSpaceDO, List<Long> childIds) {
        WorkSpaceTreeDTO treeDTO = new WorkSpaceTreeDTO();
        treeDTO.setCreatedBy(workSpaceDO.getCreatedBy());
        if (CollectionUtils.isEmpty(childIds)) {
            treeDTO.setHasChildren(false);
            treeDTO.setChildren(Collections.emptyList());
        } else {
            treeDTO.setHasChildren(true);
            treeDTO.setChildren(childIds);
        }
        WorkSpaceTreeDTO.Data data = new WorkSpaceTreeDTO.Data();
        data.setTitle(workSpaceDO.getName());
        treeDTO.setData(data);
        treeDTO.setIsExpanded(false);
        treeDTO.setIsClick(false);
        treeDTO.setParentId(workSpaceDO.getParentId());
        treeDTO.setId(workSpaceDO.getId());
        treeDTO.setRoute(workSpaceDO.getRoute());
        return treeDTO;
    }
}
