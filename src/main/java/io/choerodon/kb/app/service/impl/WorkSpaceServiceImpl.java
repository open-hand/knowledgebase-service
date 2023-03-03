package io.choerodon.kb.app.service.impl;

import static io.choerodon.kb.infra.enums.PermissionConstants.PermissionTargetBaseType.*;
import static org.hzero.core.base.BaseConstants.ErrorCode.FORBIDDEN;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.api.vo.*;
import io.choerodon.kb.api.vo.permission.PermissionCheckVO;
import io.choerodon.kb.api.vo.permission.RoleVO;
import io.choerodon.kb.api.vo.permission.UserInfoVO;
import io.choerodon.kb.api.vo.permission.WorkBenchUserInfoVO;
import io.choerodon.kb.app.service.*;
import io.choerodon.kb.domain.repository.*;
import io.choerodon.kb.domain.service.IWorkSpaceService;
import io.choerodon.kb.domain.service.PermissionCheckDomainService;
import io.choerodon.kb.domain.service.PermissionRangeKnowledgeObjectSettingService;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.dto.*;
import io.choerodon.kb.infra.enums.*;
import io.choerodon.kb.infra.enums.PermissionConstants.ActionPermission;
import io.choerodon.kb.infra.enums.PermissionConstants.PermissionTargetBaseType;
import io.choerodon.kb.infra.feign.vo.FileVO;
import io.choerodon.kb.infra.feign.vo.OrganizationDTO;
import io.choerodon.kb.infra.feign.vo.UserDO;
import io.choerodon.kb.infra.mapper.*;
import io.choerodon.kb.infra.utils.*;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import org.hzero.boot.file.dto.FileSimpleDTO;
import org.hzero.boot.file.feign.FileRemoteService;
import org.hzero.core.base.AopProxy;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.util.ResponseUtils;
import org.hzero.core.util.UUIDUtils;

/**
 * @author shinan.chen
 * @since 2019/7/17
 */
