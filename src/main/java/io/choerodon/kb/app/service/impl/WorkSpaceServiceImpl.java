package io.choerodon.kb.app.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.reflect.TypeToken;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.core.utils.PageUtils;
import io.choerodon.kb.api.vo.*;
import io.choerodon.kb.app.service.*;
import io.choerodon.kb.app.service.assembler.WorkSpaceAssembler;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.dto.*;
import io.choerodon.kb.infra.enums.FileFormatType;
import io.choerodon.kb.infra.enums.OpenRangeType;
import io.choerodon.kb.infra.enums.ReferenceType;
import io.choerodon.kb.infra.enums.WorkSpaceType;
import io.choerodon.kb.infra.feign.BaseFeignClient;
import io.choerodon.kb.infra.feign.operator.AgileFeignOperator;
import io.choerodon.kb.infra.feign.vo.FileVO;
import io.choerodon.kb.infra.feign.vo.OrganizationDTO;
import io.choerodon.kb.infra.feign.vo.UserDO;
import io.choerodon.kb.infra.mapper.*;
import io.choerodon.kb.infra.repository.PageAttachmentRepository;
import io.choerodon.kb.infra.repository.PageCommentRepository;
import io.choerodon.kb.infra.repository.PageRepository;
import io.choerodon.kb.infra.utils.*;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import org.hzero.boot.file.dto.FileSimpleDTO;
import org.hzero.core.base.BaseConstants;
import org.hzero.starter.keyencrypt.core.EncryptContext;
import org.hzero.starter.keyencrypt.core.IEncryptionService;

