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
import io.choerodon.kb.infra.dto.*;
import io.choerodon.kb.infra.dto.iam.ProjectDO;
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

        WorkSpaceDTO workSpaceDTO = new WorkSpaceDTO();
        PageDTO pageDTO = new PageDTO();
        pageDTO.setTitle(pageCreateVO.getTitle());
        if (PageResourceType.ORGANIZATION.getResourceType().equals(type)) {
            pageDTO.setOrganizationId(resourceId);
            workSpaceDTO.setOrganizationId(resourceId);
        } else {
            ProjectDO projectDO = iamRepository.queryIamProject(resourceId);
            LOGGER.info("projectId:{},get project info:{}", resourceId, projectDO.toString());
            pageDTO.setOrganizationId(projectDO.getOrganizationId());
            pageDTO.setProjectId(resourceId);
            workSpaceDTO.setOrganizationId(projectDO.getOrganizationId());
            workSpaceDTO.setProjectId(resourceId);
        }

        PageDTO page = this.insertPage(pageDTO, pageCreateVO);
        WorkSpaceDTO workSpace = this.insertWorkSpace(workSpaceDTO, page, resourceId, pageCreateVO, type);
        this.insertWorkSpacePage(page.getId(), workSpace.getId());

        return getPageInfo(workSpaceRepository.queryDetail(workSpace.getId()), BaseStage.INSERT);
    }

    @Override
    public PageVO queryDetail(Long organizationId, Long projectId, Long workSpaceId, String searchStr) {
        workSpaceRepository.checkById(organizationId, projectId, workSpaceId);
        WorkSpacePageDTO workSpacePageDTO = workSpacePageRepository.selectByWorkSpaceId(workSpaceId);
        String referenceType = workSpacePageDTO.getReferenceType();
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
        if (customUserDetails == null) {
            return;
        }
        Long userId = customUserDetails.getUserId();
        UserSettingDTO result;
        if (projectId == null) {
            UserSettingDTO userSettingDTO = new UserSettingDTO(organizationId, SETTING_TYPE_EDIT_MODE, userId);
            result = userSettingMapper.selectOne(userSettingDTO);
        } else {
            UserSettingDTO userSettingDTO = new UserSettingDTO(organizationId, projectId, SETTING_TYPE_EDIT_MODE, userId);
            result = userSettingMapper.selectOne(userSettingDTO);
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
        WorkSpaceDTO workSpaceDTO = workSpaceRepository.selectById(workspaceId);
        PageContentDTO draft = pageService.queryDraftContent(workSpaceDTO.getOrganizationId(), workSpaceDTO.getProjectId(), pageVO.getPageInfo().getId());
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
        WorkSpacePageDTO workSpacePageDTO = workSpaceValidator.checkUpdatePage(pageUpdateVO, id);
        if (BaseStage.SELF.equals(workSpacePageDTO.getReferenceType())) {
            PageDTO pageDTO = pageRepository.selectById(workSpacePageDTO.getPageId());
            pageDTO.setObjectVersionNumber(pageUpdateVO.getObjectVersionNumber());
            this.updatePageInfo(id, pageUpdateVO, pageDTO);
        } else if (BaseStage.REFERENCE_URL.equals(workSpacePageDTO.getReferenceType())) {
            workSpacePageDTO.setObjectVersionNumber(pageUpdateVO.getObjectVersionNumber());
            workSpacePageDTO.setReferenceUrl(pageUpdateVO.getReferenceUrl());
            workSpacePageRepository.update(workSpacePageDTO);
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
        WorkSpaceDTO workSpaceDTO = this.selectWorkSpaceById(id);
        WorkSpacePageDTO workSpacePageDTO = workSpacePageRepository.selectByWorkSpaceId(id);
        if (!isAdmin) {
            Long currentUserId = DetailsHelper.getUserDetails().getUserId();
            if (!workSpacePageDTO.getCreatedBy().equals(currentUserId)) {
                throw new CommonException(ILLEGAL_ERROR);
            }
        }
        workSpaceRepository.deleteByRoute(workSpaceDTO.getRoute());
        workSpacePageRepository.delete(workSpacePageDTO.getId());
        pageRepository.delete(workSpacePageDTO.getPageId());
        pageVersionRepository.deleteByPageId(workSpacePageDTO.getPageId());
        pageContentRepository.deleteByPageId(workSpacePageDTO.getPageId());
        pageCommentRepository.deleteByPageId(workSpacePageDTO.getPageId());
        List<PageAttachmentDTO> pageAttachmentDTOList = pageAttachmentRepository.selectByPageId(workSpacePageDTO.getPageId());
        for (PageAttachmentDTO pageAttachment : pageAttachmentDTOList) {
            pageAttachmentRepository.delete(pageAttachment.getId());
            pageAttachmentService.deleteFile(pageAttachment.getUrl());
        }
        pageTagRepository.deleteByPageId(workSpacePageDTO.getPageId());
        pageLogRepository.deleteByPageId(workSpacePageDTO.getPageId());
        workSpaceShareRepository.deleteByWorkSpaceId(id);
        esRestUtil.deletePage(BaseStage.ES_PAGE_INDEX, workSpacePageDTO.getPageId());
    }

    @Override
    public void moveWorkSpace(Long resourceId, Long id, MoveWorkSpaceVO moveWorkSpaceVO, String type) {
        if (moveWorkSpaceVO.getTargetId() != 0) {
            this.checkWorkSpaceBelong(resourceId, moveWorkSpaceVO.getTargetId(), type);
        }
        WorkSpaceDTO sourceWorkSpace = this.checkWorkSpaceBelong(resourceId, moveWorkSpaceVO.getId(), type);
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
                WorkSpaceDTO parent = this.checkWorkSpaceBelong(resourceId, id, type);
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

    private WorkSpaceDTO selectWorkSpaceById(Long id) {
        return workSpaceRepository.selectById(id);
    }

    private PageDTO insertPage(PageDTO pageDTO, PageCreateVO pageCreateVO) {
        pageDTO.setLatestVersionId(0L);
        pageDTO = pageRepository.create(pageDTO);
        Long latestVersionId = pageVersionService.createVersionAndContent(pageDTO.getId(), pageCreateVO.getContent(), pageDTO.getLatestVersionId(), true, false);
        PageDTO page = pageRepository.selectById(pageDTO.getId());
        page.setLatestVersionId(latestVersionId);
        return pageRepository.update(page, false);
    }

    private WorkSpaceDTO insertWorkSpace(WorkSpaceDTO workSpaceDTO,
                                         PageDTO pageDTO,
                                         Long resourceId,
                                         PageCreateVO pageCreateVO,
                                         String type) {
        workSpaceDTO.setName(pageDTO.getTitle());
        Long parentId = 0L;
        String route = "";
        if (pageCreateVO.getParentWorkspaceId() != 0) {
            WorkSpaceDTO parentWorkSpace = this.selectWorkSpaceById(pageCreateVO.getParentWorkspaceId());
            parentId = parentWorkSpace.getId();
            route = parentWorkSpace.getRoute();
        }
        if (workSpaceRepository.hasChildWorkSpace(type, resourceId, parentId)) {
            String rank = workSpaceRepository.queryMaxRank(type, resourceId, parentId);
            workSpaceDTO.setRank(RankUtil.genNext(rank));
        } else {
            workSpaceDTO.setRank(RankUtil.mid());
        }
        workSpaceDTO.setParentId(parentId);
        workSpaceDTO = workSpaceRepository.insert(workSpaceDTO);

        String realRoute = route.isEmpty() ? workSpaceDTO.getId().toString() : route + "." + workSpaceDTO.getId();
        WorkSpaceDTO workSpace = workSpaceRepository.selectById(workSpaceDTO.getId());
        workSpace.setRoute(realRoute);
        return workSpaceRepository.update(workSpace);
    }

    private WorkSpacePageDTO insertWorkSpacePage(Long pageId, Long workSpaceId) {
        WorkSpacePageDTO workSpacePageDTO = new WorkSpacePageDTO();
        workSpacePageDTO.setReferenceType(BaseStage.SELF);
        workSpacePageDTO.setPageId(pageId);
        workSpacePageDTO.setWorkspaceId(workSpaceId);
        return workSpacePageRepository.insert(workSpacePageDTO);
    }

    private void updatePageInfo(Long id, PageUpdateVO pageUpdateVO, PageDTO pageDTO) {
        WorkSpaceDTO workSpaceDTO = this.selectWorkSpaceById(id);
        if (pageUpdateVO.getContent() != null) {
            Long latestVersionId = pageVersionService.createVersionAndContent(pageDTO.getId(), pageUpdateVO.getContent(), pageDTO.getLatestVersionId(), false, pageUpdateVO.getMinorEdit());
            pageDTO.setLatestVersionId(latestVersionId);
        }
        if (pageUpdateVO.getTitle() != null) {
            pageDTO.setTitle(pageUpdateVO.getTitle());
            workSpaceDTO.setName(pageUpdateVO.getTitle());
            workSpaceRepository.update(workSpaceDTO);
        }
        pageRepository.update(pageDTO, true);
    }


    private WorkSpaceDTO checkWorkSpaceBelong(Long resourceId, Long id, String type) {
        WorkSpaceDTO workSpaceDTO = this.selectWorkSpaceById(id);
        if (PageResourceType.ORGANIZATION.getResourceType().equals(type) && !workSpaceDTO.getOrganizationId().equals(resourceId)) {
            throw new CommonException("The workspace not found in the organization");
        } else if (PageResourceType.PROJECT.getResourceType().equals(type) && !workSpaceDTO.getProjectId().equals(resourceId)) {
            throw new CommonException("The workspace not found in the project");
        }

        return workSpaceDTO;
    }

    private PageVO getPageInfo(PageDetailDTO pageDetailDTO, String operationType) {
        PageVO pageVO = new PageVO();
        BeanUtils.copyProperties(pageDetailDTO, pageVO);

        WorkSpaceTreeVO workSpaceTreeVO = new WorkSpaceTreeVO();
        workSpaceTreeVO.setId(pageDetailDTO.getWorkSpaceId());
        workSpaceTreeVO.setParentId(pageDetailDTO.getWorkSpaceParentId());
        workSpaceTreeVO.setIsExpanded(false);
        workSpaceTreeVO.setCreatedBy(pageDetailDTO.getCreatedBy());
        if (operationType.equals(BaseStage.INSERT)) {
            workSpaceTreeVO.setHasChildren(false);
            workSpaceTreeVO.setChildren(Collections.emptyList());
        } else if (operationType.equals(BaseStage.UPDATE)) {
            List<WorkSpaceDTO> list = workSpaceRepository.workSpacesByParentId(pageDetailDTO.getWorkSpaceId());
            if (list.isEmpty()) {
                workSpaceTreeVO.setHasChildren(false);
                workSpaceTreeVO.setChildren(Collections.emptyList());
            } else {
                workSpaceTreeVO.setHasChildren(true);
                List<Long> children = list.stream().map(WorkSpaceDTO::getId).collect(Collectors.toList());
                workSpaceTreeVO.setChildren(children);
            }
        }
        WorkSpaceTreeVO.Data data = new WorkSpaceTreeVO.Data();
        data.setTitle(pageDetailDTO.getTitle());
        workSpaceTreeVO.setData(data);
        pageVO.setWorkSpace(workSpaceTreeVO);

        PageVO.PageInfo pageInfo = new PageVO.PageInfo();
        pageInfo.setId(pageDetailDTO.getPageId());
        BeanUtils.copyProperties(pageDetailDTO, pageInfo);
        pageVO.setPageInfo(pageInfo);

        return pageVO;
    }

    private PageVO getReferencePageInfo(PageDetailDTO pageDetailDTO) {
        PageVO pageVO = new PageVO();
        BeanUtils.copyProperties(pageDetailDTO, pageVO);

        WorkSpaceTreeVO workSpaceTreeVO = new WorkSpaceTreeVO();
        workSpaceTreeVO.setId(pageDetailDTO.getWorkSpaceId());
        workSpaceTreeVO.setParentId(pageDetailDTO.getWorkSpaceParentId());
        workSpaceTreeVO.setIsExpanded(false);
        List<WorkSpaceDTO> list = workSpaceRepository.workSpacesByParentId(pageDetailDTO.getWorkSpaceId());
        if (list.isEmpty()) {
            workSpaceTreeVO.setHasChildren(false);
            workSpaceTreeVO.setChildren(Collections.emptyList());
        } else {
            workSpaceTreeVO.setHasChildren(true);
            List<Long> children = list.stream().map(WorkSpaceDTO::getId).collect(Collectors.toList());
            workSpaceTreeVO.setChildren(children);
        }
        WorkSpaceTreeVO.Data data = new WorkSpaceTreeVO.Data();
        data.setTitle(pageDetailDTO.getTitle());
        workSpaceTreeVO.setData(data);
        pageVO.setWorkSpace(workSpaceTreeVO);

        pageVO.setPageInfo(null);

        return pageVO;
    }

    @Override
    public Map<String, Object> queryAllChildTreeByWorkSpaceId(Long workSpaceId, Boolean isNeedChild) {
        List<WorkSpaceDTO> workSpaceDTOList;
        if (isNeedChild) {
            workSpaceDTOList = workSpaceRepository.queryAllChildByWorkSpaceId(workSpaceId);
        } else {
            WorkSpaceDTO workSpaceDTO = workSpaceRepository.selectById(workSpaceId);
            workSpaceDTOList = Arrays.asList(workSpaceDTO);
        }
        Map<String, Object> result = new HashMap<>(2);
        Map<Long, WorkSpaceTreeVO> workSpaceTreeMap = new HashMap<>(workSpaceDTOList.size());
        Map<Long, List<Long>> groupMap = workSpaceDTOList.stream().collect(Collectors.
                groupingBy(WorkSpaceDTO::getParentId, Collectors.mapping(WorkSpaceDTO::getId, Collectors.toList())));
        //创建topTreeVO
        WorkSpaceDTO topSpace = new WorkSpaceDTO();
        topSpace.setName(TOP_TITLE);
        topSpace.setParentId(0L);
        topSpace.setId(0L);
        workSpaceTreeMap.put(0L, buildTreeVO(topSpace, Arrays.asList(workSpaceId)));
        for (WorkSpaceDTO workSpaceDTO : workSpaceDTOList) {
            WorkSpaceTreeVO treeVO = buildTreeVO(workSpaceDTO, groupMap.get(workSpaceDTO.getId()));
            workSpaceTreeMap.put(workSpaceDTO.getId(), treeVO);
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
        List<WorkSpaceDTO> workSpaceDTOList = workSpaceRepository.queryAll(resourceId, type);
        Map<Long, WorkSpaceTreeVO> workSpaceTreeMap = new HashMap<>(workSpaceDTOList.size());
        Map<Long, List<Long>> groupMap = workSpaceDTOList.stream().collect(Collectors.
                groupingBy(WorkSpaceDTO::getParentId, Collectors.mapping(WorkSpaceDTO::getId, Collectors.toList())));
        //创建topTreeVO
        WorkSpaceDTO topSpace = new WorkSpaceDTO();
        topSpace.setName(TOP_TITLE);
        topSpace.setParentId(0L);
        topSpace.setId(0L);
        List<Long> topChildIds = groupMap.get(0L);
        workSpaceTreeMap.put(0L, buildTreeVO(topSpace, topChildIds));
        for (WorkSpaceDTO workSpaceDTO : workSpaceDTOList) {
            WorkSpaceTreeVO treeVO = buildTreeVO(workSpaceDTO, groupMap.get(workSpaceDTO.getId()));
            workSpaceTreeMap.put(workSpaceDTO.getId(), treeVO);
        }
        //设置展开的工作空间，并设置点击当前
        if (expandWorkSpaceId != null) {
            WorkSpaceDTO workSpaceDTO;
            if (PageResourceType.ORGANIZATION.getResourceType().equals(type)) {
                workSpaceDTO = workSpaceRepository.queryById(resourceId, null, expandWorkSpaceId);
            } else {
                workSpaceDTO = workSpaceRepository.queryById(null, resourceId, expandWorkSpaceId);
            }
            List<Long> expandIds = Stream.of(workSpaceDTO.getRoute().split("\\.")).map(Long::parseLong).collect(Collectors.toList());
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
     * @param workSpaceDTO
     * @param childIds
     * @return
     */
    private WorkSpaceTreeVO buildTreeVO(WorkSpaceDTO workSpaceDTO, List<Long> childIds) {
        WorkSpaceTreeVO treeVO = new WorkSpaceTreeVO();
        treeVO.setCreatedBy(workSpaceDTO.getCreatedBy());
        if (CollectionUtils.isEmpty(childIds)) {
            treeVO.setHasChildren(false);
            treeVO.setChildren(Collections.emptyList());
        } else {
            treeVO.setHasChildren(true);
            treeVO.setChildren(childIds);
        }
        WorkSpaceTreeVO.Data data = new WorkSpaceTreeVO.Data();
        data.setTitle(workSpaceDTO.getName());
        treeVO.setData(data);
        treeVO.setIsExpanded(false);
        treeVO.setIsClick(false);
        treeVO.setParentId(workSpaceDTO.getParentId());
        treeVO.setId(workSpaceDTO.getId());
        treeVO.setRoute(workSpaceDTO.getRoute());
        return treeVO;
    }

    @Override
    public List<WorkSpaceDTO> queryAllSpaceByProject() {
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
        List<WorkSpaceDTO> workSpaceDTOList = workSpaceRepository.queryAll(resourceId, type);
        Map<Long, List<WorkSpaceVO>> groupMap = workSpaceDTOList.stream().collect(Collectors.
                groupingBy(WorkSpaceDTO::getParentId, Collectors.mapping(item -> {
                    WorkSpaceVO workSpaceVO = new WorkSpaceVO(item.getId(), item.getName(), item.getRoute());
                    return workSpaceVO;
                }, Collectors.toList())));
        for (WorkSpaceDTO workSpaceDTO : workSpaceDTOList) {
            if (Objects.equals(workSpaceDTO.getParentId(), 0L)) {
                WorkSpaceVO workSpaceVO = new WorkSpaceVO(workSpaceDTO.getId(), workSpaceDTO.getName(), workSpaceDTO.getRoute());
                workSpaceVO.setChildren(groupMap.get(workSpaceDTO.getId()));
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
        List<WorkSpaceDTO> workSpaceDTOList = workSpaceMapper.selectSpaceByIds(projectId, spaceIds);
        List<WorkSpaceVO> result = new ArrayList<>();
        for (WorkSpaceDTO workSpaceDTO : workSpaceDTOList) {
            WorkSpaceVO workSpaceVO = new WorkSpaceVO();
            workSpaceVO.setId(workSpaceDTO.getId());
            workSpaceVO.setName(workSpaceDTO.getName());
            result.add(workSpaceVO);
        }
        return result;
    }
}
