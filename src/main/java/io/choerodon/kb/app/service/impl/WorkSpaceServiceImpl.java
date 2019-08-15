package io.choerodon.kb.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.api.vo.*;
import io.choerodon.kb.app.service.*;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.dto.*;
import io.choerodon.kb.infra.enums.ReferenceType;
import io.choerodon.kb.infra.feign.BaseFeignClient;
import io.choerodon.kb.infra.feign.vo.UserDO;
import io.choerodon.kb.infra.mapper.*;
import io.choerodon.kb.infra.repository.PageAttachmentRepository;
import io.choerodon.kb.infra.repository.PageCommentRepository;
import io.choerodon.kb.infra.repository.PageRepository;
import io.choerodon.kb.infra.utils.EsRestUtil;
import io.choerodon.kb.infra.utils.RankUtil;
import io.choerodon.kb.infra.utils.TypeUtil;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author shinan.chen
 * @since 2019/7/17
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class WorkSpaceServiceImpl implements WorkSpaceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkSpaceServiceImpl.class);
    private static final String ROOT_ID = "rootId";
    private static final String ITEMS = "items";
    private static final String TOP_TITLE = "choerodon";
    private static final String TREE_NAME = "name";
    private static final String TREE_NAME_PRO = "我的项目";
    private static final String TREE_NAME_ORG = "我的组织";
    private static final String TREE_CODE = "code";
    private static final String TREE_CODE_PRO = "pro";
    private static final String TREE_CODE_ORG = "org";
    private static final String TREE_DATA = "data";
    private static final String TREE_IS_OPERATE = "isOperate";
    private static final String SETTING_TYPE_EDIT_MODE = "edit_mode";
    private static final String ERROR_WORKSPACE_INSERT = "error.workspace.insert";
    private static final String ERROR_WORKSPACE_UPDATE = "error.workspace.update";
    private static final String ERROR_WORKSPACE_ILLEGAL = "error.workspace.illegal";
    private static final String ERROR_WORKSPACE_NOTFOUND = "error.workspace.notFound";

    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private PageCommentRepository pageCommentRepository;
    @Autowired
    private PageAttachmentRepository pageAttachmentRepository;
    @Autowired
    private WorkSpacePageService workSpacePageService;
    @Autowired
    private BaseFeignClient baseFeignClient;
    @Autowired
    private PageVersionService pageVersionService;
    @Autowired
    private PageLogService pageLogService;
    @Autowired
    private PageAttachmentService pageAttachmentService;
    @Autowired
    private PageCommentService pageCommentService;
    @Autowired
    private WorkSpaceShareService workSpaceShareService;
    @Autowired
    private PageService pageService;
    @Autowired
    private WorkSpaceMapper workSpaceMapper;
    @Autowired
    private UserSettingMapper userSettingMapper;
    @Autowired
    private EsRestUtil esRestUtil;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PageAttachmentMapper pageAttachmentMapper;
    @Autowired
    private PageContentMapper pageContentMapper;
    @Autowired
    private PageVersionMapper pageVersionMapper;

    public void setBaseFeignClient(BaseFeignClient baseFeignClient) {
        this.baseFeignClient = baseFeignClient;
    }

    @Override
    public WorkSpaceDTO baseCreate(WorkSpaceDTO workSpaceDTO) {
        if (workSpaceMapper.insert(workSpaceDTO) != 1) {
            throw new CommonException(ERROR_WORKSPACE_INSERT);
        }
        return workSpaceMapper.selectByPrimaryKey(workSpaceDTO.getId());
    }

    @Override
    public WorkSpaceDTO baseUpdate(WorkSpaceDTO workSpaceDTO) {
        if (workSpaceMapper.updateByPrimaryKey(workSpaceDTO) != 1) {
            throw new CommonException(ERROR_WORKSPACE_UPDATE);
        }
        return workSpaceMapper.selectByPrimaryKey(workSpaceDTO.getId());
    }

    @Override
    public WorkSpaceDTO selectById(Long id) {
        WorkSpaceDTO workSpaceDTO = workSpaceMapper.selectByPrimaryKey(id);
        if (workSpaceDTO == null) {
            throw new CommonException("error.work.space.select");
        }
        return workSpaceDTO;
    }

    @Override
    public WorkSpaceDTO baseQueryById(Long organizationId, Long projectId, Long workSpaceId) {
        WorkSpaceDTO workSpaceDTO = workSpaceMapper.selectByPrimaryKey(workSpaceId);
        if (workSpaceDTO == null) {
            throw new CommonException(ERROR_WORKSPACE_NOTFOUND);
        }
        if (organizationId != null && workSpaceDTO.getOrganizationId() != null && !workSpaceDTO.getOrganizationId().equals(organizationId)) {
            throw new CommonException(ERROR_WORKSPACE_ILLEGAL);
        }
        if (projectId != null && workSpaceDTO.getProjectId() != null && !workSpaceDTO.getProjectId().equals(projectId)) {
            throw new CommonException(ERROR_WORKSPACE_ILLEGAL);
        }
        return workSpaceDTO;
    }

    @Override
    public WorkSpaceDTO baseQueryByIdWithOrg(Long organizationId, Long projectId, Long workSpaceId) {
        WorkSpaceDTO workSpaceDTO = workSpaceMapper.selectByPrimaryKey(workSpaceId);
        if (workSpaceDTO == null) {
            throw new CommonException(ERROR_WORKSPACE_NOTFOUND);
        }
        if (organizationId != null && workSpaceDTO.getOrganizationId() != null && !workSpaceDTO.getOrganizationId().equals(organizationId)) {
            throw new CommonException(ERROR_WORKSPACE_ILLEGAL);
        }
        if (workSpaceDTO.getProjectId() == null) {
            return workSpaceDTO;
        }
        if (projectId != null && workSpaceDTO.getProjectId() != null && !workSpaceDTO.getProjectId().equals(projectId)) {
            throw new CommonException(ERROR_WORKSPACE_ILLEGAL);
        }
        return workSpaceDTO;
    }

    @Override
    public void checkById(Long organizationId, Long projectId, Long workSpaceId) {
        baseQueryById(organizationId, projectId, workSpaceId);
    }

    @Override
    public List<WorkSpaceDTO> queryAllChildByWorkSpaceId(Long workSpaceId) {
        WorkSpaceDTO workSpaceDTO = selectById(workSpaceId);
        List<WorkSpaceDTO> list = workSpaceMapper.selectAllChildByRoute(workSpaceDTO.getRoute());
        list.add(workSpaceDTO);
        return list;
    }

    @Override
    public WorkSpaceInfoVO createWorkSpaceAndPage(Long organizationId, Long projectId, PageCreateWithoutContentVO createVO) {
        LOGGER.info("start create page...");
        //创建空页面
        PageDTO page = pageService.createPage(organizationId, projectId, createVO);
        WorkSpaceDTO workSpaceDTO = new WorkSpaceDTO();
        workSpaceDTO.setOrganizationId(organizationId);
        workSpaceDTO.setProjectId(projectId);
        workSpaceDTO.setName(page.getTitle());
        //获取父空间id和route
        Long parentId = createVO.getParentWorkspaceId();
        String route = "";
        if (!parentId.equals(0L)) {
            WorkSpaceDTO parentWorkSpace = this.baseQueryById(organizationId, projectId, parentId);
            route = parentWorkSpace.getRoute();
        }
        //设置rank值
        if (workSpaceMapper.hasChildWorkSpace(organizationId, projectId, parentId)) {
            String rank = workSpaceMapper.queryMaxRank(organizationId, projectId, parentId);
            workSpaceDTO.setRank(RankUtil.genNext(rank));
        } else {
            workSpaceDTO.setRank(RankUtil.mid());
        }
        workSpaceDTO.setParentId(parentId);
        //创建空间
        workSpaceDTO = this.baseCreate(workSpaceDTO);
        //设置新的route
        String realRoute = route.isEmpty() ? workSpaceDTO.getId().toString() : route + "." + workSpaceDTO.getId();
        workSpaceDTO.setRoute(realRoute);
        this.baseUpdate(workSpaceDTO);
        //创建空间和页面的关联关系
        this.insertWorkSpacePage(page.getId(), workSpaceDTO.getId());
        //返回workSpaceInfo
        WorkSpaceInfoVO workSpaceInfoVO = workSpaceMapper.queryWorkSpaceInfo(workSpaceDTO.getId());
        workSpaceInfoVO.setWorkSpace(buildTreeVO(workSpaceDTO, Collections.emptyList()));
        return workSpaceInfoVO;
    }

    @Override
    public WorkSpaceInfoVO queryWorkSpaceInfo(Long organizationId, Long projectId, Long workSpaceId, String searchStr) {
        WorkSpaceDTO workSpaceDTO = this.baseQueryByIdWithOrg(organizationId, projectId, workSpaceId);
        WorkSpaceInfoVO workSpaceInfo = workSpaceMapper.queryWorkSpaceInfo(workSpaceId);
        workSpaceInfo.setWorkSpace(buildTreeVO(workSpaceDTO, Collections.emptyList()));
        //是否有操作的权限（用于项目层只能查看组织层文档，不能操作）
        workSpaceInfo.setIsOperate(!(workSpaceDTO.getProjectId() == null && projectId != null));
        fillUserData(workSpaceInfo);
        handleHasDraft(workSpaceDTO.getOrganizationId(), workSpaceDTO.getProjectId(), workSpaceInfo);
        handleSearchStrHighlight(searchStr, workSpaceInfo.getPageInfo());
        setUserSettingInfo(organizationId, projectId, workSpaceInfo);
        workSpaceInfo.setPageAttachments(pageAttachmentService.queryByList(organizationId, projectId, workSpaceInfo.getPageInfo().getId()));
        workSpaceInfo.setPageComments(pageCommentService.queryByPageId(organizationId, projectId, workSpaceInfo.getPageInfo().getId()));
        return workSpaceInfo;
    }

    private void fillUserData(WorkSpaceInfoVO workSpaceInfoVO) {
        PageInfoVO pageInfo = workSpaceInfoVO.getPageInfo();
        List<Long> userIds = Arrays.asList(workSpaceInfoVO.getCreatedBy(), workSpaceInfoVO.getLastUpdatedBy(), pageInfo.getCreatedBy(), pageInfo.getLastUpdatedBy());
        Map<Long, UserDO> map = baseFeignClient.listUsersByIds(userIds.toArray(new Long[userIds.size()]), false).getBody().stream().collect(Collectors.toMap(UserDO::getId, x -> x));
        UserDO workSpaceCreateUser = map.get(workSpaceInfoVO.getCreatedBy());
        workSpaceInfoVO.setCreateName(workSpaceCreateUser != null ? workSpaceCreateUser.getLoginName() + workSpaceCreateUser.getRealName() : null);
        UserDO workSpaceUpdateUser = map.get(workSpaceInfoVO.getLastUpdatedBy());
        workSpaceInfoVO.setLastUpdatedName(workSpaceUpdateUser != null ? workSpaceUpdateUser.getLoginName() + workSpaceUpdateUser.getRealName() : null);
        UserDO pageCreateUser = map.get(pageInfo.getCreatedBy());
        pageInfo.setCreateName(pageCreateUser != null ? pageCreateUser.getLoginName() + pageCreateUser.getRealName() : null);
        UserDO pageUpdateUser = map.get(pageInfo.getLastUpdatedBy());
        pageInfo.setLastUpdatedName(pageUpdateUser != null ? pageUpdateUser.getLoginName() + pageUpdateUser.getRealName() : null);
    }

    private void setUserSettingInfo(Long organizationId, Long projectId, WorkSpaceInfoVO workSpaceInfoVO) {
        CustomUserDetails customUserDetails = DetailsHelper.getUserDetails();
        if (customUserDetails == null) {
            return;
        }
        Long userId = customUserDetails.getUserId();
        List<UserSettingDTO> userSettingDTOList = userSettingMapper.selectByOption(organizationId, projectId, SETTING_TYPE_EDIT_MODE, userId);
        if (!userSettingDTOList.isEmpty() && userSettingDTOList.size() == 1) {
            workSpaceInfoVO.setUserSettingVO(modelMapper.map(userSettingDTOList.get(0), UserSettingVO.class));
        }
    }

    /**
     * 应用于全文检索，根据检索内容高亮内容
     *
     * @param searchStr
     * @param pageInfo
     */
    private void handleSearchStrHighlight(String searchStr, PageInfoVO pageInfo) {
        if (searchStr != null && !"".equals(searchStr)) {
            String highlightContent = esRestUtil.highlightContent(searchStr, pageInfo.getContent());
            pageInfo.setHighlightContent(highlightContent != null && !highlightContent.equals("") ? highlightContent : pageInfo.getContent());
        }
    }

    /**
     * 判断是否有草稿数据
     *
     * @param organizationId
     * @param projectId
     * @param workSpaceInfo
     */
    private void handleHasDraft(Long organizationId, Long projectId, WorkSpaceInfoVO workSpaceInfo) {
        PageContentDTO draft = pageService.queryDraftContent(organizationId, projectId, workSpaceInfo.getPageInfo().getId());
        if (draft != null) {
            workSpaceInfo.setHasDraft(true);
            workSpaceInfo.setCreateDraftDate(draft.getLastUpdateDate());
        } else {
            workSpaceInfo.setHasDraft(false);
        }
    }

    @Override
    public WorkSpaceInfoVO updateWorkSpaceAndPage(Long organizationId, Long projectId, Long workSpaceId, PageUpdateVO pageUpdateVO) {
        WorkSpaceDTO workSpaceDTO = this.baseQueryById(organizationId, projectId, workSpaceId);
        WorkSpacePageDTO workSpacePageDTO = workSpacePageService.selectByWorkSpaceId(workSpaceId);
        switch (workSpacePageDTO.getReferenceType()) {
            case ReferenceType.SELF:
                PageDTO pageDTO = pageRepository.selectById(workSpacePageDTO.getPageId());
                pageDTO.setObjectVersionNumber(pageUpdateVO.getObjectVersionNumber());
                if (pageUpdateVO.getTitle() != null) {
                    //更新标题
                    pageDTO.setTitle(pageUpdateVO.getTitle());
                    workSpaceDTO.setName(pageUpdateVO.getTitle());
                    this.baseUpdate(workSpaceDTO);
                }
                if (pageUpdateVO.getContent() != null) {
                    //更新内容
                    Long latestVersionId = pageVersionService.createVersionAndContent(pageDTO.getId(), pageUpdateVO.getContent(), pageDTO.getLatestVersionId(), false, pageUpdateVO.getMinorEdit());
                    pageDTO.setLatestVersionId(latestVersionId);
                }
                pageRepository.baseUpdate(pageDTO, true);
                break;
            default:
                break;
        }
        return queryWorkSpaceInfo(organizationId, projectId, workSpaceId, null);
    }

    @Override
    public void deleteWorkSpaceAndPage(Long organizationId, Long projectId, Long workspaceId, Boolean isAdmin) {
        WorkSpaceDTO workSpaceDTO = this.baseQueryById(organizationId, projectId, workspaceId);
        WorkSpacePageDTO workSpacePageDTO = workSpacePageService.selectByWorkSpaceId(workspaceId);
        if (!isAdmin) {
            Long currentUserId = DetailsHelper.getUserDetails().getUserId();
            PageDTO pageDTO = pageRepository.baseQueryById(organizationId, projectId, workSpacePageDTO.getPageId());
            if (!workSpacePageDTO.getCreatedBy().equals(currentUserId) && !pageDTO.getCreatedBy().equals(currentUserId)) {
                throw new CommonException(ERROR_WORKSPACE_ILLEGAL);
            }
        }
        workSpaceMapper.deleteByRoute(workSpaceDTO.getRoute());
        workSpacePageService.baseDelete(workSpacePageDTO.getId());
        pageRepository.baseDelete(workSpacePageDTO.getPageId());
        pageVersionMapper.deleteByPageId(workSpacePageDTO.getPageId());
        pageContentMapper.deleteByPageId(workSpacePageDTO.getPageId());
        pageCommentRepository.deleteByPageId(workSpacePageDTO.getPageId());
        List<PageAttachmentDTO> pageAttachmentDTOList = pageAttachmentMapper.selectByPageId(workSpacePageDTO.getPageId());
        for (PageAttachmentDTO pageAttachment : pageAttachmentDTOList) {
            pageAttachmentRepository.baseDelete(pageAttachment.getId());
            pageAttachmentService.deleteFile(pageAttachment.getUrl());
        }
        pageLogService.deleteByPageId(workSpacePageDTO.getPageId());
        workSpaceShareService.deleteByWorkSpaceId(workspaceId);
        esRestUtil.deletePage(BaseStage.ES_PAGE_INDEX, workSpacePageDTO.getPageId());
    }

    @Override
    public void moveWorkSpace(Long organizationId, Long projectId, Long workSpaceId, MoveWorkSpaceVO moveWorkSpaceVO) {
        if (moveWorkSpaceVO.getTargetId() != 0) {
            this.checkById(organizationId, projectId, moveWorkSpaceVO.getTargetId());
        }
        WorkSpaceDTO sourceWorkSpace = this.baseQueryById(organizationId, projectId, moveWorkSpaceVO.getId());
        String oldRoute = sourceWorkSpace.getRoute();
        String rank = "";
        if (moveWorkSpaceVO.getBefore()) {
            rank = beforeRank(organizationId, projectId, workSpaceId, moveWorkSpaceVO);
        } else {
            rank = afterRank(organizationId, projectId, workSpaceId, moveWorkSpaceVO);
        }
        sourceWorkSpace.setRank(rank);
        if (sourceWorkSpace.getParentId().equals(workSpaceId)) {
            this.baseUpdate(sourceWorkSpace);
        } else {
            if (workSpaceId.equals(0L)) {
                sourceWorkSpace.setParentId(0L);
                sourceWorkSpace.setRoute(TypeUtil.objToString(sourceWorkSpace.getId()));
            } else {
                WorkSpaceDTO parent = this.baseQueryById(organizationId, projectId, workSpaceId);
                sourceWorkSpace.setParentId(parent.getId());
                sourceWorkSpace.setRoute(parent.getRoute() + "." + sourceWorkSpace.getId());
            }
            sourceWorkSpace = this.baseUpdate(sourceWorkSpace);

            if (workSpaceMapper.hasChildWorkSpace(organizationId, projectId, sourceWorkSpace.getId())) {
                String newRoute = sourceWorkSpace.getRoute();
                workSpaceMapper.updateByRoute(organizationId, projectId, oldRoute, newRoute);
            }
        }
    }

    private String beforeRank(Long organizationId, Long projectId, Long workSpaceId, MoveWorkSpaceVO moveWorkSpaceVO) {
        if (Objects.equals(moveWorkSpaceVO.getTargetId(), 0L)) {
            return noOutsetBeforeRank(organizationId, projectId, workSpaceId);
        } else {
            return outsetBeforeRank(organizationId, projectId, workSpaceId, moveWorkSpaceVO);
        }
    }

    private String afterRank(Long organizationId, Long projectId, Long workSpaceId, MoveWorkSpaceVO moveWorkSpaceVO) {
        String leftRank = workSpaceMapper.queryRank(organizationId, projectId, moveWorkSpaceVO.getTargetId());
        String rightRank = workSpaceMapper.queryRightRank(organizationId, projectId, workSpaceId, leftRank);
        if (rightRank == null) {
            return RankUtil.genNext(leftRank);
        } else {
            return RankUtil.between(leftRank, rightRank);
        }
    }

    private String noOutsetBeforeRank(Long organizationId, Long projectId, Long workSpaceId) {
        String minRank = workSpaceMapper.queryMinRank(organizationId, projectId, workSpaceId);
        if (minRank == null) {
            return RankUtil.mid();
        } else {
            return RankUtil.genPre(minRank);
        }
    }

    private String outsetBeforeRank(Long organizationId, Long projectId, Long workSpaceId, MoveWorkSpaceVO moveWorkSpaceVO) {
        String rightRank = workSpaceMapper.queryRank(organizationId, projectId, moveWorkSpaceVO.getTargetId());
        String leftRank = workSpaceMapper.queryLeftRank(organizationId, projectId, workSpaceId, rightRank);
        if (leftRank == null) {
            return RankUtil.genPre(rightRank);
        } else {
            return RankUtil.between(leftRank, rightRank);
        }
    }

    /**
     * 创建workSpace与page的关联关系
     *
     * @param pageId
     * @param workSpaceId
     * @return
     */
    private WorkSpacePageDTO insertWorkSpacePage(Long pageId, Long workSpaceId) {
        WorkSpacePageDTO workSpacePageDTO = new WorkSpacePageDTO();
        workSpacePageDTO.setReferenceType(ReferenceType.SELF);
        workSpacePageDTO.setPageId(pageId);
        workSpacePageDTO.setWorkspaceId(workSpaceId);
        return workSpacePageService.baseCreate(workSpacePageDTO);
    }

    @Override
    public Map<String, Object> queryAllChildTreeByWorkSpaceId(Long workSpaceId, Boolean isNeedChild) {
        List<WorkSpaceDTO> workSpaceDTOList;
        if (isNeedChild) {
            workSpaceDTOList = this.queryAllChildByWorkSpaceId(workSpaceId);
        } else {
            WorkSpaceDTO workSpaceDTO = this.selectById(workSpaceId);
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
    public Map<String, Map<String, Object>> queryAllTreeList(Long organizationId, Long projectId, Long expandWorkSpaceId) {
        Map<String, Map<String, Object>> result = new HashMap<>(2);
        //获取树形结构
        Map<String, Object> treeObj = new HashMap<>(4);
        Map<String, Object> tree = queryAllTree(organizationId, projectId, expandWorkSpaceId);
        treeObj.put(TREE_NAME, projectId != null ? TREE_NAME_PRO : TREE_NAME_ORG);
        treeObj.put(TREE_CODE, projectId != null ? TREE_CODE_PRO : TREE_CODE_ORG);
        treeObj.put(TREE_DATA, tree);
        treeObj.put(TREE_IS_OPERATE, true);
        result.put(projectId != null ? TREE_CODE_PRO : TREE_CODE_ORG, treeObj);
        //若是项目层，则获取组织层数据
        if (projectId != null) {
            Map<String, Object> orgTreeObj = new HashMap<>(4);
            Map<String, Object> orgTree = queryAllTree(organizationId, null, expandWorkSpaceId);
            orgTreeObj.put(TREE_NAME, TREE_NAME_ORG);
            orgTreeObj.put(TREE_CODE, TREE_CODE_ORG);
            orgTreeObj.put(TREE_DATA, orgTree);
            orgTreeObj.put(TREE_IS_OPERATE, false);
            result.put(TREE_CODE_ORG, orgTreeObj);
        }
        return result;
    }

    @Override
    public Map<String, Object> queryAllTree(Long organizationId, Long projectId, Long expandWorkSpaceId) {
        Map<String, Object> result = new HashMap<>(2);
        List<WorkSpaceDTO> workSpaceDTOList = workSpaceMapper.queryAll(organizationId, projectId);
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
        if (expandWorkSpaceId != null && !expandWorkSpaceId.equals(0L)) {
            WorkSpaceDTO workSpaceDTO = this.baseQueryById(organizationId, projectId, expandWorkSpaceId);
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
    public List<WorkSpaceVO> queryAllSpaceByOptions(Long organizationId, Long projectId) {
        List<WorkSpaceVO> result = new ArrayList<>();
        List<WorkSpaceDTO> workSpaceDTOList = workSpaceMapper.queryAll(organizationId, projectId);
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