@Service
public class WorkSpaceServiceImpl implements WorkSpaceService, AopProxy<WorkSpaceServiceImpl> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkSpaceServiceImpl.class);

    private static final int LENGTH_LIMIT = 40;

    private final ObjectMapper mapper = new ObjectMapper();

    private static final String ERROR_GET_FILE_BY_KEY = "error.get.file.by.key";

    private static final String ERROR_GET_FILE_BY_URL = "error.get.file.by.url";


    @Value("${choerodon.file-server.upload.type-limit}")
    private String fileServerUploadTypeLimit;

    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private PageCommentRepository pageCommentRepository;
    @Autowired
    private PageAttachmentRepository pageAttachmentRepository;
    @Autowired
    private WorkSpacePageService workSpacePageService;
    @Autowired
    private PageVersionService pageVersionService;
    @Autowired
    private PageLogService pageLogService;
    @Autowired
    private PageAttachmentService pageAttachmentService;
    @Autowired
    private WorkSpaceShareService workSpaceShareService;
    @Autowired
    private PageService pageService;
    @Autowired
    private WorkSpaceMapper workSpaceMapper;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PageAttachmentMapper pageAttachmentMapper;
    @Autowired
    private PageContentMapper pageContentMapper;
    @Autowired
    private PageVersionMapper pageVersionMapper;
    @Autowired
    private AgileRemoteRepository agileRemoteRepository;
    @Autowired
    private PageLogMapper pageLogMapper;
    @Autowired
    private IamRemoteRepository iamRemoteRepository;
    @Autowired
    private ExpandFileClient expandFileClient;
    @Autowired
    private WorkSpacePageMapper workSpacePageMapper;
    @Autowired
    private FilePathHelper filePathService;
    @Autowired
    protected TransactionalProducer transactionalProducer;
    @Autowired
    private FileRemoteService fileRemoteService;

    @Autowired
    private WorkSpaceRepository workSpaceRepository;
    @Autowired
    private WorkSpacePageRepository workSpacePageRepository;

    @Autowired
    private PermissionRangeKnowledgeObjectSettingService permissionRangeKnowledgeObjectSettingService;
    @Autowired
    private PermissionRangeKnowledgeObjectSettingRepository permissionRangeKnowledgeObjectSettingRepository;
    @Autowired
    private PermissionAggregationService permissionAggregationService;
    @Autowired
    private PermissionCheckDomainService permissionCheckDomainService;
    @Autowired
    private List<IWorkSpaceService> iWorkSpaceServices;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkSpaceInfoVO createWorkSpaceAndPage(Long organizationId,
                                                  Long projectId,
                                                  PageCreateWithoutContentVO createVO,
                                                  boolean initFlag) {
        //创建workspace的类型分成了三种  一种是文档，一种是文件，一种是文件夹
        WorkSpaceDTO workSpaceDTO;
        PermissionTargetBaseType permissionTargetBaseType;
        switch (WorkSpaceType.valueOf(createVO.getType().toUpperCase())) {
            case FOLDER:
                //校验文件夹名称的长度
                checkFolderNameLength(createVO.getTitle());
                permissionTargetBaseType = PermissionTargetBaseType.FOLDER;
                workSpaceDTO = createWorkSpace(organizationId, projectId, createVO, ActionPermission.FOLDER_CREATE, initFlag);
                break;
            case DOCUMENT:
                permissionTargetBaseType = PermissionTargetBaseType.FILE;
                workSpaceDTO = createWorkSpace(organizationId, projectId, createVO, ActionPermission.DOCUMENT_CREATE, initFlag);
                //创建页面，空间和页面的关联关系
                PageDTO page = pageService.createPage(organizationId, projectId, createVO);
                workSpacePageRepository.baseCreate(page.getId(), workSpaceDTO.getId());
                // 刷新es
                pageRepository.createOrUpdateEs(page.getId());
                break;
            default:
                throw new CommonException("Unsupported knowledge space type");
        }
        //返回workSpaceInfo
        WorkSpaceInfoVO workSpaceInfoVO = this.workSpaceRepository.queryWorkSpaceInfo(organizationId, projectId, workSpaceDTO.getId(), null, false, createVO.getTemplateFlag());
        workSpaceInfoVO.setWorkSpace(WorkSpaceTreeNodeVO.of(workSpaceDTO, Collections.emptyList()));
        // 初始化权限
        permissionAggregationService.autoGeneratePermission(organizationId, projectId, permissionTargetBaseType, workSpaceInfoVO.getWorkSpace());
        // 填充权限信息
        if (!createTemplate(createVO)){
            workSpaceInfoVO.setPermissionCheckInfos(permissionInfos(projectId, organizationId, workSpaceDTO));
        }
        return workSpaceInfoVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkSpaceInfoVO updateWorkSpaceAndPage(Long organizationId, Long projectId,
                                                  Long workSpaceId, String searchStr,
                                                  PageUpdateVO pageUpdateVO, boolean checkPermission,
                                                  boolean templateFlag) {
        WorkSpaceDTO workSpaceDTO = this.workSpaceRepository.baseQueryById(organizationId, projectId, workSpaceId);
        // 文档编辑权限校验
        if (checkPermission) {
            Map<WorkSpaceType, IWorkSpaceService> spaceServiceMap = iWorkSpaceServices.stream()
                    .collect(Collectors.toMap(IWorkSpaceService::handleSpaceType, Function.identity()));
            spaceServiceMap.get(WorkSpaceType.of(workSpaceDTO.getType())).update(workSpaceDTO);
        }
        Boolean isTemplate = this.workSpaceRepository.checkIsTemplate(organizationId, projectId, workSpaceDTO);
        WorkSpacePageDTO workSpacePageDTO = workSpacePageService.selectByWorkSpaceId(workSpaceId);
        if (ReferenceType.SELF.equals(workSpacePageDTO.getReferenceType())) {
            PageDTO pageDTO = pageRepository.selectById(workSpacePageDTO.getPageId());
            pageDTO.setObjectVersionNumber(pageUpdateVO.getObjectVersionNumber());
            PageContentDTO pageContent = pageContentMapper.selectLatestByPageId(workSpacePageDTO.getPageId());
            if (pageUpdateVO.getTitle() != null) {
                //更新标题
                pageDTO.setTitle(pageUpdateVO.getTitle());
                workSpaceDTO.setName(pageUpdateVO.getTitle());
                workSpaceRepository.baseUpdate(workSpaceDTO);
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
                workSpaceRepository.baseUpdate(workSpace);
            }
            pageRepository.baseUpdate(pageDTO, true);
        }
        return this.workSpaceRepository.queryWorkSpaceInfo(organizationId, projectId, workSpaceId, searchStr, false, templateFlag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveToRecycle(Long organizationId, Long projectId, Long workspaceId, Boolean isAdmin, boolean checkPermission,
                              boolean templateFlag) {
        //目前删除workSpace前端全部走的remove这个接口， 删除文档的逻辑  组织管理员可以删除组织下所有的，组织成员只能删除自己创建的
        WorkSpaceDTO workSpaceDTO = this.workSpaceRepository.baseQueryById(organizationId, projectId, workspaceId);
//        Map<WorkSpaceType, IWorkSpaceService> spaceServiceMap = iWorkSpaceServices.stream()
//                .collect(Collectors.toMap(IWorkSpaceService::handleSpaceType, Function.identity()));
//        spaceServiceMap.get(WorkSpaceType.of(workSpaceDTO.getType())).moveToRecycle(workSpaceDTO, newName);
        WorkSpacePageDTO workSpacePageDTO;
        switch (WorkSpaceType.of(workSpaceDTO.getType())) {
            case FOLDER:
                if (checkPermission) {
                    // 鉴权
                    Assert.isTrue(permissionCheckDomainService.checkPermission(organizationId,
                            projectId,
                            PermissionTargetBaseType.FOLDER.toString(),
                            null,
                            workspaceId,
                            ActionPermission.FOLDER_DELETE.getCode()), FORBIDDEN);
                }
                // 未删除的子集全部移至回收站, 并同file类型将自己移至回收站
                List<WorkSpaceDTO> childWorkSpaces = workSpaceMapper.selectAllChildByRoute(workSpaceDTO.getRoute(), true);
                self().batchMoveToRecycle(childWorkSpaces);
                break;
            case DOCUMENT:
                if (checkPermission) {
                    // 鉴权
                    Assert.isTrue(permissionCheckDomainService.checkPermission(organizationId,
                            projectId,
                            PermissionTargetBaseType.FILE.toString(),
                            null,
                            workspaceId,
                            ActionPermission.DOCUMENT_DELETE.getCode()), FORBIDDEN);
                }
                // 未删除的子集全部移至回收站, 并同file类型将自己移至回收站
                childWorkSpaces = workSpaceMapper.selectAllChildByRoute(workSpaceDTO.getRoute(), true);
                self().batchMoveToRecycle(childWorkSpaces);
                break;
            case FILE:
                // 鉴权
                if (checkPermission) {
                    Assert.isTrue(permissionCheckDomainService.checkPermission(organizationId,
                            projectId,
                            PermissionTargetBaseType.FILE.toString(),
                            null,
                            workspaceId,
                            ActionPermission.FILE_DELETE.getCode()), FORBIDDEN);
                }
                workSpacePageDTO = new WorkSpacePageDTO();
                workSpacePageDTO.setWorkspaceId(workspaceId);
                workSpacePageDTO = workSpacePageMapper.selectOne(workSpacePageDTO);
                checkRemovePermission(organizationId, projectId, workSpacePageDTO, isAdmin);
                break;
            default:
                throw new CommonException("Unsupported knowledge space type");
        }
        workSpaceDTO.setDelete(true);
        workSpaceRepository.baseUpdate(workSpaceDTO);
        // 删除文档后同步删除es
        workSpacePageService.deleteEs(workspaceId);
        //删除agile关联的workspace
        agileRemoteRepository.deleteByWorkSpaceId(projectId == null ? organizationId : projectId, workspaceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWorkSpaceAndPage(Long organizationId, Long projectId, Long workspaceId) {
        WorkSpaceDTO workSpaceDTO = this.workSpaceRepository.baseQueryById(organizationId, projectId, workspaceId);

        // FIXME 由于模板的存储结构有大问题, 这里暂时跳过对模板增删改操作的鉴权
        // 2022-10-27 pei.chen@zknow.com gaokuo.dai@zknow.com

        final boolean isTemplate = this.workSpaceRepository.checkIsTemplate(workSpaceDTO.getOrganizationId(), workSpaceDTO.getProjectId(), workSpaceDTO);

        switch (WorkSpaceType.valueOf(workSpaceDTO.getType().toUpperCase())) {
            case FILE:
                if (!isTemplate) {
                    Assert.isTrue(permissionCheckDomainService.checkPermission(workSpaceDTO.getOrganizationId(),
                            workSpaceDTO.getProjectId(),
                            FILE.toString(),
                            null,
                            workSpaceDTO.getId(),
                            ActionPermission.FILE_PERMANENTLY_DELETE.getCode()), FORBIDDEN);
                }
                deleteFile(organizationId, workSpaceDTO);
                // 删除知识库权限配置信息
                permissionRangeKnowledgeObjectSettingService.removePermissionRange(workSpaceDTO.getOrganizationId(), workSpaceDTO.getProjectId(), PermissionTargetBaseType.FILE, workspaceId);
                break;
            case FOLDER:
                if (!isTemplate) {
                    Assert.isTrue(permissionCheckDomainService.checkPermission(workSpaceDTO.getOrganizationId(),
                            workSpaceDTO.getProjectId(),
                            FOLDER.toString(),
                            null,
                            workSpaceDTO.getId(),
                            ActionPermission.FOLDER_PERMANENTLY_DELETE.getCode()), FORBIDDEN);
                }
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
                permissionRangeKnowledgeObjectSettingService.removePermissionRange(workSpaceDTO.getOrganizationId(), workSpaceDTO.getProjectId(), PermissionTargetBaseType.FOLDER, workspaceId);
                break;
            case DOCUMENT:
                if (!isTemplate) {
                    Assert.isTrue(permissionCheckDomainService.checkPermission(workSpaceDTO.getOrganizationId(),
                            workSpaceDTO.getProjectId(),
                            FILE.toString(),
                            null,
                            workSpaceDTO.getId(),
                            ActionPermission.DOCUMENT_PERMANENTLY_DELETE.getCode()), FORBIDDEN);
                }
                deleteDocument(workSpaceDTO, organizationId);
                permissionRangeKnowledgeObjectSettingService.removePermissionRange(workSpaceDTO.getOrganizationId(), workSpaceDTO.getProjectId(), PermissionTargetBaseType.FILE, workspaceId);
                break;
            default:
                throw new CommonException("Unsupported knowledge space type");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreWorkSpaceAndPage(Long organizationId, Long projectId, Long workspaceId, Long baseId) {
        WorkSpaceDTO workSpaceDTO = this.workSpaceRepository.baseQueryById(organizationId, projectId, workspaceId);

        // FIXME 由于模板的存储结构有大问题, 这里暂时跳过对模板增删改操作的鉴权
        // 2022-10-27 pei.chen@zknow.com gaokuo.dai@zknow.com

        final boolean isTemplate = this.workSpaceRepository.checkIsTemplate(workSpaceDTO.getOrganizationId(), workSpaceDTO.getProjectId(), workSpaceDTO);

        if (!isTemplate) {
            Map<WorkSpaceType, IWorkSpaceService> iWorkSpaceServiceMap = iWorkSpaceServices.stream()
                    .collect(Collectors.toMap(IWorkSpaceService::handleSpaceType, Function.identity()));
            iWorkSpaceServiceMap.get(WorkSpaceType.of(workSpaceDTO.getType())).restore(workSpaceDTO);
        }
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
            workSpaceRepository.baseUpdate(workSpaceDTO);
            if (!WorkSpaceType.FOLDER.getValue().equalsIgnoreCase(workSpaceDTO.getType())) {
                workSpacePageService.createOrUpdateEs(workspaceId);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveWorkSpace(Long organizationId, Long projectId, Long workSpaceId, MoveWorkSpaceVO moveWorkSpaceVO) {
        WorkSpaceDTO targetWorkSpace = null;
        if (moveWorkSpaceVO.getTargetId() != 0) {
            targetWorkSpace = this.workSpaceRepository.baseQueryById(organizationId, projectId, moveWorkSpaceVO.getTargetId());
        }
        // FIXME ↓
        WorkSpaceDTO sourceWorkSpace = this.workSpaceRepository.baseQueryById(organizationId, projectId, moveWorkSpaceVO.getId());
        Map<WorkSpaceType, IWorkSpaceService> iWorkSpaceServiceMap = iWorkSpaceServices.stream()
                .collect(Collectors.toMap(IWorkSpaceService::handleSpaceType, Function.identity()));
        iWorkSpaceServiceMap.get(WorkSpaceType.of(sourceWorkSpace.getType())).move(sourceWorkSpace, targetWorkSpace);
        // FIXME ↑
        String oldRoute = sourceWorkSpace.getRoute();
        String rank = "";
        if (Boolean.TRUE.equals(moveWorkSpaceVO.getBefore())) {
            rank = beforeRank(organizationId, projectId, workSpaceId, moveWorkSpaceVO);
        } else {
            rank = afterRank(organizationId, projectId, workSpaceId, moveWorkSpaceVO);
        }
        sourceWorkSpace.setRank(rank);
        if (sourceWorkSpace.getParentId().equals(workSpaceId)) {
            workSpaceRepository.baseUpdate(sourceWorkSpace);
        } else {
            if (workSpaceId.equals(0L)) {
                sourceWorkSpace.setParentId(0L);
                sourceWorkSpace.setRoute(TypeUtil.objToString(sourceWorkSpace.getId()));
            } else {
                WorkSpaceDTO parent = this.workSpaceRepository.baseQueryById(organizationId, projectId, workSpaceId);
                sourceWorkSpace.setParentId(parent.getId());
                sourceWorkSpace.setRoute(parent.getRoute() + "." + sourceWorkSpace.getId());
            }
            sourceWorkSpace = workSpaceRepository.baseUpdate(sourceWorkSpace);

            if (Boolean.TRUE.equals(workSpaceMapper.hasChildWorkSpace(organizationId, projectId, sourceWorkSpace.getId()))) {
                String newRoute = sourceWorkSpace.getRoute();
                workSpaceMapper.updateChildByRoute(organizationId, projectId, oldRoute, newRoute);
            }
        }
    }

//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void removeWorkSpaceByBaseId(Long organizationId, Long projectId, Long baseId) {
//        List<Long> workSpaceIds = this.workSpaceRepository.listAllParentIdByBaseId(organizationId, projectId, baseId);
//        if (CollectionUtils.isNotEmpty(workSpaceIds)) {
//            for (Long workSpaceId : workSpaceIds) {
//                moveToRecycle(organizationId, projectId, workSpaceId, true, true);
//            }
//        }
//    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWorkSpaceByBaseId(Long organizationId, Long projectId, Long baseId) {
        List<Long> list = workSpaceMapper.listAllParentIdByBaseId(organizationId, projectId, baseId);
        if (!CollectionUtils.isEmpty(list)) {
            list.forEach(v -> deleteWorkSpaceAndPage(organizationId, projectId, v));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreWorkSpaceByBaseId(Long organizationId, Long projectId, Long baseId) {
        List<Long> list = workSpaceMapper.listAllParentIdByBaseId(organizationId, projectId, baseId);
        if (!CollectionUtils.isEmpty(list)) {
            list.forEach(v -> restoreWorkSpaceAndPage(organizationId, projectId, v, null));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkSpaceInfoVO clonePage(Long organizationId, Long projectId, Long workSpaceId, Long parentId, boolean skipPermission) {
        // 复制页面内容
        WorkSpaceDTO workSpaceDTO = getWorkSpaceDTO(organizationId, projectId, workSpaceId);
        //根据类型来判断
        if (StringUtils.equalsIgnoreCase(workSpaceDTO.getType(), WorkSpaceType.FILE.getValue())) {
            // 校验自身的复制权限
            if (!skipPermission) {
                Assert.isTrue(permissionCheckDomainService.checkPermission(organizationId,
                        projectId,
                        PermissionTargetBaseType.FILE.toString(),
                        null,
                        workSpaceId,
                        ActionPermission.FILE_COPY.getCode()), FORBIDDEN);
            }
            //获得文件 上传文件
            return cloneFile(projectId, organizationId, workSpaceDTO, parentId, skipPermission);
        } else {
            // 校验自身的复制权限
            if (!skipPermission) {
                Assert.isTrue(permissionCheckDomainService.checkPermission(organizationId,
                        projectId,
                        PermissionTargetBaseType.FILE.toString(),
                        null,
                        workSpaceId,
                        ActionPermission.DOCUMENT_COPY.getCode()), FORBIDDEN);
            }
            return cloneDocument(projectId, organizationId, workSpaceDTO, parentId, skipPermission);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Page<WorkBenchRecentVO> selectProjectRecentList(PageRequest pageRequest, Long organizationId, Long projectId, boolean selfFlag) {
        Assert.notNull(organizationId, BaseConstants.ErrorCode.DATA_INVALID);
        Long userId = DetailsHelper.getUserDetails().getUserId();
        List<ProjectDTO> projectList;
        if (Objects.nonNull(projectId)) {
            projectList = iamRemoteRepository.queryProjectByIds(Collections.singleton(projectId));
        } else {
            //这里考虑到汉得信息下项目众多，并且这里只用到了项目的id name 和imageUrl，提供轻量级的查询
            projectList = iamRemoteRepository.queryOrgProjectsOptional(organizationId, userId);
        }
        OrganizationDTO organization = Optional.ofNullable(iamRemoteRepository.queryOrganizationById(organizationId)).orElse(new OrganizationDTO());
        if (CollectionUtils.isEmpty(projectList)) {
            return new Page<>();
        }
        List<Long> projectIds = projectList.stream().map(ProjectDTO::getId).collect(Collectors.toList());

        String body = iamRemoteRepository.queryOrgLevel(organizationId);
        boolean isOrganizationAdmin = StringUtils.contains(body, "administrator");
        // 检查组织级权限
//        WorkBenchUserInfoVO workBenchUserInfo = permissionRangeKnowledgeObjectSettingRepository.queryWorkbenchUserInfo(organizationId);
//        List<RoleVO> roles = workBenchUserInfo.getRoles();
//        Set<String> organizationRoleCodes = new HashSet<>();
//        List<RoleVO> projectRoles = new ArrayList<>();
//        if (!ObjectUtils.isEmpty(roles)) {
//            roles.forEach(role -> {
//                Long thisProjectId = role.getProjectId();
//                if (thisProjectId == null) {
//                    //组织层角色
//                    organizationRoleCodes.add(role.getCode());
//                } else{
//                    projectRoles.add(role);
//                }
//            });
//        }
//        workBenchUserInfo.setRoles(projectRoles);
//        boolean isOrganizationAdmin = organizationRoleCodes.contains("administrator");
//        Page<WorkBenchRecentVO> recentResults;
//        List<Integer> rowNums = new ArrayList<>();
//        if (!selfFlag) {
//            int maxDepth = workSpaceRepository.selectRecentMaxDepth(organizationId, projectId, null, false);
//            for (int i = 2; i <= maxDepth; i++) {
//                rowNums.add(i);
//            }
//        }
        Page<WorkBenchRecentVO> recentResults =
                PageHelper.doPage(pageRequest,
                        () -> workSpaceMapper.selectProjectRecentList(
                                organizationId,
                                projectIds,
                                selfFlag,
                                isOrganizationAdmin,
                                userId));
        if (CollectionUtils.isEmpty(recentResults)) {
            return recentResults;
        }
        // 取一个月内的更新人
        List<Long> pageIdList = recentResults.stream().map(WorkBenchRecentVO::getPageId).collect(Collectors.toList());
        List<PageLogDTO> pageLogList = pageLogMapper.selectByPageIdList(pageIdList, DateUtils.truncate(DateUtils.addDays(new Date(), -30), Calendar.DAY_OF_MONTH));
        Map<Long, List<PageLogDTO>> pageMap =
                pageLogList.stream().collect(Collectors.groupingBy(PageLogDTO::getPageId));
        // 获取用户信息
        Map<Long, UserDO> map = iamRemoteRepository.listUsersByIds(
                        pageLogList.stream().map(PageLogDTO::getCreatedBy).collect(Collectors.toList()),
                        false
                ).stream()
                .collect(Collectors.toMap(UserDO::getId, Function.identity()));
        // 获取项目logo
        Map<Long, ProjectDTO> projectMap = projectList.stream().collect(Collectors.toMap(ProjectDTO::getId, Function.identity()));
        List<PageLogDTO> temp;
        Map<Long, Boolean> approveMap = new HashMap<>();
        for (WorkBenchRecentVO recent : recentResults) {
            Long thisProjectId = recent.getProjectId();
            Long baseId = recent.getBaseId();
            Boolean approve = approveMap.get(baseId);
            if (approve == null) {
                approve =
                        permissionCheckDomainService.checkPermission(
                                organizationId,
                                thisProjectId,
                                PermissionConstants.PermissionTargetBaseType.KNOWLEDGE_BASE.toString(),
                                null,
                                baseId,
                                PermissionConstants.ActionPermission.KNOWLEDGE_BASE_READ.getCode(),
                                false
                        );
                approveMap.put(baseId, approve);
            }
            recent.setApprove(approve);
            temp = sortAndDistinct(pageMap.get(recent.getPageId()), Comparator.comparing(PageLogDTO::getCreationDate).reversed());
            if (temp.size() > 3) {
                recent.setOtherUserCount(temp.size() - 3);
                temp = temp.subList(0, 2);
            }
            recent.setUpdatedUserList(temp.stream().map(PageLogDTO::getCreatedBy).map(map::get).collect(Collectors.toList()));
            if (Objects.isNull(recent.getProjectId())) {
                recent.setOrgFlag(true);
            }
            recent.setImageUrl(projectMap.getOrDefault(recent.getProjectId(), new ProjectDTO()).getImageUrl());
            recent.setProjectName(projectMap.getOrDefault(recent.getProjectId(), new ProjectDTO()).getName());
            recent.setOrganizationName(organization.getTenantName());
        }
        UserInfoVO.clearCurrentUserInfo();
        return recentResults;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Saga(code = WorkSpaceRepository.KNOWLEDGE_UPLOAD_FILE, description = "知识库上传文件", inputSchemaClass = PageCreateWithoutContentVO.class)
    public WorkSpaceInfoVO upload(Long projectId, Long organizationId, PageCreateWithoutContentVO createVO, boolean templateFlag) {
        createVO.setOrganizationId(organizationId);
        createVO.setTemplateFlag(templateFlag);
        //把文件读出来传到文件服务器上面去获得fileKey
        checkParams(createVO);
        //获取父空间id和route
        Long parentId = createVO.getParentWorkspaceId();
        String route = "";
        // 默认为知识库根目录, 如果父级有值则设置为对应类型
        PermissionConstants.PermissionTargetBaseType permissionTargetBaseType = KNOWLEDGE_BASE;
        if (parentId != null && !parentId.equals(0L)) {
            WorkSpaceDTO parentWorkSpace = this.workSpaceRepository.baseQueryById(organizationId, projectId, parentId);
            permissionTargetBaseType = PermissionTargetBaseType.ofWorkSpaceType(WorkSpaceType.of(parentWorkSpace.getType()));
            route = parentWorkSpace.getRoute();
        } else {
            parentId = 0L;
        }
        // 上传文件校验，校验上级权限
        if (!templateFlag) {
            Assert.isTrue(permissionCheckDomainService.checkPermission(organizationId,
                    projectId,
                    permissionTargetBaseType.toString(),
                    null,
                    permissionTargetBaseType == KNOWLEDGE_BASE ? createVO.getBaseId() : parentId,
                    ActionPermission.FILE_CREATE.getCode()), FORBIDDEN);
        }

        PageDTO page = pageService.createPage(organizationId, projectId, createVO);
        WorkSpaceDTO workSpaceDTO = initWorkSpaceDTO(projectId, organizationId, createVO);
        //设置rank值
        if (Boolean.TRUE.equals(workSpaceMapper.hasChildWorkSpace(organizationId, projectId, parentId))) {
            String rank = workSpaceMapper.queryMaxRank(organizationId, projectId, parentId);
            workSpaceDTO.setRank(RankUtil.genNext(rank));
        } else {
            workSpaceDTO.setRank(RankUtil.mid());
        }
        workSpaceDTO.setParentId(parentId);
        //创建空间
        workSpaceDTO = workSpaceRepository.baseCreate(workSpaceDTO);
        //设置新的route
        String realRoute = route.isEmpty() ? workSpaceDTO.getId().toString() : route + "." + workSpaceDTO.getId();
        workSpaceDTO.setRoute(realRoute);
        workSpaceRepository.baseUpdate(workSpaceDTO);
        //创建空间和页面的关联关系
        workSpacePageRepository.baseCreate(page.getId(), workSpaceDTO.getId());
        pageRepository.createOrUpdateEs(page.getId());
        //返回workSpaceInfo
        WorkSpaceInfoVO workSpaceInfoVO = this.workSpaceRepository.queryWorkSpaceInfo(organizationId, projectId, workSpaceDTO.getId(), null, false, templateFlag);
        workSpaceInfoVO.setWorkSpace(WorkSpaceTreeNodeVO.of(workSpaceDTO, Collections.emptyList()));

        // 初始化权限
        permissionAggregationService.autoGeneratePermission(organizationId, projectId, PermissionTargetBaseType.FILE, workSpaceInfoVO.getWorkSpace());

        createVO.setRefId(workSpaceInfoVO.getId());
        try {
            String input = mapper.writeValueAsString(createVO);
            transactionalProducer.apply(StartSagaBuilder.newBuilder()
                            .withRefId(String.valueOf(workSpaceInfoVO.getId()))
                            .withRefType(createVO.getSourceType())
                            .withSagaCode(WorkSpaceRepository.KNOWLEDGE_UPLOAD_FILE)
                            .withLevel(ResourceLevel.valueOf(createVO.getSourceType().toUpperCase()))
                            .withSourceId(createVO.getSourceId())
                            .withJson(input),
                    builder -> {
                    });
        } catch (Exception e) {
            throw new CommonException("error.upload.file", e);
        }
        // 填充权限信息
        workSpaceInfoVO.setPermissionCheckInfos(permissionInfos(projectId, organizationId, workSpaceDTO));
        return workSpaceInfoVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileSimpleDTO uploadMultipartFileWithMD5(Long organizationId, String directory, String fileName, Integer docType, String storageCode, MultipartFile multipartFile) {
        // 放开文件类型的校验
//        checkFileType(multipartFile);
        if (StringUtils.isBlank(fileName)) {
            fileName = multipartFile.getOriginalFilename();
        }
        //调用file服务也需要分片去传，不然也有大小的限制
        // FileSimpleDTO fileSimpleDTO = expandFileClient.uploadFileWithMD5(organizationId, BaseStage.BACKETNAME, null, fileName, multipartFile);
//        String url = expandFileClient.uploadFile(organizationId, BaseStage.BACKETNAME, null, fileName, BaseConstants.Flag.NO, storageCode, multipartFile);
        //手动实现文件的分片上传
        String url = null;
        try {
            int sliceSize = 5242880;
            // 每5M进行一次切片
            // 暂不支持断点续传，直接使用uuid做文件指纹
            String guid = UUIDUtils.generateUUID();
            // 计算分片数量
            long fileSize = multipartFile.getSize();
            int lastSize = (int) (fileSize % sliceSize);
            int count = (int) (fileSize / sliceSize);
            if (lastSize != 0) {
                count += 1;
            }
            byte[] buffer = new byte[5242880];
            InputStream inputStream = multipartFile.getInputStream();
            int i = 0;
            while (inputStream.read(buffer, 0, 5242880) != -1) {
                int size = sliceSize;
                if (lastSize != 0 && i == (count - 1)) {
                    size = lastSize;
                }
                byte[] slice = new byte[size];
                System.arraycopy(buffer, 0, slice, 0, slice.length);
                ResponseUtils.getResponse(fileRemoteService.uploadByteSlice(organizationId, BaseStage.BACKETNAME, directory, storageCode, fileName, MediaType.APPLICATION_OCTET_STREAM_VALUE, i, guid, slice), Void.class);
                i++;
            }
            // 合并分片文件
            url = ResponseUtils.getResponse(fileRemoteService.combine(organizationId, BaseStage.BACKETNAME, directory, storageCode, fileName, fileSize, MediaType.APPLICATION_OCTET_STREAM_VALUE, guid, null), String.class);
            inputStream.close();
        } catch (IOException e) {
            throw new CommonException(e);
        }
        if (StringUtils.isEmpty(url)) {
            throw new CommonException("error.url.is.null");
        }
        FileSimpleDTO fileSimpleDTO = new FileSimpleDTO();
        fileSimpleDTO.setFileKey(filePathService.generateRelativePath(url).substring(1));
        return fileSimpleDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void renameWorkSpace(Long projectId, Long organizationId, Long id, String newName) {
        WorkSpaceDTO spaceDTO = workSpaceRepository.selectByPrimaryKey(id);
        if (spaceDTO == null) {
            return;
        }
        Map<WorkSpaceType, IWorkSpaceService> spaceServiceMap = iWorkSpaceServices.stream()
                .collect(Collectors.toMap(IWorkSpaceService::handleSpaceType, Function.identity()));
        spaceServiceMap.get(WorkSpaceType.of(spaceDTO.getType())).rename(spaceDTO, newName);
        workSpaceRepository.baseUpdate(spaceDTO);
    }

    private WorkSpaceDTO createWorkSpace(Long organizationId, Long projectId, PageCreateWithoutContentVO createVO, ActionPermission actionPermission, boolean initFlag) {
        WorkSpaceDTO workSpaceDTO = new WorkSpaceDTO();
        workSpaceDTO.setOrganizationId(organizationId);
        workSpaceDTO.setProjectId(projectId);
        workSpaceDTO.setName(createVO.getTitle());
        workSpaceDTO.setBaseId(createVO.getBaseId());
        workSpaceDTO.setDescription(createVO.getDescription());
        workSpaceDTO.setType(createVO.getType());
        workSpaceDTO.setTemplateCategory(createVO.getTemplateCategory());
        workSpaceDTO.setTemplateFlag(createVO.getTemplateFlag());
        //获取父空间id和route
        Long parentId = createVO.getParentWorkspaceId();
        String route = "";
        PermissionTargetBaseType permissionTargetBaseType = KNOWLEDGE_BASE;
        if (parentId != null && !parentId.equals(0L)) {
            WorkSpaceDTO parentWorkSpace = this.workSpaceRepository.baseQueryById(organizationId, projectId, parentId);
            route = parentWorkSpace.getRoute();
            permissionTargetBaseType = PermissionTargetBaseType.ofWorkSpaceType(WorkSpaceType.of(parentWorkSpace.getType()));
        } else {
            parentId = 0L;
        }
        // 创建校验，校验上级权限
        if (!createTemplate(createVO)) {
            if (!initFlag) {
                Assert.isTrue(permissionCheckDomainService.checkPermission(organizationId,
                        projectId,
                        permissionTargetBaseType.toString(),
                        null,
                        permissionTargetBaseType == KNOWLEDGE_BASE ? createVO.getBaseId() : parentId,
                        actionPermission.getCode()), FORBIDDEN);
            }
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
        workSpaceDTO = workSpaceRepository.baseCreate(workSpaceDTO);
        //设置新的route
        String realRoute = route.isEmpty() ? workSpaceDTO.getId().toString() : route + "." + workSpaceDTO.getId();
        workSpaceDTO.setRoute(realRoute);
        workSpaceRepository.baseUpdate(workSpaceDTO);
        return workSpaceDTO;
    }


    private void checkFolderNameLength(String title) {
        if (StringUtils.isBlank(title) && title.length() > LENGTH_LIMIT) {
            throw new CommonException("error.folder.name.length.limit.exceeded", LENGTH_LIMIT);
        }
    }

    /**
     * 批量移至回收站
     *
     * @param childWorkSpaces 需要移动的数据
     */
    protected void batchMoveToRecycle(List<WorkSpaceDTO> childWorkSpaces) {
        if (CollectionUtils.isEmpty(childWorkSpaces)) {
            return;
        }
        // 先删除es，再改状态
        for (WorkSpaceDTO childWorkSpace : childWorkSpaces) {
            workSpacePageService.deleteEs(childWorkSpace.getId());
            childWorkSpace.setDelete(true);
        }
        workSpaceRepository.batchUpdateOptional(childWorkSpaces, WorkSpaceDTO.FIELD_DELETE);
    }

    private void checkRemovePermission(Long organizationId, Long projectId, WorkSpacePageDTO workSpacePageDTO, boolean isAdmin) {
        if (isAdmin || workSpacePageDTO == null) {
            return;
        }
        //删除文档的逻辑  组织管理员可以删除组织下所有的，组织成员只能删除自己创建的
        Long currentUserId = Optional.ofNullable(DetailsHelper.getUserDetails()).map(CustomUserDetails::getUserId).orElse(null);
        if (projectId != null) {
            //组织层校验
            Boolean isProjectAdmin = iamRemoteRepository.checkAdminPermission(projectId);
            if (!Boolean.TRUE.equals(isProjectAdmin)) {
                PageDTO pageDTO = pageRepository.baseQueryById(organizationId, projectId, workSpacePageDTO.getPageId());
                if (!Objects.equals(workSpacePageDTO.getCreatedBy(), currentUserId) && !Objects.equals(pageDTO.getCreatedBy(), currentUserId)) {
                    throw new CommonException(WorkSpaceRepository.ERROR_WORKSPACE_ILLEGAL);
                }
            }
        } else {
            Boolean isOrgAdmin = iamRemoteRepository.checkIsOrgRoot(organizationId, currentUserId);
            if (!Boolean.TRUE.equals(isOrgAdmin)) {
                PageDTO pageDTO = pageRepository.baseQueryById(organizationId, projectId, workSpacePageDTO.getPageId());
                if (!Objects.equals(workSpacePageDTO.getCreatedBy(), currentUserId) && !Objects.equals(pageDTO.getCreatedBy(), currentUserId)) {
                    throw new CommonException(WorkSpaceRepository.ERROR_WORKSPACE_ILLEGAL);
                }
            }
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
//        }

    }

    private void deleteFile(Long organizationId, WorkSpaceDTO workSpaceDTO) {
        FileVO fileDTOByFileKey = expandFileClient.getFileDTOByFileKey(organizationId, workSpaceDTO.getFileKey());
        expandFileClient.deleteFileByUrlWithDbOptional(organizationId, BaseStage.BACKETNAME, Collections.singletonList(fileDTOByFileKey.getFileUrl()));
        //删除workSpace
        workSpaceMapper.deleteByPrimaryKey(workSpaceDTO.getId());
        //删除 workspace page
        WorkSpacePageDTO spacePageDTO = workSpacePageService.selectByWorkSpaceId(workSpaceDTO.getId());
        workSpacePageService.baseDelete(spacePageDTO.getId());
        //删除评论
        pageCommentRepository.deleteByPageId(spacePageDTO.getId());
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

    private void updateWorkSpace(WorkSpaceDTO workSpaceDTO, Long organizationId, Long projectId, Long workspaceId, Long baseId) {
        //恢复到顶层
        String[] split = StringUtils.split(workSpaceDTO.getRoute(), BaseConstants.Symbol.POINT);
        int index = ArrayUtils.indexOf(split, String.valueOf(workspaceId));
        String[] subarray = (String[]) ArrayUtils.subarray(split, 0, index);
        String join = StringUtils.join(subarray, BaseConstants.Symbol.POINT);

        List<WorkSpaceDTO> workSpaceDTOS = workSpaceMapper.selectAllChildByRoute(workSpaceDTO.getRoute(), false);
        workSpaceDTO.setBaseId(baseId);
        workSpaceDTO.setDelete(false);
        workSpaceDTO.setParentId(0L);
        workSpaceDTO.setRoute(String.valueOf(workSpaceDTO.getId()));
        String rank = workSpaceMapper.queryMaxRank(organizationId, projectId, 0L);
        workSpaceDTO.setRank(RankUtil.genNext(rank));
        workSpaceRepository.baseUpdate(workSpaceDTO);
        workSpacePageService.createOrUpdateEs(workspaceId);

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
                workSpaceRepository.baseUpdate(workSpace);
            }
        }

    }

    @Override
    public WorkSpaceInfoVO cloneFolder(Long projectId,
                                       Long organizationId,
                                       Long workSpaceId,
                                       Long parentId,
                                       Long knowledgeBaseId) {
        WorkSpaceDTO folder = workSpaceRepository.selectByPrimaryKey(workSpaceId);
        if (folder == null) {
            throw new CommonException("error.clone.taregt.obj.not.exist");
        }
        PageCreateWithoutContentVO pageCreateVO = new PageCreateWithoutContentVO();
        pageCreateVO.setBaseId(knowledgeBaseId);
        pageCreateVO.setParentWorkspaceId(parentId);
        pageCreateVO.setTitle(folder.getName());
        pageCreateVO.setType(WorkSpaceType.FOLDER.getValue());
        return createWorkSpaceAndPage(organizationId, projectId, pageCreateVO, true);
    }


    private WorkSpaceInfoVO cloneDocument(Long projectId, Long organizationId, WorkSpaceDTO workSpaceDTO, Long parentId, boolean templateFlag) {
        PageContentDTO pageContentDTO = pageContentMapper.selectLatestByWorkSpaceId(workSpaceDTO.getId());
        PageCreateVO pageCreateVO = new PageCreateVO(parentId, workSpaceDTO.getName(), pageContentDTO.getContent(), workSpaceDTO.getBaseId(), workSpaceDTO.getType());
        pageCreateVO.setTemplateFlag(templateFlag);
        WorkSpaceInfoVO pageWithContent = pageService.createPageWithContent(organizationId, projectId, pageCreateVO, false);
        // 复制页面的附件
        List<PageAttachmentDTO> pageAttachmentDTOS = pageAttachmentMapper.selectByPageId(pageContentDTO.getPageId());

        if (CollectionUtils.isNotEmpty(pageAttachmentDTOS)) {
            Long userId = Optional.ofNullable(DetailsHelper.getUserDetails()).map(CustomUserDetails::getUserId).orElse(null);
            for (PageAttachmentDTO attach : pageAttachmentDTOS) {
                attach.setId(null);
                attach.setPageId(pageWithContent.getPageInfo().getId());
                attach.setCreatedBy(userId);
                attach.setLastUpdatedBy(userId);
            }
            List<PageAttachmentDTO> attachmentDTOS = pageAttachmentService.batchInsert(pageAttachmentDTOS);
            pageWithContent.setPageAttachments(
                    modelMapper.map(
                            attachmentDTOS,
                            new TypeReference<List<PageAttachmentVO>>() {
                            }.getType()
                    )
            );
        }
        return pageWithContent;
    }

    private WorkSpaceInfoVO cloneFile(Long projectId, Long organizationId, WorkSpaceDTO workSpaceDTO, Long parentId, boolean templateFlag) {
        // 优化文件的复制
        FileVO fileDTOByFileKey = expandFileClient.getFileDTOByFileKey(organizationId, workSpaceDTO.getFileKey());
        if (Objects.isNull(fileDTOByFileKey) || StringUtils.isBlank(fileDTOByFileKey.getFileUrl())) {
            throw new CommonException(ERROR_GET_FILE_BY_KEY);
        }
        String fileName = generateFileName(workSpaceDTO.getName());
        String copyFileByUrl = expandFileClient.copyFileByUrl(organizationId, fileDTOByFileKey.getFileUrl(), fileDTOByFileKey.getBucketName(), fileDTOByFileKey.getBucketName(), fileName);
        FileVO fileVO = getFileByUrl(organizationId, copyFileByUrl);
        //创建workSpace
        PageCreateWithoutContentVO pageCreateWithoutContentVO = new PageCreateWithoutContentVO();
        pageCreateWithoutContentVO.setTitle(fileName);
        pageCreateWithoutContentVO.setFileKey(fileVO.getFileKey());
        pageCreateWithoutContentVO.setBaseId(workSpaceDTO.getBaseId());
        pageCreateWithoutContentVO.setType(WorkSpaceType.FILE.getValue());
        pageCreateWithoutContentVO.setParentWorkspaceId(parentId);
        pageCreateWithoutContentVO.setFileSourceType(FileSourceType.COPY.getFileSourceType());
        pageCreateWithoutContentVO.setSourceType(projectId == null ? ResourceLevel.ORGANIZATION.value() : ResourceLevel.PROJECT.value());
        pageCreateWithoutContentVO.setSourceId(projectId == null ? organizationId : projectId);
        pageCreateWithoutContentVO.setTemplateFlag(templateFlag);
        return createPageWithoutContent(projectId, organizationId, pageCreateWithoutContentVO);
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
            throw new CommonException(WorkSpaceRepository.ERROR_WORKSPACE_NOTFOUND);
        }
        return workSpaceDTO;
    }

    private MultipartFile getMultipartFile(InputStream inputStream, String fileName) {
        FileItem fileItem = createFileItem(inputStream, fileName);
        //CommonsMultipartFile是feign对multipartFile的封装，但是要FileItem类对象
        return new CommonsMultipartFile(fileItem);
    }

    private FileItem createFileItem(InputStream inputStream, String fileName) {
        FileItemFactory factory = new DiskFileItemFactory(16, null);
        String textFieldName = "file";
        //contentType为multipart/form-data  minio报400
        FileItem item = factory.createItem(textFieldName, MediaType.APPLICATION_OCTET_STREAM_VALUE, true, fileName);
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

    private List<PermissionCheckVO> permissionInfos(Long projectId, Long organizationId, WorkSpaceDTO workSpaceDTO) {
        // 文件/文件夹/文档type一致于permissionActionRange
        final String permissionActionRange = workSpaceDTO.getType();
        final String targetBaseType = Objects.requireNonNull(WorkSpaceType.toTargetBaseType(workSpaceDTO.getType())).toString();
        return permissionCheckDomainService.checkPermission(
                organizationId,
                projectId,
                targetBaseType,
                null,
                workSpaceDTO.getId(),
                ActionPermission.generatePermissionCheckVOList(permissionActionRange)
        );
    }

    private void checkFileSize(String unit, Long fileSize, Long size) {
        if (FileUtil.StorageUnit.MB.equals(unit) && fileSize > size * FileUtil.ENTERING * FileUtil.ENTERING) {
            throw new CommonException(FileUtil.ERROR_FILE_SIZE, size + unit);
        } else if (FileUtil.StorageUnit.KB.equals(unit) && fileSize > size * FileUtil.ENTERING) {
            throw new CommonException(FileUtil.ERROR_FILE_SIZE, size + unit);
        }
    }

    private void checkParams(PageCreateWithoutContentVO createVO) {
        if (StringUtils.isBlank(createVO.getFileSourceType())) {
            throw new CommonException("file.source.type.is.null");
        }
        if (StringUtils.equalsIgnoreCase(createVO.getFilePath(), FileSourceType.UPLOAD.getFileSourceType()) && StringUtils.isBlank(createVO.getFilePath())) {
            throw new CommonException("file.path.is.null");
        }
    }

    private void checkFileType(MultipartFile multipartFile) {
        String originalFilename = multipartFile.getOriginalFilename();
        List<String> onlyFileFormats = FileFormatType.ONLY_FILE_FORMATS;
        if (StringUtils.equalsIgnoreCase(fileServerUploadTypeLimit, FilePlatformType.WPS.getPlatformType())) {
            onlyFileFormats = FileFormatType.WPS_FILE_FORMATS;
        }
        if (StringUtils.isEmpty(originalFilename) || !onlyFileFormats.contains(CommonUtil.getFileTypeByFileName(originalFilename).toUpperCase())) {
            throw new CommonException("error.not.supported.file.upload");
        }

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
        String[] parents = StringUtils.split(workSpaceDTO.getRoute(), BaseConstants.Symbol.POINT);
        List<String> list = Arrays.asList(parents);
        List<Long> parentIds = list.stream().filter(StringUtils::isNumeric).map(Long::parseLong).collect(Collectors.toList());
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
        workSpaceDTO.setName(createVO.getTitle());
        workSpaceDTO.setBaseId(createVO.getBaseId());
        workSpaceDTO.setDescription(createVO.getDescription());
        workSpaceDTO.setFileKey(createVO.getFileKey());
        workSpaceDTO.setType(createVO.getType());
        workSpaceDTO.setTemplateFlag(createVO.getTemplateFlag());
        return workSpaceDTO;
    }

    private FileVO getFileByUrl(Long organizationId, String copyFileByUrl) {
        String fileKey = CommonUtil.getFileKeyByUrl(copyFileByUrl);
        FileVO fileDTOByFileKey = expandFileClient.getFileDTOByFileKey(organizationId, fileKey);
        if (fileDTOByFileKey == null) {
            throw new CommonException(ERROR_GET_FILE_BY_URL);
        }
        return fileDTOByFileKey;
    }

    private WorkSpaceInfoVO createPageWithoutContent(Long projectId, Long organizationId, PageCreateWithoutContentVO createVO) {
        createVO.setOrganizationId(organizationId);
        checkParams(createVO);
        //获取父空间id和route
        Long parentId = createVO.getParentWorkspaceId();
        String route = "";
        PageDTO page = pageService.createPage(organizationId, projectId, createVO);
        WorkSpaceDTO workSpaceDTO = initWorkSpaceDTO(projectId, organizationId, createVO);
        //设置rank值
        if (Boolean.TRUE.equals(this.workSpaceMapper.hasChildWorkSpace(organizationId, projectId, parentId))) {
            String rank = this.workSpaceMapper.queryMaxRank(organizationId, projectId, parentId);
            workSpaceDTO.setRank(RankUtil.genNext(rank));
        } else {
            workSpaceDTO.setRank(RankUtil.mid());
        }
        workSpaceDTO.setParentId(parentId);
        //创建空间
        workSpaceDTO = workSpaceRepository.baseCreate(workSpaceDTO);
        //设置新的route
        String realRoute = route.isEmpty() ? workSpaceDTO.getId().toString() : route + "." + workSpaceDTO.getId();
        workSpaceDTO.setRoute(realRoute);
        workSpaceRepository.baseUpdate(workSpaceDTO);
        //创建空间和页面的关联关系
        workSpacePageRepository.baseCreate(page.getId(), workSpaceDTO.getId());
        pageRepository.createOrUpdateEs(page.getId());
        //返回workSpaceInfo
        WorkSpaceInfoVO workSpaceInfoVO = this.workSpaceRepository.queryWorkSpaceInfo(organizationId, projectId, workSpaceDTO.getId(), null, false, false);
        workSpaceInfoVO.setWorkSpace(WorkSpaceTreeNodeVO.of(workSpaceDTO, Collections.emptyList()));
        // 初始化权限
        permissionAggregationService.autoGeneratePermission(organizationId, projectId, PermissionTargetBaseType.FILE, workSpaceInfoVO.getWorkSpace());
        createVO.setRefId(workSpaceInfoVO.getId());
        // 填充权限信息
        workSpaceInfoVO.setPermissionCheckInfos(permissionInfos(projectId, organizationId, workSpaceDTO));
        return workSpaceInfoVO;
    }


    private boolean createTemplate(PageCreateWithoutContentVO createVO) {
        return !Objects.isNull(createVO.getTemplateFlag()) && createVO.getTemplateFlag();
    }
}