/**
 * @author shinan.chen
 * @since 2019/7/17
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class WorkSpaceServiceImpl implements WorkSpaceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkSpaceServiceImpl.class);
    public static final String ROOT_ID = "rootId";
    public static final String ITEMS = "items";
    private static final String TOP_TITLE = "choerodon";
    private static final String TREE_NAME = "name";
    private static final String TREE_NAME_LIST = "所有文档";
    private static final String TREE_CODE = "code";
    private static final String TREE_CODE_PRO = "pro";
    private static final String TREE_CODE_ORG = "org";
    private static final String TREE_CODE_SHARE = "share";
    public static final String TREE_DATA = "data";
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
    private KnowledgeBaseService knowledgeBaseService;
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
    @Autowired
    private WorkSpaceAssembler workSpaceAssembler;
    @Autowired
    private KnowledgeBaseMapper knowledgeBaseMapper;
    @Autowired
    private AgileFeignOperator agileFeignOperator;
    @Autowired
    private PageLogMapper pageLogMapper;
    @Autowired
    private IEncryptionService encryptionService;
    @Autowired
    private BaseFeignService baseFeignService;
    @Autowired
    private ExpandFileClient expandFileClient;
    @Autowired
    private WorkSpacePageMapper workSpacePageMapper;


    public void setBaseFeignClient(BaseFeignClient baseFeignClient) {
        this.baseFeignClient = baseFeignClient;
    }

    @Override
    public WorkSpaceDTO baseCreate(WorkSpaceDTO workSpaceDTO) {
        workSpaceDTO.setDelete(false);
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
        if (workSpaceDTO.getOrganizationId() == 0L || (workSpaceDTO.getProjectId() != null && workSpaceDTO.getProjectId() == 0L)) {
            return workSpaceDTO;
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
        if (workSpaceDTO.getOrganizationId() == 0L || (workSpaceDTO.getProjectId() != null && workSpaceDTO.getProjectId() == 0L)) {
            return workSpaceDTO;
        }
        if (organizationId != null && workSpaceDTO.getOrganizationId() != null && !workSpaceDTO.getOrganizationId().equals(organizationId)) {
            throw new CommonException(ERROR_WORKSPACE_ILLEGAL);
        }
        if (projectId != null && workSpaceDTO.getProjectId() != null && !workSpaceDTO.getProjectId().equals(projectId)) {
            KnowledgeBaseDTO knowledgeBaseDTO = knowledgeBaseMapper.selectByPrimaryKey(workSpaceDTO.getBaseId());
            if (OpenRangeType.RANGE_PRIVATE.getType().equals(knowledgeBaseDTO.getOpenRange())) {
                throw new CommonException(ERROR_WORKSPACE_ILLEGAL);
            }
            if (OpenRangeType.RANGE_PROJECT.getType().equals(knowledgeBaseDTO.getOpenRange())) {
                String rangeProject = knowledgeBaseDTO.getRangeProject();
                List<String> strings = Arrays.asList(rangeProject.split(","));
                if (!strings.contains(String.valueOf(projectId))) {
                    throw new CommonException(ERROR_WORKSPACE_ILLEGAL);
                }
            }
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
        List<WorkSpaceDTO> list = workSpaceMapper.selectAllChildByRoute(workSpaceDTO.getRoute(), true);
        list.add(workSpaceDTO);
        return list;
    }

    @Override
    public WorkSpaceInfoVO createWorkSpaceAndPage(Long organizationId, Long projectId, PageCreateWithoutContentVO createVO) {
        //创建workspace的类型分成了三种  一种是文档，一种是文件，一种是文件夹
        switch (WorkSpaceType.valueOf(createVO.getType().toUpperCase())) {
            case DOCUMENT:
                return createDocument(organizationId, projectId, createVO);
            case FOLDER:
                return createFolder(organizationId, projectId, createVO);
            default:
                throw new CommonException("Unsupported knowledge space type");
        }

    }

    private WorkSpaceInfoVO createFolder(Long organizationId, Long projectId, PageCreateWithoutContentVO createVO) {
        WorkSpaceDTO workSpaceDTO = new WorkSpaceDTO();
        workSpaceDTO.setOrganizationId(organizationId);
        workSpaceDTO.setProjectId(projectId);
        workSpaceDTO.setName(createVO.getTitle());
        workSpaceDTO.setBaseId(createVO.getBaseId());
        workSpaceDTO.setDescription(createVO.getDescription());
        workSpaceDTO.setType(createVO.getType());

        //获取父空间id和route
        Long parentId = createVO.getParentWorkspaceId();
        String route = "";
        if (parentId != null && !parentId.equals(0L)) {
            WorkSpaceDTO parentWorkSpace = this.baseQueryById(organizationId, projectId, parentId);
            route = parentWorkSpace.getRoute();
        } else {
            parentId = 0L;
        }
        //设置rank值
        if (Boolean.TRUE.equals(workSpaceMapper.hasChildWorkSpace(organizationId, projectId, parentId))) {
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
        //返回workSpaceInfo
        WorkSpaceInfoVO workSpaceInfoVO = workSpaceMapper.queryWorkSpaceInfo(workSpaceDTO.getId());
        workSpaceInfoVO.setWorkSpace(buildTreeVO(workSpaceDTO, Collections.emptyList()));
        return workSpaceInfoVO;
    }

    private WorkSpaceInfoVO createDocument(Long organizationId, Long projectId, PageCreateWithoutContentVO createVO) {
        LOGGER.info("start create page...");
        //创建空页面
        PageDTO page = pageService.createPage(organizationId, projectId, createVO);
        WorkSpaceDTO workSpaceDTO = new WorkSpaceDTO();
        workSpaceDTO.setOrganizationId(organizationId);
        workSpaceDTO.setProjectId(projectId);
        workSpaceDTO.setName(page.getTitle());
        workSpaceDTO.setBaseId(createVO.getBaseId());
        workSpaceDTO.setDescription(createVO.getDescription());
        workSpaceDTO.setType(createVO.getType());
        //获取父空间id和route
        Long parentId = createVO.getParentWorkspaceId();
        String route = "";
        if (parentId != null && !parentId.equals(0L)) {
            WorkSpaceDTO parentWorkSpace = this.baseQueryById(organizationId, projectId, parentId);
            route = parentWorkSpace.getRoute();
        } else {
            parentId = 0L;
        }
        //设置rank值
        if (Boolean.TRUE.equals(workSpaceMapper.hasChildWorkSpace(organizationId, projectId, parentId))) {
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
        //根据WorkSpace的类型返回相应的值
        switch (WorkSpaceType.valueOf(workSpaceDTO.getType().toUpperCase())) {
            case FOLDER:
                //todo  前端点击文件夹的时候应该不会发送请求？？？？ 就展开下面的结构就行了？？？
                //查询文件夹下子项
                return queryFolderInfo(workSpaceDTO);
            case DOCUMENT:
                return getWorkSpaceInfoVO(organizationId, projectId, workSpaceId, searchStr, workSpaceDTO);
            case FILE:
                return queryFileInfo(organizationId, projectId, workSpaceId, workSpaceDTO);
            default:
                throw new CommonException("Unsupported knowledge space type");
        }

    }

    private WorkSpaceInfoVO queryFolderInfo(WorkSpaceDTO workSpaceDTO) {
        WorkSpaceInfoVO workSpaceInfoVO = new WorkSpaceInfoVO();
        workSpaceInfoVO.setWorkSpace(buildTreeVO(workSpaceDTO, Collections.emptyList()));
        workSpaceInfoVO.setDelete(workSpaceDTO.getDelete());
        return workSpaceInfoVO;
    }

    private WorkSpaceInfoVO queryFileInfo(Long organizationId, Long projectId, Long workSpaceId, WorkSpaceDTO workSpaceDTO) {
        WorkSpaceInfoVO file = workSpaceMapper.queryWorkSpaceInfo(workSpaceId);
        WorkSpaceDTO spaceDTO = workSpaceMapper.selectByPrimaryKey(workSpaceId);
        FileVO fileDTOByFileKey = expandFileClient.getFileDTOByFileKey(organizationId, workSpaceDTO.getFileKey());

        file.setFileType(CommonUtil.getFileType(fileDTOByFileKey.getFileKey()));
        file.setTitle(spaceDTO.getName());
        file.setUrl(fileDTOByFileKey.getFileUrl());
        file.setKey(CommonUtil.getFileId(fileDTOByFileKey.getFileKey()));

        BeanUtils.copyProperties(file, workSpaceDTO);
        file.setWorkSpace(buildTreeVO(workSpaceDTO, Collections.emptyList()));

        file.setPageComments(pageCommentService.queryByPageId(organizationId, projectId, file.getPageInfo().getId()));
        file.setDelete(workSpaceDTO.getDelete());
        return file;
    }

    private WorkSpaceInfoVO getWorkSpaceInfoVO(Long organizationId, Long projectId, Long workSpaceId, String searchStr, WorkSpaceDTO workSpaceDTO) {
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
        workSpaceInfo.setDelete(workSpaceDTO.getDelete());
        return workSpaceInfo;
    }

    private void fillUserData(WorkSpaceInfoVO workSpaceInfoVO) {
        PageInfoVO pageInfo = workSpaceInfoVO.getPageInfo();
        List<Long> userIds = Arrays.asList(workSpaceInfoVO.getCreatedBy(), workSpaceInfoVO.getLastUpdatedBy(), pageInfo.getCreatedBy(), pageInfo.getLastUpdatedBy());
        Map<Long, UserDO> map = baseFeignClient.listUsersByIds(userIds.toArray(new Long[userIds.size()]), false).getBody().stream().collect(Collectors.toMap(UserDO::getId, x -> x));
        workSpaceInfoVO.setCreateUser(map.get(workSpaceInfoVO.getCreatedBy()));
        workSpaceInfoVO.setLastUpdatedUser(map.get(workSpaceInfoVO.getLastUpdatedBy()));
        pageInfo.setCreateUser(map.get(pageInfo.getCreatedBy()));
        pageInfo.setLastUpdatedUser(map.get(pageInfo.getLastUpdatedBy()));
    }

    private void fillUserData(List<WorkSpaceRecentVO> recents, KnowledgeBaseDTO knowledgeBaseDTO) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        List<Long> userIds = recents.stream().map(WorkSpaceRecentVO::getLastUpdatedBy).collect(Collectors.toList());
        Map<Long, UserDO> map = baseFeignClient.listUsersByIds(userIds.toArray(new Long[userIds.size()]), false).getBody().stream().collect(Collectors.toMap(UserDO::getId, x -> x));
        for (WorkSpaceRecentVO recent : recents) {
            recent.setLastUpdatedUser(map.get(recent.getLastUpdatedBy()));
            recent.setLastUpdateDateStr(sdf.format(recent.getLastUpdateDate()));
            recent.setBaseId(knowledgeBaseDTO.getId());
            recent.setKnowledgeBaseName(knowledgeBaseDTO.getName());
        }
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
    public WorkSpaceInfoVO updateWorkSpaceAndPage(Long organizationId, Long projectId, Long workSpaceId, String searchStr, PageUpdateVO pageUpdateVO) {
        WorkSpaceDTO workSpaceDTO = this.baseQueryById(organizationId, projectId, workSpaceId);
        Boolean isTemplate = checkTemplate(organizationId, projectId, workSpaceDTO);
        WorkSpacePageDTO workSpacePageDTO = workSpacePageService.selectByWorkSpaceId(workSpaceId);
        switch (workSpacePageDTO.getReferenceType()) {
            case ReferenceType.SELF:
                PageDTO pageDTO = pageRepository.selectById(workSpacePageDTO.getPageId());
                pageDTO.setObjectVersionNumber(pageUpdateVO.getObjectVersionNumber());
                PageContentDTO pageContent = pageContentMapper.selectLatestByPageId(workSpacePageDTO.getPageId());
                if (pageUpdateVO.getTitle() != null) {
                    //更新标题
                    pageDTO.setTitle(pageUpdateVO.getTitle());
                    workSpaceDTO.setName(pageUpdateVO.getTitle());
                    this.baseUpdate(workSpaceDTO);
                    if (pageUpdateVO.getContent() == null) {
                        //若内容为空，则更新一个标题的改动版本
                        Long latestVersionId = pageVersionService.createVersionAndContent(pageDTO.getId(), pageUpdateVO.getTitle(), pageContent.getContent(), pageDTO.getLatestVersionId(), false, true);
                        pageDTO.setLatestVersionId(latestVersionId);
                    }
                }
                if (pageUpdateVO.getContent() != null) {
                    //更新内容
                    Long latestVersionId = pageVersionService.createVersionAndContent(pageDTO.getId(), pageDTO.getTitle(), pageUpdateVO.getContent(), pageDTO.getLatestVersionId(), false, pageUpdateVO.getMinorEdit());
                    pageDTO.setLatestVersionId(latestVersionId);
                }

                if (Boolean.TRUE.equals(isTemplate)) {
                    // 更改模板的描述
                    WorkSpaceDTO workSpace = workSpaceMapper.selectByPrimaryKey(workSpaceDTO.getId());
                    workSpace.setDescription(pageUpdateVO.getDescription());
                    baseUpdate(workSpace);
                }
                pageRepository.baseUpdate(pageDTO, true);
                break;
            default:
                break;
        }
        return queryWorkSpaceInfo(organizationId, projectId, workSpaceId, searchStr);
    }

    @Override
    public void removeWorkSpaceAndPage(Long organizationId, Long projectId, Long workspaceId, Boolean isAdmin) {
        //目前删除workSpace前端全部走的remove这个接口， 删除文档的逻辑  组织管理员可以删除组织下所有的，组织成员只能删除自己创建的
        WorkSpaceDTO workSpaceDTO = this.baseQueryById(organizationId, projectId, workspaceId);
        if (StringUtils.equalsIgnoreCase(workSpaceDTO.getType(), WorkSpaceType.FILE.getValue()) ||
                StringUtils.equalsIgnoreCase(workSpaceDTO.getType(), WorkSpaceType.FOLDER.getValue())) {
            WorkSpacePageDTO workSpacePageDTO = new WorkSpacePageDTO();
            workSpacePageDTO.setWorkspaceId(workspaceId);
            WorkSpacePageDTO workSpacePage = workSpacePageMapper.selectOne(workSpacePageDTO);
            checkRemovePermission(organizationId, projectId, workSpacePage, isAdmin);
        } else if (StringUtils.equalsIgnoreCase(workSpaceDTO.getType(), WorkSpaceType.DOCUMENT.getValue())) {
            WorkSpacePageDTO workSpacePageDTO = workSpacePageService.selectByWorkSpaceId(workspaceId);
            checkRemovePermission(organizationId, projectId, workSpacePageDTO, isAdmin);
        } else {
            throw new CommonException("Unsupported knowledge space type");
        }
        workSpaceDTO.setDelete(true);
        this.baseUpdate(workSpaceDTO);
        //删除agile关联的workspace
        agileFeignOperator.deleteByworkSpaceId(projectId == null ? organizationId : projectId, workspaceId);
    }

    private void checkRemovePermission(Long organizationId, Long projectId, WorkSpacePageDTO workSpacePageDTO, Boolean isAdmin) {
        if (isAdmin && workSpacePageDTO != null) {
            //删除文档的逻辑  组织管理员可以删除组织下所有的，组织成员只能删除自己创建的
            Long currentUserId = DetailsHelper.getUserDetails().getUserId();
            if (projectId != null) {
                //组织层校验
                ResponseEntity<Boolean> booleanResponseEntity = baseFeignClient.checkAdminPermission(projectId);
                if (booleanResponseEntity.getStatusCode().is2xxSuccessful() && booleanResponseEntity.getBody()) {
                    return;
                } else {
                    PageDTO pageDTO = pageRepository.baseQueryById(organizationId, projectId, workSpacePageDTO.getPageId());
                    if (!workSpacePageDTO.getCreatedBy().equals(currentUserId) && !pageDTO.getCreatedBy().equals(currentUserId)) {
                        throw new CommonException(ERROR_WORKSPACE_ILLEGAL);
                    }
                }
            } else {
                ResponseEntity<Boolean> booleanResponseEntity = baseFeignClient.checkIsOrgRoot(organizationId, currentUserId);
                if (booleanResponseEntity.getStatusCode().is2xxSuccessful() && booleanResponseEntity.getBody()) {
                    return;
                } else {
                    PageDTO pageDTO = pageRepository.baseQueryById(organizationId, projectId, workSpacePageDTO.getPageId());
                    if (!workSpacePageDTO.getCreatedBy().equals(currentUserId) && !pageDTO.getCreatedBy().equals(currentUserId)) {
                        throw new CommonException(ERROR_WORKSPACE_ILLEGAL);
                    }
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWorkSpaceAndPage(Long organizationId, Long projectId, Long workspaceId) {
        WorkSpaceDTO workSpaceDTO = this.baseQueryById(organizationId, projectId, workspaceId);
        Set<Long> deleteFolderIds = new HashSet<>();
        switch (WorkSpaceType.valueOf(workSpaceDTO.getType().toUpperCase())) {
            case FILE:
                deleteFile(organizationId, workSpaceDTO);
                break;
            case FOLDER:
                //删除文件夹下面的元素
                List<WorkSpaceDTO> workSpaceDTOS = workSpaceMapper.selectAllChildByRoute(workSpaceDTO.getRoute(), false);
                workSpaceDTOS.forEach(spaceDTO -> {
                    if (StringUtils.equalsIgnoreCase(spaceDTO.getType(), WorkSpaceType.FILE.getValue())) {
                        deleteFile(organizationId, spaceDTO);
                    } else if (StringUtils.equalsIgnoreCase(spaceDTO.getType(), WorkSpaceType.DOCUMENT.getValue())) {
                        deleteDocument(spaceDTO, organizationId);
                    } else {
                        //删除文件夹
                        workSpaceMapper.deleteByPrimaryKey(spaceDTO.getId());
                    }
                });
                workSpaceMapper.deleteByPrimaryKey(workSpaceDTO.getId());
                break;
            case DOCUMENT:
                deleteDocument(workSpaceDTO, organizationId);
                break;
            default:
                throw new CommonException("Unsupported knowledge space type");
        }
    }

    private void deleteDocument(WorkSpaceDTO workSpaceDTO, Long organizationId) {
        WorkSpacePageDTO workSpacePageDTO = workSpacePageService.selectByWorkSpaceId(workSpaceDTO.getId());
        // todo 未来如果有引用页面的空间，删除这里需要做处理
//        List<WorkSpaceDTO> workSpaces = workSpaceMapper.selectAllChildByRoute(workSpaceDTO.getRoute(), false);
        workSpaceDTO.setPageId(workSpacePageDTO.getPageId());
        workSpaceDTO.setWorkPageId(workSpacePageDTO.getId());
//        workSpaces.add(workSpaceDTO);
//        for (WorkSpaceDTO workSpace : workSpaces) {
        workSpaceMapper.deleteByPrimaryKey(workSpaceDTO.getId());
        workSpacePageService.baseDelete(workSpaceDTO.getWorkPageId());
        pageRepository.baseDelete(workSpaceDTO.getPageId());
        pageVersionMapper.deleteByPageId(workSpaceDTO.getPageId());
        pageContentMapper.deleteByPageId(workSpaceDTO.getPageId());
        pageCommentRepository.deleteByPageId(workSpaceDTO.getPageId());
        List<PageAttachmentDTO> pageAttachmentDTOList = pageAttachmentMapper.selectByPageId(workSpaceDTO.getPageId());
        for (PageAttachmentDTO pageAttachment : pageAttachmentDTOList) {
            pageAttachmentRepository.baseDelete(pageAttachment.getId());
            pageAttachmentService.deleteFile(organizationId, pageAttachment.getUrl());
        }
        pageLogService.deleteByPageId(workSpaceDTO.getPageId());
        workSpaceShareService.deleteByWorkSpaceId(workSpaceDTO.getId());
        esRestUtil.deletePage(BaseStage.ES_PAGE_INDEX, workSpaceDTO.getPageId());
//        }

    }

    private void deleteFile(Long organizationId, WorkSpaceDTO workSpaceDTO) {
        FileVO fileDTOByFileKey = expandFileClient.getFileDTOByFileKey(organizationId, workSpaceDTO.getFileKey());
        expandFileClient.deleteFileByUrlWithDbOptional(organizationId, BaseStage.BACKETNAME, Arrays.asList(fileDTOByFileKey.getFileUrl()));
        //删除workSpace
        workSpaceMapper.deleteByPrimaryKey(workSpaceDTO.getId());
        //删除 workspace page
        WorkSpacePageDTO spacePageDTO = workSpacePageService.selectByWorkSpaceId(workSpaceDTO.getId());
        workSpacePageService.baseDelete(spacePageDTO.getId());
        //删除评论
        pageCommentRepository.deleteByPageId(spacePageDTO.getId());
    }

    @Override
    public void restoreWorkSpaceAndPage(Long organizationId, Long projectId, Long workspaceId, Long baseId) {
        WorkSpaceDTO workSpaceDTO = this.baseQueryById(organizationId, projectId, workspaceId);
        if (!ObjectUtils.isEmpty(baseId)) {
            updateWorkSpace(workSpaceDTO, organizationId, projectId, workspaceId, baseId);
            return;
        }
        Boolean parentDelete = isParentDelete(workSpaceDTO, workspaceId, projectId);

        if (Boolean.TRUE.equals(parentDelete)) {
            //恢复到顶层
            updateWorkSpace(workSpaceDTO, organizationId, projectId, workspaceId, workSpaceDTO.getBaseId());
        } else {
            //恢复到父级
            workSpaceDTO.setDelete(false);
            baseUpdate(workSpaceDTO);
        }
    }

    @Override
    public Boolean belongToBaseExist(Long organizationId, Long projectId, Long workspaceId) {
        WorkSpaceDTO workSpaceDTO = new WorkSpaceDTO();
        workSpaceDTO.setId(workspaceId);
        workSpaceDTO.setOrganizationId(organizationId);
        workSpaceDTO.setProjectId(projectId);
        workSpaceDTO = workSpaceMapper.selectOne(workSpaceDTO);
        Assert.notNull(workSpaceDTO, ERROR_WORKSPACE_NOTFOUND);
        KnowledgeBaseDTO knowledgeBaseDTO = knowledgeBaseMapper.selectByPrimaryKey(workSpaceDTO.getBaseId());
        return !knowledgeBaseDTO.getDelete();
    }

    @Override
    public void moveWorkSpace(Long organizationId, Long projectId, Long workSpaceId, MoveWorkSpaceVO moveWorkSpaceVO) {
        if (moveWorkSpaceVO.getTargetId() != 0) {
            this.checkById(organizationId, projectId, moveWorkSpaceVO.getTargetId());
        }
        WorkSpaceDTO sourceWorkSpace = this.baseQueryById(organizationId, projectId, moveWorkSpaceVO.getId());
        String oldRoute = sourceWorkSpace.getRoute();
        String rank = "";
        if (Boolean.TRUE.equals(moveWorkSpaceVO.getBefore())) {
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

            if (Boolean.TRUE.equals(workSpaceMapper.hasChildWorkSpace(organizationId, projectId, sourceWorkSpace.getId()))) {
                String newRoute = sourceWorkSpace.getRoute();
                workSpaceMapper.updateChildByRoute(organizationId, projectId, oldRoute, newRoute);
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
        if (Boolean.TRUE.equals(isNeedChild)) {
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
        //根据fileKey 查询文件
        Map<String, FileVO> fileVOMap = new HashMap<>();
        Map<Long, UserDO> userDOMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(workSpaceDTOList)) {
            WorkSpaceDTO spaceDTO1 = workSpaceDTOList.get(0);
            fileVOMap = fillFileVOMap(spaceDTO1.getOrganizationId(), workSpaceDTOList, fileVOMap);
            userDOMap = fillUserDOMap(workSpaceDTOList);
        }
        workSpaceTreeMap.put(0L, buildTreeVO(topSpace, Arrays.asList(workSpaceId)));
        for (WorkSpaceDTO workSpaceDTO : workSpaceDTOList) {
            WorkSpaceTreeVO treeVO = buildTreeVO(workSpaceDTO, groupMap.get(workSpaceDTO.getId()));
            if (StringUtils.equalsIgnoreCase(treeVO.getType(), WorkSpaceType.FILE.getValue())) {
                fillFileInfo(fileVOMap, userDOMap, workSpaceDTO, treeVO);
            }
            workSpaceTreeMap.put(workSpaceDTO.getId(), treeVO);
        }
        //默认第一级展开
        if (Boolean.TRUE.equals(isNeedChild)) {
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
    public Map<String, Object> queryAllTreeList(Long organizationId, Long projectId, Long expandWorkSpaceId, Long baseId, String excludeType) {
        KnowledgeBaseDTO knowledgeBaseDTO = new KnowledgeBaseDTO();
        knowledgeBaseDTO.setOrganizationId(organizationId);
        knowledgeBaseDTO.setProjectId(projectId);
        knowledgeBaseDTO.setId(baseId);
        knowledgeBaseDTO = knowledgeBaseMapper.selfSelect(knowledgeBaseDTO);
        if (Objects.isNull(knowledgeBaseDTO)) {
            throw new CommonException(ERROR_WORKSPACE_NOTFOUND);
        }
        //获取树形结构
        Map<String, Object> treeObj = new HashMap<>(4);
        List<String> excludeTypes = new ArrayList<>();
        if (StringUtils.isNotEmpty(excludeType) && excludeType.contains(",")) {
            String[] split = excludeType.split(",");
            excludeTypes = new ArrayList<>(Arrays.asList(split));
        } else {
            excludeTypes.add(excludeType);
        }
        Map<String, Object> tree = queryAllTree(knowledgeBaseDTO.getOrganizationId(), knowledgeBaseDTO.getProjectId(), expandWorkSpaceId, baseId, excludeTypes);
        String treeCode = null;
        if (knowledgeBaseDTO.getProjectId() == null) {
            treeCode = TREE_CODE_ORG;
        } else if (projectId != null && knowledgeBaseDTO.getProjectId() != null) {
            if (projectId.equals(knowledgeBaseDTO.getProjectId())) {
                treeCode = TREE_CODE_PRO;
            } else {
                treeCode = TREE_CODE_SHARE;
            }
        }
        treeObj.put(TREE_NAME, TREE_NAME_LIST);
        treeObj.put(TREE_CODE, treeCode);
        treeObj.put(TREE_DATA, tree);
        return treeObj;
    }

    @Override
    public Map<String, Object> queryAllTree(Long organizationId, Long projectId, Long expandWorkSpaceId, Long baseId, List<String> excludeTypes) {
        Map<String, Object> result = new HashMap<>(2);
        List<WorkSpaceDTO> workSpaceDTOList = workSpaceMapper.queryAll(organizationId, projectId, baseId, null, excludeTypes);
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
        //根据fileKey 查询文件
        fillWorkSpaceAttribute(organizationId, workSpaceDTOList, workSpaceTreeMap, groupMap);
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
        treeVO.setType(workSpaceDTO.getType());
        treeVO.setFileKey(workSpaceDTO.getFileKey());
        treeVO.setFileType(CommonUtil.getFileType(workSpaceDTO.getFileKey()));
        treeVO.setCreationDate(workSpaceDTO.getCreationDate());
        treeVO.setLastUpdateDate(workSpaceDTO.getLastUpdateDate());
        return treeVO;
    }

    @Override
    public List<WorkSpaceVO> queryAllSpaceByOptions(Long organizationId, Long projectId, Long baseId, Long workSpaceId, String excludeType) {

        String type = null;
        WorkSpaceDTO spaceDTO = workSpaceMapper.selectByPrimaryKey(workSpaceId);
        if (spaceDTO != null) {
            type = spaceDTO.getType();
        }
        List<String> excludeTypes = new ArrayList<>();
        if (StringUtils.isNotEmpty(excludeType) && excludeType.contains(",")) {
            String[] split = excludeType.split(",");
            excludeTypes = new ArrayList<>(Arrays.asList(split));
        } else {
            excludeTypes.add(excludeType);
        }
//            1.「文档」支持移动或复制到「文档」或「文件夹」中；
//            2.「文件」仅支持移动或复制到「文件夹」中；
//            3.「文件夹」仅支持移动到「文件夹」中。
        List<WorkSpaceVO> result = new ArrayList<>();
        List<WorkSpaceDTO> workSpaceDTOList = workSpaceMapper.queryAll(organizationId, projectId, baseId, type, excludeTypes);
        //文档不能移到自己下面和自己的子集下面
        if (StringUtils.equalsIgnoreCase(type, WorkSpaceType.DOCUMENT.getValue())) {
            List<WorkSpaceDTO> workSpaceDTOS = workSpaceMapper.selectAllChildByRoute(spaceDTO.getRoute(), false);
            workSpaceDTOS.add(spaceDTO);
            if (!CollectionUtils.isEmpty(workSpaceDTOS) && !CollectionUtils.isEmpty(workSpaceDTOList)) {
                List<Long> subIds = workSpaceDTOS.stream().map(WorkSpaceDTO::getId).collect(Collectors.toList());
                workSpaceDTOList = workSpaceDTOList.stream().filter(spaceDTO1 -> !subIds.contains(spaceDTO1.getId())).collect(Collectors.toList());
            }
        }

        if (EncryptContext.isEncrypt()) {
            workSpaceDTOList.forEach(w -> {
                String route = w.getRoute();
                route = Optional.ofNullable(StringUtils.split(route, BaseConstants.Symbol.POINT))
                        .map(list -> Stream.of(list)
                                .map(str -> encryptionService.encrypt(str, ""))
                                .collect(Collectors.joining(BaseConstants.Symbol.POINT)))
                        .orElse(null);
                w.setRoute(route);
            });
        }
        Map<Long, List<WorkSpaceVO>> groupMap = workSpaceDTOList.stream().collect(Collectors.
                groupingBy(WorkSpaceDTO::getParentId, Collectors.mapping(item -> {
                    WorkSpaceVO workSpaceVO = new WorkSpaceVO(item.getId(), item.getName(), item.getRoute(), item.getType(), CommonUtil.getFileType(item.getFileKey()));
                    return workSpaceVO;
                }, Collectors.toList())));
        for (WorkSpaceDTO workSpaceDTO : workSpaceDTOList) {
            if (Objects.equals(workSpaceDTO.getParentId(), 0L)) {
                WorkSpaceVO workSpaceVO = new WorkSpaceVO(workSpaceDTO.getId(), workSpaceDTO.getName(), workSpaceDTO.getRoute(), workSpaceDTO.getType(), CommonUtil.getFileType(workSpaceDTO.getFileKey()));
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
            workSpaceVO.setBaseId(workSpaceDTO.getBaseId());
            workSpaceVO.setFileType(CommonUtil.getFileType(workSpaceDTO.getFileKey()));
            workSpaceVO.setType(workSpaceDTO.getType());
            result.add(workSpaceVO);
        }
        return result;
    }

    @Override
    public void checkOrganizationPermission(Long organizationId) {
        Long currentUserId = DetailsHelper.getUserDetails().getUserId();
        List<OrganizationDTO> organizations = baseFeignClient.listOrganizationByUserId(currentUserId).getBody();
        if (!organizations.stream().map(OrganizationDTO::getTenantId).collect(Collectors.toList()).contains(organizationId)) {
            throw new CommonException(ERROR_WORKSPACE_ILLEGAL);
        }
    }

    @Override
    public Page<WorkSpaceRecentInfoVO> recentUpdateList(Long organizationId,
                                                        Long projectId,
                                                        Long baseId,
                                                        PageRequest pageRequest) {
        KnowledgeBaseDTO knowledgeBaseDTO = new KnowledgeBaseDTO();
        knowledgeBaseDTO.setOrganizationId(organizationId);
        knowledgeBaseDTO.setProjectId(projectId);
        knowledgeBaseDTO.setId(baseId);
        knowledgeBaseDTO = knowledgeBaseMapper.selfSelect(knowledgeBaseDTO);

        if (Objects.isNull(knowledgeBaseDTO)) {
            throw new CommonException(ERROR_WORKSPACE_NOTFOUND);
        }
        Long thisProjectId = knowledgeBaseDTO.getProjectId();
        Long thisOrganizationId = knowledgeBaseDTO.getOrganizationId();
        Page<WorkSpaceRecentVO> recentPage =
                PageHelper.doPage(pageRequest, () -> workSpaceMapper.selectRecent(thisOrganizationId, thisProjectId, baseId));
        List<WorkSpaceRecentVO> recentList = recentPage.getContent();
        fillUserData(recentList, knowledgeBaseDTO);
        Map<String, List<WorkSpaceRecentVO>> group = recentList.stream().collect(Collectors.groupingBy(WorkSpaceRecentVO::getLastUpdateDateStr));
        List<WorkSpaceRecentInfoVO> list = new ArrayList<>(group.size());
        for (Map.Entry<String, List<WorkSpaceRecentVO>> entry : group.entrySet()) {
            list.add(new WorkSpaceRecentInfoVO(entry.getKey().substring(5), entry.getKey(), entry.getValue()));
        }
        List<WorkSpaceRecentInfoVO> resultList =
                list
                        .stream()
                        .sorted(Comparator.comparing(WorkSpaceRecentInfoVO::getSortDateStr).reversed())
                        .collect(Collectors.toList());
        return PageUtils.copyPropertiesAndResetContent(recentPage, resultList);
    }

    @Override
    public Map<String, Object> recycleWorkspaceTree(Long organizationId, Long projectId) {
        Map<String, Object> result = new HashMap<>(2);
        List<WorkSpaceDTO> workSpaceDTOList = workSpaceMapper.queryAllDelete(organizationId, projectId);
        Map<Long, WorkSpaceDTO> workSpaceMap = workSpaceDTOList.stream().collect(Collectors.toMap(WorkSpaceDTO::getId, Function.identity()));
        for (WorkSpaceDTO workSpace : workSpaceDTOList) {
            if (workSpaceMap.get(workSpace.getParentId()) == null) {
                workSpace.setParentId(0L);
            }
        }
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
        result.put(ROOT_ID, 0L);
        result.put(ITEMS, workSpaceTreeMap);
        return result;
    }

    @Override
    public void removeWorkSpaceByBaseId(Long organizationId, Long projectId, Long baseId) {
        List<Long> list = workSpaceMapper.listAllParentIdByBaseId(organizationId, projectId, baseId);
        if (!CollectionUtils.isEmpty(list)) {
            list.forEach(v -> removeWorkSpaceAndPage(organizationId, projectId, v, true));
        }
    }

    @Override
    public void deleteWorkSpaceByBaseId(Long organizationId, Long projectId, Long baseId) {
        List<Long> list = workSpaceMapper.listAllParentIdByBaseId(organizationId, projectId, baseId);
        if (!CollectionUtils.isEmpty(list)) {
            list.forEach(v -> deleteWorkSpaceAndPage(organizationId, projectId, v));
        }
    }

    @Override
    public void restoreWorkSpaceByBaseId(Long organizationId, Long projectId, Long baseId) {
        List<Long> list = workSpaceMapper.listAllParentIdByBaseId(organizationId, projectId, baseId);
        if (!CollectionUtils.isEmpty(list)) {
            list.forEach(v -> restoreWorkSpaceAndPage(organizationId, projectId, v, null));
        }
    }

    @Override
    public List<KnowledgeBaseTreeVO> listSystemTemplateBase(List<Long> baseIds) {
        List<WorkSpaceDTO> workSpaceDTOS = workSpaceMapper.listTemplateByBaseIds(0L, 0L, baseIds);
        if (CollectionUtils.isEmpty(workSpaceDTOS)) {
            return new ArrayList<>();
        }
        List<KnowledgeBaseTreeVO> collect = workSpaceDTOS.stream().map(v -> workSpaceAssembler.dtoToTreeVO(v)).collect(Collectors.toList());
        return collect;
    }

    private void updateWorkSpace(WorkSpaceDTO workSpaceDTO, Long organizationId, Long projectId, Long workspaceId, Long baseId) {
        //恢复到顶层
        String[] split = StringUtils.split(workSpaceDTO.getRoute(), '.');
        int index = ArrayUtils.indexOf(split, String.valueOf(workspaceId));
        String[] subarray = (String[]) ArrayUtils.subarray(split, 0, index);
        String join = StringUtils.join(subarray, ".");

        List<WorkSpaceDTO> workSpaceDTOS = workSpaceMapper.selectAllChildByRoute(workSpaceDTO.getRoute(), false);
        workSpaceDTO.setBaseId(baseId);
        workSpaceDTO.setDelete(false);
        workSpaceDTO.setParentId(0L);
        workSpaceDTO.setRoute(String.valueOf(workSpaceDTO.getId()));
        String rank = workSpaceMapper.queryMaxRank(organizationId, projectId, 0L);
        workSpaceDTO.setRank(RankUtil.genNext(rank));
        baseUpdate(workSpaceDTO);

        StringBuilder sb = new StringBuilder(join).append(".");
        if (!CollectionUtils.isEmpty(workSpaceDTOS)) {
            for (WorkSpaceDTO workSpace : workSpaceDTOS) {
                if (Boolean.TRUE.equals(workSpace.getDelete())) {
                    return;
                }
                workSpace.setBaseId(baseId);
                workSpace.setDelete(false);
                String newRoute = StringUtils.substringAfter(workSpace.getRoute(), sb.toString());
                workSpace.setRoute(newRoute);
                baseUpdate(workSpace);
            }
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkSpaceInfoVO clonePage(Long organizationId, Long projectId, Long workSpaceId, Long parentId) {
        // 复制页面内容
        WorkSpaceDTO workSpaceDTO = getWorkSpaceDTO(organizationId, projectId, workSpaceId);
        //根据类型来判断
        if (StringUtils.equalsIgnoreCase(workSpaceDTO.getType(), WorkSpaceType.FILE.getValue())) {
            //获得文件 上传文件
            return cloneFile(projectId, organizationId, workSpaceDTO, parentId);
        } else {
            return cloneDocument(projectId, organizationId, workSpaceDTO, parentId);
        }
    }

    private WorkSpaceInfoVO cloneDocument(Long projectId, Long organizationId, WorkSpaceDTO workSpaceDTO, Long parentId) {
        PageContentDTO pageContentDTO = pageContentMapper.selectLatestByWorkSpaceId(workSpaceDTO.getId());
        PageCreateVO pageCreateVO = new PageCreateVO(workSpaceDTO.getParentId(), workSpaceDTO.getName(), pageContentDTO.getContent(), workSpaceDTO.getBaseId(), workSpaceDTO.getType());
        WorkSpaceInfoVO pageWithContent = pageService.createPageWithContent(organizationId, projectId, pageCreateVO);
        // 复制页面的附件
        List<PageAttachmentDTO> pageAttachmentDTOS = pageAttachmentMapper.selectByPageId(pageContentDTO.getPageId());

        if (!CollectionUtils.isEmpty(pageAttachmentDTOS)) {
            Long userId = DetailsHelper.getUserDetails().getUserId();
            pageAttachmentDTOS.forEach(attach -> {
                attach.setId(null);
                attach.setPageId(pageWithContent.getPageInfo().getId());
                attach.setCreatedBy(userId);
                attach.setLastUpdatedBy(userId);
            });
            List<PageAttachmentDTO> attachmentDTOS = pageAttachmentService.batchInsert(pageAttachmentDTOS);
            pageWithContent.setPageAttachments(modelMapper.map(attachmentDTOS, new TypeToken<List<PageAttachmentVO>>() {
            }.getType()));
        }
        //修改父级
        WorkSpaceDTO spaceDTO = workSpaceMapper.selectByPrimaryKey(pageWithContent.getId());
        spaceDTO.setParentId(parentId);
        baseUpdate(spaceDTO);
        return pageWithContent;
    }

    private WorkSpaceInfoVO cloneFile(Long projectId, Long organizationId, WorkSpaceDTO workSpaceDTO, Long parentId) {
        InputStream inputStream = expandFileClient.downloadFile(organizationId, workSpaceDTO.getFileKey());
        String fileName = generateFileName(workSpaceDTO.getName());
        MultipartFile multipartFile = getMultipartFile(inputStream, generateFileName(workSpaceDTO.getName()));
        //创建workSpace
        FileSimpleDTO fileSimpleDTO = uploadMultipartFileWithMD5(organizationId, null, fileName, null, null, multipartFile);
        PageCreateWithoutContentVO pageCreateWithoutContentVO = new PageCreateWithoutContentVO();
        pageCreateWithoutContentVO.setTitle(fileName);
        pageCreateWithoutContentVO.setFileKey(fileSimpleDTO.getFileKey());
        pageCreateWithoutContentVO.setBaseId(workSpaceDTO.getBaseId());
        pageCreateWithoutContentVO.setType(WorkSpaceType.FILE.getValue());
        pageCreateWithoutContentVO.setParentWorkspaceId(parentId);

        WorkSpaceInfoVO upload = upload(projectId, organizationId, pageCreateWithoutContentVO);
        //修改父级
        WorkSpaceDTO spaceDTO = workSpaceMapper.selectByPrimaryKey(upload.getId());
        spaceDTO.setParentId(parentId);
        baseUpdate(spaceDTO);
        return upload;

    }

    private String generateFileName(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new CommonException("error.file.name.is.null");
        }
        return CommonUtil.getFileNameWithoutSuffix(name) + "-副本" + BaseConstants.Symbol.POINT + CommonUtil.getFileTypeByFileName(name);
    }

    private WorkSpaceDTO getWorkSpaceDTO(Long organizationId, Long projectId, Long workSpaceId) {
        WorkSpaceDTO workSpaceDTO = new WorkSpaceDTO();
        workSpaceDTO.setProjectId(projectId);
        workSpaceDTO.setOrganizationId(organizationId);
        workSpaceDTO.setId(workSpaceId);
        workSpaceDTO = workSpaceMapper.selectOne(workSpaceDTO);
        if (Objects.isNull(workSpaceDTO)) {
            throw new CommonException(ERROR_WORKSPACE_NOTFOUND);
        }
        return workSpaceDTO;
    }

    public MultipartFile getMultipartFile(InputStream inputStream, String fileName) {
        FileItem fileItem = createFileItem(inputStream, fileName);
        //CommonsMultipartFile是feign对multipartFile的封装，但是要FileItem类对象
        return new CommonsMultipartFile(fileItem);
    }

    public FileItem createFileItem(InputStream inputStream, String fileName) {
        FileItemFactory factory = new DiskFileItemFactory(16, null);
        String textFieldName = "file";
        FileItem item = factory.createItem(textFieldName, MediaType.MULTIPART_FORM_DATA_VALUE, true, fileName);
        int bytesRead = 0;
        byte[] buffer = new byte[8192];
        OutputStream os = null;
        //使用输出流输出输入流的字节
        try {
            os = item.getOutputStream();
            while ((bytesRead = inputStream.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            inputStream.close();
        } catch (IOException e) {
            LOGGER.error("Stream copy exception", e);
            throw new IllegalArgumentException("文件上传失败");
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    LOGGER.error("Stream close exception", e);

                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.error("Stream close exception", e);
                }
            }
        }

        return item;
    }

    @Override
    public Boolean checkTemplate(Long organizationId, Long projectId, WorkSpaceDTO workSpaceDTO) {
        Boolean isTemplate = false;
        if (organizationId == 0L || (projectId != null && projectId == 0L)) {
            if (organizationId.equals(workSpaceDTO.getOrganizationId()) && projectId.equals(workSpaceDTO.getProjectId())) {
                isTemplate = true;
            } else {
                throw new CommonException(ERROR_WORKSPACE_ILLEGAL);
            }
        }
        return isTemplate;
    }


    @Override
    public List<WorkSpaceVO> listAllSpace(Long organizationId, Long projectId) {
        List<KnowledgeBaseDTO> knowledgeBaseDTOS = knowledgeBaseMapper.listKnowleadgeBase(organizationId, projectId);
        if (CollectionUtils.isEmpty(knowledgeBaseDTOS)) {
            return new ArrayList<>();
        }
        List<WorkSpaceVO> list = new ArrayList<>();
        knowledgeBaseDTOS.forEach(v -> {
            WorkSpaceVO workSpaceVO = new WorkSpaceVO(v.getId(), v.getName(), null, null, null);
            workSpaceVO.setChildren(listAllSpaceByOptions(organizationId, projectId, v.getId()));
            list.add(workSpaceVO);
        });
        return list;
    }

    private List<WorkSpaceVO> listAllSpaceByOptions(Long organizationId, Long projectId, Long baseId) {
        List<WorkSpaceVO> result = new ArrayList<>();
        List<WorkSpaceDTO> workSpaceDTOList = workSpaceMapper.queryAll(organizationId, projectId, baseId, null, null);
        if (EncryptContext.isEncrypt()) {
            workSpaceDTOList.forEach(w -> {
                String route = w.getRoute();
                route = Optional.ofNullable(StringUtils.split(route, BaseConstants.Symbol.POINT))
                        .map(list -> Stream.of(list)
                                .map(str -> encryptionService.encrypt(str, ""))
                                .collect(Collectors.joining(BaseConstants.Symbol.POINT)))
                        .orElse(null);
                w.setRoute(route);
            });
        }
        Map<Long, List<WorkSpaceVO>> groupMap = workSpaceDTOList.stream().collect(Collectors.
                groupingBy(WorkSpaceDTO::getParentId, Collectors.mapping(item -> {
                    WorkSpaceVO workSpaceVO = new WorkSpaceVO(item.getId(), item.getName(), item.getRoute(), item.getType(), CommonUtil.getFileType(item.getFileKey()));
                    return workSpaceVO;
                }, Collectors.toList())));
        for (WorkSpaceDTO workSpaceDTO : workSpaceDTOList) {
            if (Objects.equals(workSpaceDTO.getParentId(), 0L)) {
                WorkSpaceVO workSpaceVO = new WorkSpaceVO(workSpaceDTO.getId(), workSpaceDTO.getName(), workSpaceDTO.getRoute(), workSpaceDTO.getType(), CommonUtil.getFileType(workSpaceDTO.getFileKey()));
                workSpaceVO.setChildren(groupMap.get(workSpaceDTO.getId()));
                dfs(workSpaceVO, groupMap);
                result.add(workSpaceVO);
            }
        }
        return result;
    }

    @Override
    public Page<WorkBenchRecentVO> selectProjectRecentList(PageRequest pageRequest, Long organizationId, Long projectId, boolean selfFlag) {
        Assert.notNull(organizationId, BaseConstants.ErrorCode.DATA_INVALID);
        Long userId = DetailsHelper.getUserDetails().getUserId();
        List<ProjectDTO> projectList;
        if (Objects.nonNull(projectId)) {
            projectList = baseFeignClient.queryProjectByIds(Collections.singleton(projectId)).getBody();
        } else {
            //这里考虑到汉得信息下项目众多，并且这里只用到了项目的id name 和imageUrl，提供轻量级的查询
            projectList = baseFeignClient.queryOrgProjectsOptional(organizationId, userId).getBody();
        }
        OrganizationDTO organization = Optional.ofNullable(baseFeignClient.query(organizationId).getBody()).orElse(new OrganizationDTO());
        if (CollectionUtils.isEmpty(projectList)) {
            return new Page<>();
        }
        // 检查组织级权限
        boolean failed = true;
        String body = baseFeignClient.orgLevel(organizationId).getBody();
        if (StringUtils.contains(body, "administrator")) {
            failed = false;
        }
        boolean finalFailed = failed;
        Page<WorkBenchRecentVO> recentList = PageHelper.doPageAndSort(pageRequest,
                () -> workSpaceMapper.selectProjectRecentList(organizationId,
                        projectList.stream().map(ProjectDTO::getId).collect(Collectors.toList()), userId, selfFlag, finalFailed));
        if (CollectionUtils.isEmpty(recentList)) {
            return recentList;
        }
        // 取一个月内的更新人
        List<Long> pageIdList = recentList.stream().map(WorkBenchRecentVO::getPageId).collect(Collectors.toList());
        List<PageLogDTO> pageLogList = pageLogMapper.selectByPageIdList(pageIdList, DateUtils.truncate(DateUtils.addDays(new Date(), -30), Calendar.DAY_OF_MONTH));
        Map<Long, List<PageLogDTO>> pageMap =
                pageLogList.stream().collect(Collectors.groupingBy(PageLogDTO::getPageId));
        // 获取用户信息
        Map<Long, UserDO> map = baseFeignClient.listUsersByIds(pageLogList.stream()
                .map(PageLogDTO::getCreatedBy).toArray(Long[]::new), false).getBody()
                .stream().collect(Collectors.toMap(UserDO::getId, x -> x));
        // 获取项目logo
        Map<Long, ProjectDTO> projectMap = projectList.stream().collect(Collectors.toMap(ProjectDTO::getId, a -> a));
        List<PageLogDTO> temp;
        for (WorkBenchRecentVO recent : recentList) {
            temp = sortAndDistinct(pageMap.get(recent.getPageId()), Comparator.comparing(PageLogDTO::getCreationDate).reversed());
            if (temp.size() > 3) {
                recent.setOtherUserCount(temp.size() - 3);
                temp = temp.subList(0, 2);
            }
            recent.setUpdatedUserList(temp.stream().map(pageLog -> map.get(pageLog.getCreatedBy())).collect(Collectors.toList()));
            if (Objects.isNull(recent.getProjectId())) {
                recent.setOrgFlag(true);
            }
            recent.setImageUrl(projectMap.getOrDefault(recent.getProjectId(), new ProjectDTO()).getImageUrl());
            recent.setProjectName(projectMap.getOrDefault(recent.getProjectId(), new ProjectDTO()).getName());
            recent.setOrganizationName(organization.getTenantName());
        }
        return recentList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkSpaceInfoVO upload(Long projectId, Long organizationId, PageCreateWithoutContentVO createVO) {
        createVO.setTitle(CommonUtil.getFileName(createVO.getFileKey()));
        PageDTO page = pageService.createPage(organizationId, projectId, createVO);
        WorkSpaceDTO workSpaceDTO = initWorkSpaceDTO(projectId, organizationId, createVO);
        //获取父空间id和route
        Long parentId = createVO.getParentWorkspaceId();
        String route = "";
        if (parentId != null && !parentId.equals(0L)) {
            WorkSpaceDTO parentWorkSpace = this.baseQueryById(organizationId, projectId, parentId);
            route = parentWorkSpace.getRoute();
        }
        //设置rank值
        if (Boolean.TRUE.equals(workSpaceMapper.hasChildWorkSpace(organizationId, projectId, parentId))) {
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
    public Page<WorkSpaceInfoVO> queryFolder(Long projectId, Long organizationId, Long workSpaceId, PageRequest pageRequest) {
        WorkSpaceDTO workSpaceDTO = workSpaceMapper.selectByPrimaryKey(workSpaceId);
        if (workSpaceDTO == null || !StringUtils.equalsIgnoreCase(workSpaceDTO.getType(), WorkSpaceType.FOLDER.getValue())) {
            return new Page<>();
        }
        if(!Objects.equals(projectId, workSpaceDTO.getProjectId())) {
            // 这种情况说明是项目层查询共享知识库的对象
            // 鉴权
            Assert.isTrue(
                    this.knowledgeBaseService.checkOpenRangeCanAccess(organizationId, workSpaceDTO.getBaseId()),
                    BaseConstants.ErrorCode.FORBIDDEN
            );
            // 通过了鉴权的, 需要将分页查询的projectId置为workSpaceDTO的projectId才能查到数据
            projectId = workSpaceDTO.getProjectId();
        }
        final Long finalProjectId = projectId;
        //查询该工作空间的直接子项
        Page<WorkSpaceDTO> workSpaceDTOPage = PageHelper.doPageAndSort(pageRequest, () -> workSpaceMapper.queryWorkSpaceById(organizationId, finalProjectId, workSpaceDTO.getId()));
        if (workSpaceDTOPage == null || org.springframework.util.CollectionUtils.isEmpty(workSpaceDTOPage.getContent())) {
            return new Page<>();
        }
        Page<WorkSpaceInfoVO> workSpaceInfoVOS = ConvertUtils.convertPage(workSpaceDTOPage, WorkSpaceInfoVO.class);
        Map<String, FileVO> longFileVOMap = getStringFileVOMap(organizationId, workSpaceInfoVOS);
        //填充用户信息
        Map<Long, UserDO> userDOMap = getLongUserDOMap(workSpaceInfoVOS);
        Map<String, FileVO> finalLongFileVOMap = longFileVOMap;
        workSpaceInfoVOS.getContent().forEach(workSpaceInfoVO -> {
            fillAttribute(userDOMap, finalLongFileVOMap, workSpaceInfoVO);
        });
        return workSpaceInfoVOS;
    }

    private void fillAttribute(Map<Long, UserDO> userDOMap, Map<String, FileVO> finalLongFileVOMap, WorkSpaceInfoVO workSpaceInfoVO) {
        UserDO userDO = userDOMap.get(workSpaceInfoVO.getCreatedBy());
        UserDO updateUser = userDOMap.get(workSpaceInfoVO.getLastUpdatedBy());
        workSpaceInfoVO.setCreateUser(userDO);
        workSpaceInfoVO.setLastUpdatedUser(updateUser);
        //填充属性
        switch (WorkSpaceType.valueOf(workSpaceInfoVO.getType().toUpperCase())) {
            case FILE:
                //文件计算大小
                fillFileSize(finalLongFileVOMap, workSpaceInfoVO);
                break;
            case DOCUMENT:
            case FOLDER:
                //计算子项  这里也只管直接子项
                //查询该工作空间的直接子项
                fillDocsAndFolders(workSpaceInfoVO);
                break;
            default:
                throw new CommonException("Unsupported knowledge space type");
        }
    }

    private void fillDocsAndFolders(WorkSpaceInfoVO workSpaceInfoVO) {
        WorkSpaceDTO workSpaceDTO = new WorkSpaceDTO();
        workSpaceDTO.setParentId(workSpaceInfoVO.getId());
        List<WorkSpaceDTO> spaceDTOS = workSpaceMapper.select(workSpaceDTO);
        if (CollectionUtils.isEmpty(spaceDTOS)) {
            workSpaceInfoVO.setSubFiles(0L);
        } else {
            List<WorkSpaceDTO> files = spaceDTOS.stream().filter(spaceDTO1 -> StringUtils.equalsIgnoreCase(spaceDTO1.getType(), WorkSpaceType.FILE.getValue())).collect(Collectors.toList());
            List<WorkSpaceDTO> documents = spaceDTOS.stream().filter(spaceDTO1 -> StringUtils.equalsIgnoreCase(spaceDTO1.getType(), WorkSpaceType.DOCUMENT.getValue())).collect(Collectors.toList());
            List<WorkSpaceDTO> folders = spaceDTOS.stream().filter(spaceDTO1 -> StringUtils.equalsIgnoreCase(spaceDTO1.getType(), WorkSpaceType.FOLDER.getValue())).collect(Collectors.toList());
            workSpaceInfoVO.setSubFiles(Long.valueOf(files.size()));
            workSpaceInfoVO.setSubDocuments(Long.valueOf(documents.size()));
            workSpaceInfoVO.setSubFolders(Long.valueOf(folders.size()));
        }
    }

    private void fillFileSize(Map<String, FileVO> finalLongFileVOMap, WorkSpaceInfoVO workSpaceInfoVO) {
        if (finalLongFileVOMap != null) {
            FileVO fileVO = finalLongFileVOMap.get(workSpaceInfoVO.getFileKey());
            if (fileVO != null) {
                workSpaceInfoVO.setFileSize(fileVO.getFileSize());
            }
        } else {
            workSpaceInfoVO.setFileSize(0L);
        }
    }

    private Map<String, FileVO> getStringFileVOMap(Long organizationId, Page<WorkSpaceInfoVO> workSpaceInfoVOS) {
        List<WorkSpaceInfoVO> fileList = workSpaceInfoVOS.getContent().stream().filter(workSpaceInfoVO -> StringUtils.equalsIgnoreCase(workSpaceInfoVO.getType(), WorkSpaceType.FILE.getValue())).collect(Collectors.toList());
        Map<String, FileVO> longFileVOMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(fileList)) {
            List<String> fileKeys = fileList.stream().map(WorkSpaceInfoVO::getFileKey).collect(Collectors.toList());
            List<FileVO> fileVOS = expandFileClient.queryFileDTOByFileKeys(organizationId, fileKeys);
            longFileVOMap = fileVOS.stream().collect(Collectors.toMap(FileVO::getFileKey, Function.identity()));
        }
        return longFileVOMap;
    }

    private Map<Long, UserDO> getLongUserDOMap(Page<WorkSpaceInfoVO> workSpaceInfoVOS) {
        Set<Long> userIds = workSpaceInfoVOS.getContent().stream().map(WorkSpaceInfoVO::getCreatedBy).collect(Collectors.toSet());
        Set<Long> updateIds = workSpaceInfoVOS.getContent().stream().map(WorkSpaceInfoVO::getLastUpdatedBy).collect(Collectors.toSet());
        userIds.addAll(updateIds);
        Long[] ids = new Long[userIds.size()];
        userIds.toArray(ids);
        ResponseEntity<List<UserDO>> listResponseEntity = baseFeignService.listUsersByIds(ids, null);
        List<UserDO> body = listResponseEntity.getBody();
        return body.stream().collect(Collectors.toMap(UserDO::getId, Function.identity()));
    }

    @Override
    public FileSimpleDTO uploadMultipartFileWithMD5(Long organizationId, String directory, String fileName, Integer docType, String storageCode, MultipartFile multipartFile) {
        checkFileType(multipartFile);
        if (StringUtils.isBlank(fileName)) {
            fileName = multipartFile.getOriginalFilename();
        }
        String url = expandFileClient.uploadFile(organizationId, BaseStage.BACKETNAME, null, fileName, multipartFile);
        String fileKey = CommonUtil.getFileKeyByUrl(url);
        FileSimpleDTO fileSimpleDTO = new FileSimpleDTO();
        fileSimpleDTO.setFileTokenUrl(url);
        fileSimpleDTO.setFileKey(fileKey);
        return fileSimpleDTO;
    }

    private void checkFileType(MultipartFile multipartFile) {
        String originalFilename = multipartFile.getOriginalFilename();
        if (StringUtils.isEmpty(originalFilename) || !FileFormatType.ONLY_FILE_FORMATS.contains(CommonUtil.getFileTypeByFileName(originalFilename).toUpperCase())) {
            throw new CommonException("error.not.supported.file.upload");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void renameWorkSpace(Long projectId, Long organizationId, Long id, String newName) {
        WorkSpaceDTO spaceDTO = workSpaceMapper.selectByPrimaryKey(id);
        if (spaceDTO == null) {
            return;
        }
        if (StringUtils.equalsIgnoreCase(spaceDTO.getType(), WorkSpaceType.FILE.getValue())) {
            String fileType = CommonUtil.getFileType(spaceDTO.getFileKey());
            spaceDTO.setName(newName + "." + fileType);
        } else {
            spaceDTO.setName(newName);
        }
        if (
                StringUtils.equalsIgnoreCase(spaceDTO.getType(), WorkSpaceType.FILE.getValue())
                        || StringUtils.equalsIgnoreCase(spaceDTO.getType(), WorkSpaceType.DOCUMENT.getValue())
        ) {
            //同步修改page表
            updatePageTitle(spaceDTO);
        }

        this.baseUpdate(spaceDTO);
    }

    /**
     * 排序并按照人名去重
     *
     * @param pageLogList pageLogList
     * @param c           排序方法
     * @return 顺序pageLogList
     */
    private List<PageLogDTO> sortAndDistinct(List<PageLogDTO> pageLogList, Comparator<PageLogDTO> c) {
        // 集合不为空并且大于1才需要去重
        if (CollectionUtils.isNotEmpty(pageLogList) && pageLogList.size() > 1) {
            pageLogList.sort(c);
            // 创建人相邻去重
            for (int fast = 1; fast < pageLogList.size(); fast++) {
                if (Objects.equals(pageLogList.get(fast - 1).getCreatedBy(), pageLogList.get(fast).getCreatedBy())) {
                    pageLogList.remove(fast--);
                }
            }
        }
        return Optional.ofNullable(pageLogList).orElse(Collections.emptyList());
    }


    private Boolean isParentDelete(WorkSpaceDTO workSpaceDTO, Long workspaceId, Long projectId) {
        //判断父级是否有被删除
        Boolean isParentDelete = false;
        String[] parents = workSpaceDTO.getRoute().split("\\.");
        List<String> list = Arrays.asList(parents);
        List<Long> parentIds = list.stream().map(e -> Long.parseLong(e)).collect(Collectors.toList());
        List<WorkSpaceDTO> workSpaceDTOS = workSpaceMapper.selectSpaceByIds(projectId, parentIds);
        for (WorkSpaceDTO parent : workSpaceDTOS) {
            if (parent != null) {
                Boolean res = parent.getId() != null && !parent.getId().equals(workspaceId) && Boolean.TRUE.equals(parent.getDelete());
                if (Boolean.TRUE.equals(res)) {
                    isParentDelete = parent.getDelete();
                    break;
                }
            }
        }
        return isParentDelete;
    }

    private WorkSpaceDTO initWorkSpaceDTO(Long projectId, Long organizationId, PageCreateWithoutContentVO createVO) {
        WorkSpaceDTO workSpaceDTO = new WorkSpaceDTO();
        workSpaceDTO.setOrganizationId(organizationId);
        workSpaceDTO.setProjectId(projectId);
        workSpaceDTO.setName(CommonUtil.getFileName(createVO.getFileKey()));
        workSpaceDTO.setBaseId(createVO.getBaseId());
        workSpaceDTO.setDescription(createVO.getDescription());
        workSpaceDTO.setFileKey(createVO.getFileKey());
        workSpaceDTO.setType(createVO.getType());
        return workSpaceDTO;
    }

    private Map<Long, UserDO> fillUserDOMap(List<WorkSpaceDTO> workSpaceDTOList) {
        Map<Long, UserDO> userDOMap;
        Set<Long> createUserIds = workSpaceDTOList.stream().filter(spaceDTO -> StringUtils.equalsIgnoreCase(spaceDTO.getType(), WorkSpaceType.FILE.getValue())).map(WorkSpaceDTO::getCreatedBy).collect(Collectors.toSet());
        Set<Long> updateUserIds = workSpaceDTOList.stream().filter(spaceDTO -> StringUtils.equalsIgnoreCase(spaceDTO.getType(), WorkSpaceType.FILE.getValue())).map(WorkSpaceDTO::getLastUpdatedBy).collect(Collectors.toSet());
        createUserIds.addAll(updateUserIds);
        Long[] ids = new Long[createUserIds.size()];
        createUserIds.toArray(ids);
        ResponseEntity<List<UserDO>> listResponseEntity = baseFeignService.listUsersByIds(ids, null);
        List<UserDO> body = listResponseEntity.getBody();
        userDOMap = body.stream().collect(Collectors.toMap(UserDO::getId, Function.identity()));
        return userDOMap;
    }

    private void fillWorkSpaceAttribute(Long organizationId, List<WorkSpaceDTO> workSpaceDTOList, Map<Long, WorkSpaceTreeVO> workSpaceTreeMap, Map<Long, List<Long>> groupMap) {
        Map<String, FileVO> fileVOMap = new HashMap<>();
        Map<Long, UserDO> userDOMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(workSpaceDTOList)) {
            fileVOMap = fillFileVOMap(organizationId, workSpaceDTOList, fileVOMap);
            userDOMap = fillUserDOMap(workSpaceDTOList);
        }
        for (WorkSpaceDTO workSpaceDTO : workSpaceDTOList) {
            WorkSpaceTreeVO treeVO = buildTreeVO(workSpaceDTO, groupMap.get(workSpaceDTO.getId()));
            workSpaceTreeMap.put(workSpaceDTO.getId(), treeVO);
            //封装onlyOffice预览所需要的数据
            if (StringUtils.equalsIgnoreCase(treeVO.getType(), WorkSpaceType.FILE.getValue())) {
                fillFileInfo(fileVOMap, userDOMap, workSpaceDTO, treeVO);
            }
        }
    }

    private Map<String, FileVO> fillFileVOMap(Long organizationId, List<WorkSpaceDTO> workSpaceDTOList, Map<String, FileVO> fileVOMap) {
        List<String> fileKeys = workSpaceDTOList.stream().filter(spaceDTO -> StringUtils.equalsIgnoreCase(spaceDTO.getType(), WorkSpaceType.FILE.getValue())).map(WorkSpaceDTO::getFileKey).collect(Collectors.toList());
        List<FileVO> fileVOS = expandFileClient.queryFileDTOByFileKeys(organizationId, fileKeys);
        if (!CollectionUtils.isEmpty(fileVOS)) {
            fileVOMap = fileVOS.stream().collect(Collectors.toMap(FileVO::getFileKey, Function.identity()));
        }
        return fileVOMap;
    }

    private void fillFileInfo(Map<String, FileVO> fileVOMap, Map<Long, UserDO> userDOMap, WorkSpaceDTO workSpaceDTO, WorkSpaceTreeVO treeVO) {
        FileVO fileVO = fileVOMap.getOrDefault(workSpaceDTO.getFileKey(), new FileVO());
        treeVO.setKey(CommonUtil.getFileId(fileVO.getFileKey()));
        treeVO.setTitle(fileVO.getFileName());
        treeVO.setUrl(fileVO.getFileUrl());
        treeVO.setFileType(CommonUtil.getFileType(fileVO.getFileKey()));

        treeVO.setCreatedUser(userDOMap.get(workSpaceDTO.getCreatedBy()));
        treeVO.setLastUpdatedUser(userDOMap.get(workSpaceDTO.getLastUpdatedBy()));
        treeVO.setCreationDate(workSpaceDTO.getCreationDate());
        treeVO.setLastUpdateDate(workSpaceDTO.getLastUpdateDate());
    }

    private void updatePageTitle(WorkSpaceDTO spaceDTO) {
        WorkSpacePageDTO spacePageDTO = new WorkSpacePageDTO();
        spacePageDTO.setWorkspaceId(spaceDTO.getId());
        WorkSpacePageDTO workSpacePageDTO = workSpacePageMapper.selectOne(spacePageDTO);
        if (workSpacePageDTO != null) {
            final PageDTO page = this.pageRepository.selectByPrimaryKey(workSpacePageDTO.getPageId());
            if(page != null) {
                page.setTitle(spaceDTO.getName());
                this.pageRepository.updatePageTitle(page);
            }
        }
    }

}
