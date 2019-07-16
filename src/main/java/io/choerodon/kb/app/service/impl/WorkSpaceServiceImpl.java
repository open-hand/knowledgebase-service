package io.choerodon.kb.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.api.dao.*;
import io.choerodon.kb.api.validator.WorkSpaceValidator;
import io.choerodon.kb.app.service.PageAttachmentService;
import io.choerodon.kb.app.service.PageService;
import io.choerodon.kb.app.service.PageVersionService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.domain.kb.repository.*;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.common.enums.PageResourceType;
import io.choerodon.kb.infra.common.utils.EsRestUtil;
import io.choerodon.kb.infra.common.utils.RankUtil;
import io.choerodon.kb.infra.common.utils.TypeUtil;
import io.choerodon.kb.infra.dataobject.*;
import io.choerodon.kb.infra.dataobject.iam.ProjectDO;
import io.choerodon.kb.infra.mapper.UserSettingMapper;
import io.choerodon.kb.infra.mapper.WorkSpaceMapper;
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
    private static final String SETTING_TYPE_EDIT_MODE = "edit_mode";

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
    @Autowired
    private PageService pageService;
    @Autowired
    private WorkSpaceMapper workSpaceMapper;
    @Autowired
    private UserSettingMapper userSettingMapper;
    @Autowired
    private EsRestUtil esRestUtil;

    @Override
    public PageVO create(Long resourceId, PageCreateVO pageCreateVO, String type) {
        LOGGER.info("start create page...");

        WorkSpaceDO workSpaceDO = new WorkSpaceDO();
        PageDO pageDO = new PageDO();
        pageDO.setTitle(pageCreateVO.getTitle());
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

        PageDO page = this.insertPage(pageDO, pageCreateVO);
        WorkSpaceDO workSpace = this.insertWorkSpace(workSpaceDO, page, resourceId, pageCreateVO, type);
        this.insertWorkSpacePage(page.getId(), workSpace.getId());

        return getPageInfo(workSpaceRepository.queryDetail(workSpace.getId()), BaseStage.INSERT);
    }

    @Override
    public PageVO queryDetail(Long organizationId, Long projectId, Long workSpaceId, String searchStr) {
        workSpaceRepository.checkById(organizationId, projectId, workSpaceId);
        WorkSpacePageDO workSpacePageDO = workSpacePageRepository.selectByWorkSpaceId(workSpaceId);
        String referenceType = workSpacePageDO.getReferenceType();
        PageVO pageVO;
        switch (referenceType) {
            case BaseStage.REFERENCE_PAGE:
                pageVO = getPageInfo(workSpaceRepository.queryDetail(workSpaceId), BaseStage.UPDATE);
                break;
            case BaseStage.REFERENCE_URL:
                pageVO = getReferencePageInfo(workSpaceRepository.queryReferenceDetail(workSpaceId));
                break;
            case BaseStage.SELF:
                pageVO = getPageInfo(workSpaceRepository.queryDetail(workSpaceId), BaseStage.UPDATE);
                break;
            default:
                pageVO = new PageVO();
        }
        handleHasDraft(workSpaceId, pageVO);
        handleSearchStrHighlight(searchStr, pageVO);
        setUserSettingInfo(organizationId, projectId, pageVO);
        return pageVO;
    }

    private void setUserSettingInfo(Long organizationId, Long projectId, PageVO pageVO) {
        CustomUserDetails customUserDetails = DetailsHelper.getUserDetails();
        Long userId = customUserDetails.getUserId();
        UserSettingDO result;
        if (projectId == null) {
            UserSettingDO userSettingDO = new UserSettingDO(organizationId, SETTING_TYPE_EDIT_MODE, userId);
            result = userSettingMapper.selectOne(userSettingDO);
        } else {
            UserSettingDO userSettingDO = new UserSettingDO(organizationId, projectId, SETTING_TYPE_EDIT_MODE, userId);
            result = userSettingMapper.selectOne(userSettingDO);
        }
        if (result != null) {
            UserSettingVO userSettingVO = new UserSettingVO();
            BeanUtils.copyProperties(result, userSettingVO);
            pageVO.setUserSettingVO(userSettingVO);
        }
    }

    /**
     * 应用于全文检索，根据检索内容高亮内容
     *
     * @param searchStr
     * @param pageVO
     */
    private void handleSearchStrHighlight(String searchStr, PageVO pageVO) {
        if (searchStr != null) {
            String highlightContent = esRestUtil.highlightContent(searchStr, pageVO.getPageInfo().getContent());
            pageVO.getPageInfo().setHighlightContent(highlightContent != null && !highlightContent.equals("") ? highlightContent : pageVO.getPageInfo().getContent());
        }
    }

    /**
     * 判断是否有草稿数据
     *
     * @param workspaceId
     * @param pageVO
     */
    private void handleHasDraft(Long workspaceId, PageVO pageVO) {
        WorkSpaceDO workSpaceDO = workSpaceRepository.selectById(workspaceId);
        PageContentDO draft = pageService.queryDraftContent(workSpaceDO.getOrganizationId(), workSpaceDO.getProjectId(), pageVO.getPageInfo().getId());
        if (draft != null) {
            pageVO.setHasDraft(true);
            pageVO.setCreateDraftDate(draft.getLastUpdateDate());
        } else {
            pageVO.setHasDraft(false);
        }
    }

    @Override
    public PageVO update(Long resourceId, Long id, PageUpdateVO pageUpdateVO, String type) {
        this.checkWorkSpaceBelong(resourceId, id, type);
        WorkSpacePageDO workSpacePageDO = workSpaceValidator.checkUpdatePage(pageUpdateVO, id);
        if (BaseStage.SELF.equals(workSpacePageDO.getReferenceType())) {
            PageDO pageDO = pageRepository.selectById(workSpacePageDO.getPageId());
            pageDO.setObjectVersionNumber(pageUpdateVO.getObjectVersionNumber());
            this.updatePageInfo(id, pageUpdateVO, pageDO);
        } else if (BaseStage.REFERENCE_URL.equals(workSpacePageDO.getReferenceType())) {
            workSpacePageDO.setObjectVersionNumber(pageUpdateVO.getObjectVersionNumber());
            workSpacePageDO.setReferenceUrl(pageUpdateVO.getReferenceUrl());
            workSpacePageRepository.update(workSpacePageDO);
            return getReferencePageInfo(workSpaceRepository.queryReferenceDetail(id));
        }

        PageVO pageVO = getPageInfo(workSpaceRepository.queryDetail(id), BaseStage.UPDATE);
        if (Objects.equals(PageResourceType.PROJECT.getResourceType(), type)) {
            ProjectDO projectDO = iamRepository.queryIamProject(resourceId);
            setUserSettingInfo(projectDO.getOrganizationId(), resourceId, pageVO);
        } else if (Objects.equals(PageResourceType.ORGANIZATION.getResourceType(), type)) {
            setUserSettingInfo(resourceId, null, pageVO);
        }
        return pageVO;
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
        esRestUtil.deletePage(BaseStage.ES_PAGE_INDEX, workSpacePageDO.getPageId());
    }

    @Override
    public void moveWorkSpace(Long resourceId, Long id, MoveWorkSpaceVO moveWorkSpaceVO, String type) {
        if (moveWorkSpaceVO.getTargetId() != 0) {
            this.checkWorkSpaceBelong(resourceId, moveWorkSpaceVO.getTargetId(), type);
        }
        WorkSpaceDO sourceWorkSpace = this.checkWorkSpaceBelong(resourceId, moveWorkSpaceVO.getId(), type);
        String oldRoute = sourceWorkSpace.getRoute();
        String rank = "";
        if (moveWorkSpaceVO.getBefore()) {
            rank = beforeRank(resourceId, type, id, moveWorkSpaceVO);
        } else {
            rank = afterRank(resourceId, type, id, moveWorkSpaceVO);
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

    private String beforeRank(Long resourceId, String type, Long id, MoveWorkSpaceVO moveWorkSpaceVO) {
        if (Objects.equals(moveWorkSpaceVO.getTargetId(), 0L)) {
            return noOutsetBeforeRank(resourceId, type, id);
        } else {
            return outsetBeforeRank(resourceId, type, id, moveWorkSpaceVO);
        }
    }

    private String afterRank(Long resourceId, String type, Long id, MoveWorkSpaceVO moveWorkSpaceVO) {
        String leftRank = workSpaceRepository.queryRank(type, resourceId, moveWorkSpaceVO.getTargetId());
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

    private String outsetBeforeRank(Long resourceId, String type, Long id, MoveWorkSpaceVO moveWorkSpaceVO) {
        String rightRank = workSpaceRepository.queryRank(type, resourceId, moveWorkSpaceVO.getTargetId());
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

    private PageDO insertPage(PageDO pageDO, PageCreateVO pageCreateVO) {
        pageDO.setLatestVersionId(0L);
        pageDO = pageRepository.create(pageDO);
        Long latestVersionId = pageVersionService.createVersionAndContent(pageDO.getId(), pageCreateVO.getContent(), pageDO.getLatestVersionId(), true, false);
        PageDO page = pageRepository.selectById(pageDO.getId());
        page.setLatestVersionId(latestVersionId);
        return pageRepository.update(page, false);
    }

    private WorkSpaceDO insertWorkSpace(WorkSpaceDO workSpaceDO,
                                        PageDO pageDO,
                                        Long resourceId,
                                        PageCreateVO pageCreateVO,
                                        String type) {
        workSpaceDO.setName(pageDO.getTitle());
        Long parentId = 0L;
        String route = "";
        if (pageCreateVO.getParentWorkspaceId() != 0) {
            WorkSpaceDO parentWorkSpace = this.selectWorkSpaceById(pageCreateVO.getParentWorkspaceId());
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

    private void updatePageInfo(Long id, PageUpdateVO pageUpdateVO, PageDO pageDO) {
        WorkSpaceDO workSpaceDO = this.selectWorkSpaceById(id);
        if (pageUpdateVO.getContent() != null) {
            Long latestVersionId = pageVersionService.createVersionAndContent(pageDO.getId(), pageUpdateVO.getContent(), pageDO.getLatestVersionId(), false, pageUpdateVO.getMinorEdit());
            pageDO.setLatestVersionId(latestVersionId);
        }
        if (pageUpdateVO.getTitle() != null) {
            pageDO.setTitle(pageUpdateVO.getTitle());
            workSpaceDO.setName(pageUpdateVO.getTitle());
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

    private PageVO getPageInfo(PageDetailDO pageDetailDO, String operationType) {
        PageVO pageVO = new PageVO();
        BeanUtils.copyProperties(pageDetailDO, pageVO);

        WorkSpaceTreeVO workSpaceTreeVO = new WorkSpaceTreeVO();
        workSpaceTreeVO.setId(pageDetailDO.getWorkSpaceId());
        workSpaceTreeVO.setParentId(pageDetailDO.getWorkSpaceParentId());
        workSpaceTreeVO.setIsExpanded(false);
        workSpaceTreeVO.setCreatedBy(pageDetailDO.getCreatedBy());
        if (operationType.equals(BaseStage.INSERT)) {
            workSpaceTreeVO.setHasChildren(false);
            workSpaceTreeVO.setChildren(Collections.emptyList());
        } else if (operationType.equals(BaseStage.UPDATE)) {
            List<WorkSpaceDO> list = workSpaceRepository.workSpacesByParentId(pageDetailDO.getWorkSpaceId());
            if (list.isEmpty()) {
                workSpaceTreeVO.setHasChildren(false);
                workSpaceTreeVO.setChildren(Collections.emptyList());
            } else {
                workSpaceTreeVO.setHasChildren(true);
                List<Long> children = list.stream().map(WorkSpaceDO::getId).collect(Collectors.toList());
                workSpaceTreeVO.setChildren(children);
            }
        }
        WorkSpaceTreeVO.Data data = new WorkSpaceTreeVO.Data();
        data.setTitle(pageDetailDO.getTitle());
        workSpaceTreeVO.setData(data);
        pageVO.setWorkSpace(workSpaceTreeVO);

        PageVO.PageInfo pageInfo = new PageVO.PageInfo();
        pageInfo.setId(pageDetailDO.getPageId());
        BeanUtils.copyProperties(pageDetailDO, pageInfo);
        pageVO.setPageInfo(pageInfo);

        return pageVO;
    }

    private PageVO getReferencePageInfo(PageDetailDO pageDetailDO) {
        PageVO pageVO = new PageVO();
        BeanUtils.copyProperties(pageDetailDO, pageVO);

        WorkSpaceTreeVO workSpaceTreeVO = new WorkSpaceTreeVO();
        workSpaceTreeVO.setId(pageDetailDO.getWorkSpaceId());
        workSpaceTreeVO.setParentId(pageDetailDO.getWorkSpaceParentId());
        workSpaceTreeVO.setIsExpanded(false);
        List<WorkSpaceDO> list = workSpaceRepository.workSpacesByParentId(pageDetailDO.getWorkSpaceId());
        if (list.isEmpty()) {
            workSpaceTreeVO.setHasChildren(false);
            workSpaceTreeVO.setChildren(Collections.emptyList());
        } else {
            workSpaceTreeVO.setHasChildren(true);
            List<Long> children = list.stream().map(WorkSpaceDO::getId).collect(Collectors.toList());
            workSpaceTreeVO.setChildren(children);
        }
        WorkSpaceTreeVO.Data data = new WorkSpaceTreeVO.Data();
        data.setTitle(pageDetailDO.getTitle());
        workSpaceTreeVO.setData(data);
        pageVO.setWorkSpace(workSpaceTreeVO);

        pageVO.setPageInfo(null);

        return pageVO;
    }

    @Override
    public Map<String, Object> queryAllChildTreeByWorkSpaceId(Long workSpaceId, Boolean isNeedChild) {
        List<WorkSpaceDO> workSpaceDOList;
        if (isNeedChild) {
            workSpaceDOList = workSpaceRepository.queryAllChildByWorkSpaceId(workSpaceId);
        } else {
            WorkSpaceDO workSpaceDO = workSpaceRepository.selectById(workSpaceId);
            workSpaceDOList = Arrays.asList(workSpaceDO);
        }
        Map<String, Object> result = new HashMap<>(2);
        Map<Long, WorkSpaceTreeVO> workSpaceTreeMap = new HashMap<>(workSpaceDOList.size());
        Map<Long, List<Long>> groupMap = workSpaceDOList.stream().collect(Collectors.
                groupingBy(WorkSpaceDO::getParentId, Collectors.mapping(WorkSpaceDO::getId, Collectors.toList())));
        //创建topTreeVO
        WorkSpaceDO topSpace = new WorkSpaceDO();
        topSpace.setName(TOP_TITLE);
        topSpace.setParentId(0L);
        topSpace.setId(0L);
        workSpaceTreeMap.put(0L, buildTreeVO(topSpace, Arrays.asList(workSpaceId)));
        for (WorkSpaceDO workSpaceDO : workSpaceDOList) {
            WorkSpaceTreeVO treeVO = buildTreeVO(workSpaceDO, groupMap.get(workSpaceDO.getId()));
            workSpaceTreeMap.put(workSpaceDO.getId(), treeVO);
        }
        //默认第一级展开
        if (isNeedChild) {
            WorkSpaceTreeVO treeVO = workSpaceTreeMap.get(workSpaceId);
            if (treeVO != null && treeVO.getHasChildren()) {
                treeVO.setIsExpanded(true);
            }
        }

        result.put(ROOT_ID, 0L);
        result.put(ITEMS, workSpaceTreeMap);
        return result;
    }

    @Override
    public Map<String, Object> queryAllTree(Long resourceId, Long expandWorkSpaceId, String type) {
        Map<String, Object> result = new HashMap<>(2);
        List<WorkSpaceDO> workSpaceDOList = workSpaceRepository.queryAll(resourceId, type);
        Map<Long, WorkSpaceTreeVO> workSpaceTreeMap = new HashMap<>(workSpaceDOList.size());
        Map<Long, List<Long>> groupMap = workSpaceDOList.stream().collect(Collectors.
                groupingBy(WorkSpaceDO::getParentId, Collectors.mapping(WorkSpaceDO::getId, Collectors.toList())));
        //创建topTreeVO
        WorkSpaceDO topSpace = new WorkSpaceDO();
        topSpace.setName(TOP_TITLE);
        topSpace.setParentId(0L);
        topSpace.setId(0L);
        List<Long> topChildIds = groupMap.get(0L);
        workSpaceTreeMap.put(0L, buildTreeVO(topSpace, topChildIds));
        for (WorkSpaceDO workSpaceDO : workSpaceDOList) {
            WorkSpaceTreeVO treeVO = buildTreeVO(workSpaceDO, groupMap.get(workSpaceDO.getId()));
            workSpaceTreeMap.put(workSpaceDO.getId(), treeVO);
        }
        //设置展开的工作空间，并设置点击当前
        if (expandWorkSpaceId != null) {
            WorkSpaceDO workSpaceDO;
            if (PageResourceType.ORGANIZATION.getResourceType().equals(type)) {
                workSpaceDO = workSpaceRepository.queryById(resourceId, null, expandWorkSpaceId);
            } else {
                workSpaceDO = workSpaceRepository.queryById(null, resourceId, expandWorkSpaceId);
            }
            List<Long> expandIds = Stream.of(workSpaceDO.getRoute().split("\\.")).map(Long::parseLong).collect(Collectors.toList());
            for (Long expandId : expandIds) {
                WorkSpaceTreeVO treeVO = workSpaceTreeMap.get(expandId);
                if (treeVO != null) {
                    treeVO.setIsExpanded(true);
                }
            }
            WorkSpaceTreeVO treeVO = workSpaceTreeMap.get(expandWorkSpaceId);
            if (treeVO != null) {
                treeVO.setIsExpanded(false);
                treeVO.setIsClick(true);
            }
        }
        result.put(ROOT_ID, 0L);
        result.put(ITEMS, workSpaceTreeMap);
        return result;
    }

    /**
     * 构建treeVO
     *
     * @param workSpaceDO
     * @param childIds
     * @return
     */
    private WorkSpaceTreeVO buildTreeVO(WorkSpaceDO workSpaceDO, List<Long> childIds) {
        WorkSpaceTreeVO treeVO = new WorkSpaceTreeVO();
        treeVO.setCreatedBy(workSpaceDO.getCreatedBy());
        if (CollectionUtils.isEmpty(childIds)) {
            treeVO.setHasChildren(false);
            treeVO.setChildren(Collections.emptyList());
        } else {
            treeVO.setHasChildren(true);
            treeVO.setChildren(childIds);
        }
        WorkSpaceTreeVO.Data data = new WorkSpaceTreeVO.Data();
        data.setTitle(workSpaceDO.getName());
        treeVO.setData(data);
        treeVO.setIsExpanded(false);
        treeVO.setIsClick(false);
        treeVO.setParentId(workSpaceDO.getParentId());
        treeVO.setId(workSpaceDO.getId());
        treeVO.setRoute(workSpaceDO.getRoute());
        return treeVO;
    }

    @Override
    public List<WorkSpaceDO> queryAllSpaceByProject() {
        return workSpaceMapper.selectAll();
    }

    private void dfs(WorkSpaceVO workSpaceVO, Map<Long, List<WorkSpaceVO>> groupMap) {
        List<WorkSpaceVO> subList = workSpaceVO.getChildren();
        if (subList == null || subList.isEmpty()) {
            return;
        }
        for (WorkSpaceVO workSpace : subList) {
            workSpace.setChildren(groupMap.get(workSpace.getId()));
            dfs(workSpace, groupMap);
        }
    }

    @Override
    public List<WorkSpaceVO> queryAllSpaceByOptions(Long resourceId, String type) {
        List<WorkSpaceVO> result = new ArrayList<>();
        List<WorkSpaceDO> workSpaceDOList = workSpaceRepository.queryAll(resourceId, type);
        Map<Long, List<WorkSpaceVO>> groupMap = workSpaceDOList.stream().collect(Collectors.
                groupingBy(WorkSpaceDO::getParentId, Collectors.mapping(item -> {
                    WorkSpaceVO workSpaceVO = new WorkSpaceVO(item.getId(), item.getName(), item.getRoute());
                    return workSpaceVO;
                }, Collectors.toList())));
        for (WorkSpaceDO workSpaceDO : workSpaceDOList) {
            if (Objects.equals(workSpaceDO.getParentId(), 0L)) {
                WorkSpaceVO workSpaceVO = new WorkSpaceVO(workSpaceDO.getId(), workSpaceDO.getName(), workSpaceDO.getRoute());
                workSpaceVO.setChildren(groupMap.get(workSpaceDO.getId()));
                dfs(workSpaceVO, groupMap);
                result.add(workSpaceVO);
            }
        }
        return result;
    }

    @Override
    public List<WorkSpaceVO> querySpaceByIds(Long projectId, List<Long> spaceIds) {
        if (spaceIds == null || spaceIds.isEmpty()) {
            return new ArrayList();
        }
        List<WorkSpaceDO> workSpaceDOList = workSpaceMapper.selectSpaceByIds(projectId, spaceIds);
        List<WorkSpaceVO> result = new ArrayList<>();
        for (WorkSpaceDO workSpaceDO : workSpaceDOList) {
            WorkSpaceVO workSpaceVO = new WorkSpaceVO();
            workSpaceVO.setId(workSpaceDO.getId());
            workSpaceVO.setName(workSpaceDO.getName());
            result.add(workSpaceVO);
        }
        return result;
    }
}
