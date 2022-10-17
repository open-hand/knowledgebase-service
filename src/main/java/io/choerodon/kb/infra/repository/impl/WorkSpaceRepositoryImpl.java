package io.choerodon.kb.infra.repository.impl;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.springframework.util.StopWatch;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.core.utils.PageUtils;
import io.choerodon.kb.api.vo.*;
import io.choerodon.kb.api.vo.permission.PermissionCheckVO;
import io.choerodon.kb.api.vo.permission.UserInfoVO;
import io.choerodon.kb.app.service.assembler.WorkSpaceAssembler;
import io.choerodon.kb.domain.repository.*;
import io.choerodon.kb.domain.service.PermissionCheckDomainService;
import io.choerodon.kb.infra.dto.KnowledgeBaseDTO;
import io.choerodon.kb.infra.dto.PageContentDTO;
import io.choerodon.kb.infra.dto.UserSettingDTO;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.enums.OpenRangeType;
import io.choerodon.kb.infra.enums.PermissionConstants;
import io.choerodon.kb.infra.enums.WorkSpaceTreeType;
import io.choerodon.kb.infra.enums.WorkSpaceType;
import io.choerodon.kb.infra.feign.vo.FileVO;
import io.choerodon.kb.infra.feign.vo.OrganizationDTO;
import io.choerodon.kb.infra.feign.vo.SagaInstanceDetails;
import io.choerodon.kb.infra.feign.vo.UserDO;
import io.choerodon.kb.infra.mapper.WorkSpaceMapper;
import io.choerodon.kb.infra.utils.CommonUtil;
import io.choerodon.kb.infra.utils.EsRestUtil;
import io.choerodon.kb.infra.utils.ExpandFileClient;
import io.choerodon.kb.infra.utils.SagaInstanceUtils;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import org.hzero.core.base.BaseConstants;
import org.hzero.core.redis.RedisHelper;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.hzero.starter.keyencrypt.core.EncryptContext;
import org.hzero.starter.keyencrypt.core.EncryptionService;

/**
 * Copyright (c) 2022. Hand Enterprise Solution Company. All right reserved.
 *
 * @author zongqi.hao@zknow.com
 * @since 2022/8/29
 */
@Repository
public class WorkSpaceRepositoryImpl extends BaseRepositoryImpl<WorkSpaceDTO> implements WorkSpaceRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkSpaceRepositoryImpl.class);
    private static final String TOP_TITLE = "choerodon";
    private static final String SETTING_TYPE_EDIT_MODE = "edit_mode";
    private static final String BASE_READ = PermissionConstants.ActionPermission.KNOWLEDGE_BASE_READ.getCode();


    @Autowired
    private WorkSpaceMapper workSpaceMapper;
    @Autowired
    private KnowledgeBaseRepository knowledgeBaseRepository;
    @Autowired
    private PageCommentRepository pageCommentRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private PageAttachmentRepository pageAttachmentRepository;
    @Autowired
    private UserSettingRepository userSettingRepository;
    @Autowired
    @Lazy
    private PermissionRangeKnowledgeObjectSettingRepository permissionRangeKnowledgeObjectSettingRepository;
    @Autowired
    @Lazy
    private PermissionCheckDomainService permissionCheckDomainService;
    @Autowired
    private IamRemoteRepository iamRemoteRepository;
    @Autowired
    private AsgardRemoteRepository asgardRemoteRepository;
    @Autowired
    private WorkSpaceAssembler workSpaceAssembler;
    @Autowired
    private RedisHelper redisHelper;
    @Autowired
    private EsRestUtil esRestUtil;
    @Autowired
    private ExpandFileClient expandFileClient;
    @Autowired
    private EncryptionService encryptionService;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public WorkSpaceDTO baseQueryById(Long organizationId, Long projectId, Long workSpaceId) {
        WorkSpaceDTO workSpaceDTO = workSpaceMapper.selectByPrimaryKey(workSpaceId);
        if (workSpaceDTO == null) {
            throw new CommonException(ERROR_WORKSPACE_NOTFOUND);
        }
        if (Objects.equals(workSpaceDTO.getOrganizationId(), PermissionConstants.EMPTY_ID_PLACEHOLDER) || (workSpaceDTO.getProjectId() != null && Objects.equals(workSpaceDTO.getProjectId(), PermissionConstants.EMPTY_ID_PLACEHOLDER))) {
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
        if (Objects.equals(workSpaceDTO.getOrganizationId(), PermissionConstants.EMPTY_ID_PLACEHOLDER) || (workSpaceDTO.getProjectId() != null && Objects.equals(workSpaceDTO.getProjectId(), PermissionConstants.EMPTY_ID_PLACEHOLDER))) {
            return workSpaceDTO;
        }
        if (organizationId != null && workSpaceDTO.getOrganizationId() != null && !workSpaceDTO.getOrganizationId().equals(organizationId)) {
            throw new CommonException(ERROR_WORKSPACE_ILLEGAL);
        }
        if (projectId != null && workSpaceDTO.getProjectId() != null && !workSpaceDTO.getProjectId().equals(projectId)) {
            KnowledgeBaseDTO knowledgeBaseDTO = this.knowledgeBaseRepository.selectByPrimaryKey(workSpaceDTO.getBaseId());
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
    public void checkExistsById(Long organizationId, Long projectId, Long workSpaceId) {
        baseQueryById(organizationId, projectId, workSpaceId);
    }

    @Override
    public List<WorkSpaceDTO> queryAllChildByWorkSpaceId(Long workSpaceId) {
        WorkSpaceDTO rootWorkSpace = this.selectByPrimaryKey(workSpaceId);
        if(rootWorkSpace == null) {
            return Collections.emptyList();
        }
        List<WorkSpaceDTO> childrenNodes = workSpaceMapper.selectAllChildByRoute(rootWorkSpace.getRoute(), true);
        childrenNodes.add(rootWorkSpace);
        return childrenNodes;
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
                return queryWorkSpaceInfoVO(organizationId, projectId, workSpaceId, searchStr, workSpaceDTO);
            case FILE:
                return queryFileInfo(organizationId, projectId, workSpaceId, workSpaceDTO);
            default:
                throw new CommonException("Unsupported knowledge space type");
        }
    }

    @Override
    public boolean belongToBaseExist(Long organizationId, Long projectId, Long workSpaceId) {
        WorkSpaceDTO workSpace = new WorkSpaceDTO();
        workSpace.setId(workSpaceId);
        workSpace.setOrganizationId(organizationId);
        workSpace.setProjectId(projectId);
        workSpace = workSpaceMapper.selectOne(workSpace);
        if(workSpace == null) {
            return false;
        }
        KnowledgeBaseDTO knowledgeBase = knowledgeBaseRepository.selectByPrimaryKey(workSpace.getBaseId());
        return knowledgeBase != null && !Boolean.TRUE.equals(knowledgeBase.getDelete());
    }

    @Override
    public WorkSpaceTreeVO queryAllChildTreeByWorkSpaceId(Long workSpaceId, boolean needChild) {
        // 获取当前对象信息
        final WorkSpaceDTO currentWorkSpace = this.selectByPrimaryKey(workSpaceId);
        if(currentWorkSpace == null) {
            return null;
        }
        // 获取树对象节点列表
        final List<WorkSpaceDTO> workSpaceList = needChild ?
                this.queryAllChildByWorkSpaceId(workSpaceId) :
                Collections.singletonList(currentWorkSpace);
        // 组装树
        final Long projectId = currentWorkSpace.getProjectId();
        final List<WorkSpaceTreeNodeVO> nodeList = this.buildWorkSpaceTree(
                currentWorkSpace.getOrganizationId(),
                projectId,
                workSpaceList,
                Boolean.TRUE.equals(needChild) ? workSpaceId : null,
                false
        );
        // 组装结果
        return new WorkSpaceTreeVO()
                .setRootId(PermissionConstants.EMPTY_ID_PLACEHOLDER)
                .setNodeList(nodeList);
    }

    @Override
    public WorkSpaceTreeVO queryAllTreeList(Long organizationId, Long projectId, Long knowledgeBaseId, Long expandWorkSpaceId, String excludeTypeCsv) {
        // 查询知识库信息
        final KnowledgeBaseDTO knowledgeBase = this.findKnowledgeBase(organizationId, projectId, knowledgeBaseId);
        if (knowledgeBase == null) {
            throw new CommonException(ERROR_WORKSPACE_NOTFOUND);
        }
        // 获取排除的类型
        final List<String> excludeTypes = StringUtils.isBlank(excludeTypeCsv) ?
                Collections.emptyList() : Arrays.asList(StringUtils.split(excludeTypeCsv, BaseConstants.Symbol.COMMA));
        // 获取树节点
        final List<WorkSpaceDTO> workSpaceList = workSpaceMapper.queryAll(organizationId, projectId, knowledgeBaseId, null, excludeTypes);
        List<WorkSpaceTreeNodeVO> nodeList =  this.buildWorkSpaceTree(organizationId, projectId, workSpaceList, expandWorkSpaceId, true);
        // 处理权限
        nodeList = nodeList.stream()
                .peek(node -> {
                    final List<PermissionCheckVO> folderCheckInfos = Arrays.stream(PermissionConstants.ActionPermission.FOLDER_ACTION_PERMISSION)
                            .map(PermissionConstants.ActionPermission::getCode)
                            .map(code -> new PermissionCheckVO().setPermissionCode(code)).collect(Collectors.toList());;
                    final List<PermissionCheckVO> documentCheckInfos = Arrays.stream(PermissionConstants.ActionPermission.DOCUMENT_ACTION_PERMISSION)
                            .map(PermissionConstants.ActionPermission::getCode)
                            .map(code -> new PermissionCheckVO().setPermissionCode(code)).collect(Collectors.toList());;
                    final List<PermissionCheckVO> fileCheckInfos = Arrays.stream(PermissionConstants.ActionPermission.FILE_ACTION_PERMISSION)
                            .map(PermissionConstants.ActionPermission::getCode)
                            .map(code -> new PermissionCheckVO().setPermissionCode(code)).collect(Collectors.toList());
                    // 特殊处理根节点
                    if(PermissionConstants.EMPTY_ID_PLACEHOLDER.equals(node.getId())) {
                        node.setPermissionCheckInfos(PermissionCheckVO.generateManagerPermission(folderCheckInfos));
                        return;
                    }
                    final String workSpaceType = node.getType();
                    final List<PermissionCheckVO> permissionCheckInfos;
                    if(WorkSpaceType.FOLDER.getValue().equals(workSpaceType)) {
                        permissionCheckInfos = folderCheckInfos;
                    } else if(WorkSpaceType.DOCUMENT.getValue().equals(workSpaceType)) {
                        permissionCheckInfos = documentCheckInfos;
                    } else if(WorkSpaceType.FILE.getValue().equals(workSpaceType)) {
                        permissionCheckInfos = fileCheckInfos;
                    } else {
                        permissionCheckInfos = Collections.emptyList();
                    }
                    // FIXME 性能问题
                    StopWatch sw = new StopWatch();
                    sw.start();
                    node.setPermissionCheckInfos(this.permissionCheckDomainService.checkPermission(
                            organizationId,
                            projectId,
                            String.valueOf(WorkSpaceType.queryPermissionTargetBaseTypeByType(workSpaceType)),
                            null,
                            node.getId(),
                            permissionCheckInfos,
                            false
                    ));
                    sw.stop();
                    System.out.println(workSpaceType);
                    System.out.println(permissionCheckInfos.size());
                    System.out.println(sw.getTotalTimeMillis());
                    System.out.println("====");
                })
                .filter(node -> PermissionCheckVO.hasAnyPermission(node.getPermissionCheckInfos()))
                .collect(Collectors.toList());
        UserInfoVO.clearCurrentUserInfo();

        // 组装结果
        return new WorkSpaceTreeVO()
                .setRootId(PermissionConstants.EMPTY_ID_PLACEHOLDER)
                .setTreeTypeCode(generateTreeTypeCode(projectId, knowledgeBase))
                .setNodeList(nodeList);
    }

    @Override
    public List<WorkSpaceVO> queryAllSpaceByOptions(Long organizationId, Long projectId, Long knowledgeBaseId, Long workSpaceId, String excludeType) {

        String type = null;
        String route = null;
        WorkSpaceDTO spaceDTO = workSpaceMapper.selectByPrimaryKey(workSpaceId);
        if (spaceDTO != null) {
            type = spaceDTO.getType();
            route = spaceDTO.getRoute();
        }
        List<String> excludeTypes = new ArrayList<>();
        if (StringUtils.isNotEmpty(excludeType) && excludeType.contains(BaseConstants.Symbol.COMMA)) {
            String[] split = excludeType.split(BaseConstants.Symbol.COMMA);
            excludeTypes = Arrays.asList(split);
        } else {
            excludeTypes.add(excludeType);
        }
//            1.「文档」支持移动或复制到「文档」或「文件夹」中；
//            2.「文件」仅支持移动或复制到「文件夹」中；
//            3.「文件夹」仅支持移动到「文件夹」中。
        List<WorkSpaceVO> result = new ArrayList<>();
        List<WorkSpaceDTO> workSpaceDTOList = workSpaceMapper.queryAll(organizationId, projectId, knowledgeBaseId, type, excludeTypes);
        //文档不能移到自己下面和自己的子集下面
        if (StringUtils.equalsIgnoreCase(type, WorkSpaceType.DOCUMENT.getValue())) {
            List<WorkSpaceDTO> workSpaceDTOS = workSpaceMapper.selectAllChildByRoute(route, false);
            workSpaceDTOS.add(spaceDTO);
            if (!CollectionUtils.isEmpty(workSpaceDTOS) && !CollectionUtils.isEmpty(workSpaceDTOList)) {
                List<Long> subIds = workSpaceDTOS.stream().map(WorkSpaceDTO::getId).collect(Collectors.toList());
                workSpaceDTOList = workSpaceDTOList.stream().filter(spaceDTO1 -> !subIds.contains(spaceDTO1.getId())).collect(Collectors.toList());
            }
        }

        if (EncryptContext.isEncrypt()) {
            for (WorkSpaceDTO w : workSpaceDTOList) {
                String r = w.getRoute();
                r = Optional.ofNullable(StringUtils.split(r, BaseConstants.Symbol.POINT))
                        .map(list -> Stream.of(list)
                                .map(str -> encryptionService.encrypt(str, StringUtils.EMPTY))
                                .collect(Collectors.joining(BaseConstants.Symbol.POINT)))
                        .orElse(null);
                w.setRoute(r);
            }
        }
        Map<Long, List<WorkSpaceVO>> groupMap = workSpaceDTOList.stream().collect(Collectors.groupingBy(
                WorkSpaceDTO::getParentId,
                Collectors.mapping(item ->
                                new WorkSpaceVO(
                                        item.getId(),
                                        item.getName(),
                                        item.getRoute(),
                                        item.getType(),
                                        CommonUtil.getFileType(item.getFileKey())
                                ),
                        Collectors.toList()
                )
        ));
        for (WorkSpaceDTO workSpaceDTO : workSpaceDTOList) {
            if (Objects.equals(workSpaceDTO.getParentId(), PermissionConstants.EMPTY_ID_PLACEHOLDER)) {
                WorkSpaceVO workSpaceVO = new WorkSpaceVO(workSpaceDTO.getId(), workSpaceDTO.getName(), workSpaceDTO.getRoute(), workSpaceDTO.getType(), CommonUtil.getFileType(workSpaceDTO.getFileKey()));
                workSpaceVO.setChildren(groupMap.get(workSpaceDTO.getId()));
                dfs(workSpaceVO, groupMap);
                result.add(workSpaceVO);
            }
        }
        return result;
    }

    @Override
    public List<WorkSpaceVO> querySpaceByIds(Long projectId, Collection<Long> workSpaceIds) {
        if (CollectionUtils.isEmpty(workSpaceIds)) {
            return Collections.emptyList();
        }
        List<WorkSpaceDTO> workSpaceDTOList = workSpaceMapper.selectSpaceByIds(projectId, workSpaceIds);
        List<WorkSpaceVO> result = new ArrayList<>();
        ProjectDTO project = iamRemoteRepository.queryProjectById(projectId);
        Long organizationId = project.getOrganizationId();
        for (WorkSpaceDTO workSpaceDTO : workSpaceDTOList) {
            WorkSpaceVO workSpaceVO = new WorkSpaceVO();
            workSpaceVO.setId(workSpaceDTO.getId());
            workSpaceVO.setName(workSpaceDTO.getName());
            workSpaceVO.setBaseId(workSpaceDTO.getBaseId());
            workSpaceVO.setFileType(CommonUtil.getFileType(workSpaceDTO.getFileKey()));
            workSpaceVO.setType(workSpaceDTO.getType());
            workSpaceVO.setBaseName(workSpaceDTO.getBaseName());
            workSpaceVO.setApprove(isApproved(organizationId, projectId, workSpaceDTO));
            result.add(workSpaceVO);
        }
        return result;
    }

    @Override
    public void checkOrganizationPermission(Long organizationId) {
        Long currentUserId = DetailsHelper.getUserDetails().getUserId();
        List<OrganizationDTO> organizations = iamRemoteRepository.listOrganizationByUserId(currentUserId);
        if (!organizations.stream().map(OrganizationDTO::getTenantId).collect(Collectors.toList()).contains(organizationId)) {
            throw new CommonException(ERROR_WORKSPACE_ILLEGAL);
        }
    }

    @Override
    public Page<WorkSpaceRecentInfoVO> recentUpdateList(Long organizationId,
                                                        Long projectId,
                                                        Long baseId,
                                                        PageRequest pageRequest) {
        KnowledgeBaseDTO knowledgeBaseDTO = this.findKnowledgeBase(organizationId, projectId, baseId);

        if (Objects.isNull(knowledgeBaseDTO)) {
            throw new CommonException(ERROR_WORKSPACE_NOTFOUND);
        }
        Long thisProjectId = knowledgeBaseDTO.getProjectId();
        Long thisOrganizationId = knowledgeBaseDTO.getOrganizationId();
        UserInfoVO userInfo = permissionRangeKnowledgeObjectSettingRepository.queryUserInfo(thisOrganizationId, thisProjectId);
        boolean hasKnowledgeBasePermission =
                permissionCheckDomainService.checkPermission(
                        organizationId,
                        projectId,
                        PermissionConstants.PermissionTargetBaseType.KNOWLEDGE_BASE.toString(),
                        null,
                        baseId,
                        PermissionConstants.ActionPermission.KNOWLEDGE_BASE_READ.getCode());
        Page<WorkSpaceRecentVO> recentPage;
        List<Integer> rowNums = new ArrayList<>();
        if (!hasKnowledgeBasePermission) {
            int maxDepth = this.selectRecentMaxDepth(thisOrganizationId, thisProjectId, baseId, false);
            for (int i = 2; i <= maxDepth; i++) {
                rowNums.add(i);
            }
        }
        recentPage = PageHelper.doPage(pageRequest, () -> workSpaceMapper.selectRecent(thisOrganizationId, thisProjectId, baseId, hasKnowledgeBasePermission, rowNums, userInfo));
        List<WorkSpaceRecentVO> recentList = recentPage.getContent();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        List<Long> userIds = recentList.stream().map(WorkSpaceRecentVO::getLastUpdatedBy).collect(Collectors.toList());
        final Map<Long, UserDO> userInfoMap = this.queryUserInfoMap(userIds);
        for (WorkSpaceRecentVO recent : recentList) {
            recent.setLastUpdatedUser(userInfoMap.get(recent.getLastUpdatedBy()));
            recent.setLastUpdateDateStr(sdf.format(recent.getLastUpdateDate()));
            recent.setBaseId(knowledgeBaseDTO.getId());
            recent.setKnowledgeBaseName(knowledgeBaseDTO.getName());
        }

        fillParentPath(recentList);
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
    public List<KnowledgeBaseTreeVO> listSystemTemplateBase(Collection<Long> knowledgeBaseIds) {
        List<WorkSpaceDTO> workSpaceDTOS = workSpaceMapper.listTemplateByBaseIds(PermissionConstants.EMPTY_ID_PLACEHOLDER, PermissionConstants.EMPTY_ID_PLACEHOLDER, knowledgeBaseIds);
        if (CollectionUtils.isEmpty(workSpaceDTOS)) {
            return new ArrayList<>();
        }
        return workSpaceDTOS.stream().map(workSpaceAssembler::dtoToTreeVO).collect(Collectors.toList());
    }

    @Override
    public boolean checkIsTemplate(Long organizationId, Long projectId, WorkSpaceDTO workSpace) {
        boolean isTemplate = false;
        if (Objects.equals(organizationId, PermissionConstants.EMPTY_ID_PLACEHOLDER) || (projectId != null && projectId.equals(PermissionConstants.EMPTY_ID_PLACEHOLDER))) {
            if (organizationId.equals(workSpace.getOrganizationId()) && projectId.equals(workSpace.getProjectId())) {
                isTemplate = true;
            } else {
                throw new CommonException(ERROR_WORKSPACE_ILLEGAL);
            }
        }
        return isTemplate;
    }

    @Override
    public List<WorkSpaceVO> listAllSpace(Long organizationId, Long projectId) {
        List<KnowledgeBaseDTO> knowledgeBases = knowledgeBaseRepository.listKnowledgeBase(organizationId, projectId);
        if (CollectionUtils.isEmpty(knowledgeBases)) {
            return Collections.emptyList();
        }
        List<KnowledgeBaseDTO> filterPermissionBases = new ArrayList<>();
        for (KnowledgeBaseDTO base : knowledgeBases) {
            boolean hasPermission =
                    permissionCheckDomainService.checkPermission(
                            organizationId,
                            projectId,
                            PermissionConstants.PermissionTargetBaseType.KNOWLEDGE_BASE.toString(),
                            null,
                            base.getId(),
                            BASE_READ,
                            false);
            if (hasPermission) {
                filterPermissionBases.add(base);
            }
        }
        List<WorkSpaceVO> result = new ArrayList<>();
        for (KnowledgeBaseDTO base : filterPermissionBases) {
            WorkSpaceVO workSpaceVO = new WorkSpaceVO(base.getId(), base.getName(), null, null, null);
            workSpaceVO.setChildren(listAllSpaceByOptions(organizationId, projectId, base.getId()));
            result.add(workSpaceVO);
        }
        // 清除用户信息缓存
        UserInfoVO.clearCurrentUserInfo();
        return result;
    }

    @Override
    public Page<WorkSpaceInfoVO> pageQueryFolder(Long organizationId, Long projectId, Long workSpaceId, PageRequest pageRequest) {
        WorkSpaceDTO workSpaceDTO = workSpaceMapper.selectByPrimaryKey(workSpaceId);
        if (workSpaceDTO == null || !StringUtils.equalsIgnoreCase(workSpaceDTO.getType(), WorkSpaceType.FOLDER.getValue())) {
            return new Page<>();
        }
        //查询该工作空间的直接子项
        Page<WorkSpaceDTO> workSpaceDTOPage = PageHelper.doPageAndSort(pageRequest, () -> workSpaceMapper.queryWorkSpaceById(organizationId, projectId, workSpaceDTO.getId()));
        if (workSpaceDTOPage == null || org.springframework.util.CollectionUtils.isEmpty(workSpaceDTOPage.getContent())) {
            return new Page<>();
        }
        Page<WorkSpaceInfoVO> workSpaceInfos = ConvertUtils.convertPage(workSpaceDTOPage, WorkSpaceInfoVO.class);
        Map<String, FileVO> longFileVOMap = getStringFileVOMap(organizationId, workSpaceInfos);
        //填充用户信息
        Set<Long> userIds = workSpaceInfos.stream().flatMap(workSpace -> Stream.of(workSpace.getCreatedBy(), workSpace.getLastUpdatedBy())).collect(Collectors.toSet());
        Map<Long, UserDO> userDOMap = this.queryUserInfoMap(userIds);
        for (WorkSpaceInfoVO workSpaceInfoVO : workSpaceInfos.getContent()) {
            fillAttribute(userDOMap, longFileVOMap, workSpaceInfoVO);
        }
        return workSpaceInfos;
    }

    @Override
    public UploadFileStatusVO queryUploadStatus(Long projectId, Long organizationId, Long refId, String sourceType) {
        List<String> refIds = new ArrayList<>();
        refIds.add(String.valueOf(refId));
        Map<String, SagaInstanceDetails> stringSagaInstanceDetailsMap = SagaInstanceUtils.listToMap(asgardRemoteRepository.queryByRefTypeAndRefIds(sourceType, refIds, KNOWLEDGE_UPLOAD_FILE));
        List<SagaInstanceDetails> sagaInstanceDetails = new ArrayList<>();
        if (!MapUtils.isEmpty(stringSagaInstanceDetailsMap) && !Objects.isNull(stringSagaInstanceDetailsMap.get(String.valueOf(refId)))) {
            sagaInstanceDetails.add(stringSagaInstanceDetailsMap.get(String.valueOf(refId)));
        }
        String sagaStatus = SagaInstanceUtils.getSagaStatus(sagaInstanceDetails);
        UploadFileStatusVO uploadFileStatusVO = new UploadFileStatusVO();
        uploadFileStatusVO.setStatus(sagaStatus);
        return uploadFileStatusVO;
    }
    
    @Override
    public List<WorkSpaceDTO> selectErrorRoute() {
        return workSpaceMapper.selectErrorRoute();
    }

    @Override
    public List<WorkSpaceDTO> selectWorkSpaceNameByIds(Collection<Long> workSpaceIds) {
        if (CollectionUtils.isEmpty(workSpaceIds)) {
            return Collections.emptyList();
        }
        return this.workSpaceMapper.selectWorkSpaceNameByIds(workSpaceIds);
    }

    @Override
    public void reloadTargetParentMappingToRedis() {
        StringBuilder builder =
                new StringBuilder(PermissionConstants.PERMISSION_CACHE_PREFIX)
                        .append(PermissionConstants.PermissionRefreshType.TARGET_PARENT.getKebabCaseName());
        String dirPath = builder.toString();
        builder.append(BaseConstants.Symbol.STAR);
        String dirRegex = builder.toString();
        Set<String> keys = redisHelper.keys(dirRegex);
        if (CollectionUtils.isNotEmpty(keys)) {
            redisHelper.delKeys(keys);
        }
        int page = 0;
        int size = 1000;
        int totalPage = 1;
        Map<Long, String> workSpaceTypeMap = new HashMap<>();
        while (page + 1 <= totalPage) {
            Page<WorkSpaceDTO> workSpacePage = PageHelper.doPage(page, size, this::selectAll);
            List<WorkSpaceDTO> workSpaceList = workSpacePage.getContent();
            for (WorkSpaceDTO workSpace : workSpaceList) {
                Long id = workSpace.getId();
                workSpaceTypeMap.put(id, workSpace.getType());
                String key = dirPath + BaseConstants.Symbol.COLON + id;
                loadTargetParentToRedis(key, workSpace, id, workSpaceTypeMap);
            }
            LOGGER.info("workspace父子级映射第【{}】页加载redis完成，共【{}】页，总计【{}】条，步长【{}】",
                    workSpacePage.getNumber() + 1,
                    workSpacePage.getTotalPages(),
                    workSpacePage.getTotalElements(),
                    size);
            totalPage = workSpacePage.getTotalPages();
            page++;
        }
    }

    @Override
    public void delTargetParentRedisCache(Long id) {
        String key = buildTargetParentCacheKey(id);
        redisHelper.delKey(key);
    }

    /**
     * redis value为list string,由根节点依此向下顺序存放
     * parentId:{@link PermissionConstants.PermissionTargetType}:{@link PermissionConstants.PermissionTargetBaseType kebabCaseName}
     *
     * @param key              redis key => knowledge:permission:target-parent:#{workSpaceId}
     * @param workSpace        文档/文件夹
     * @param id               文档/文件夹id
     * @param workSpaceTypeMap workspace 和 type映射，加速刷新缓存速度
     */
    private void loadTargetParentToRedis(String key,
                                         WorkSpaceDTO workSpace,
                                         Long id,
                                         Map<Long, String> workSpaceTypeMap) {
        if (workSpaceTypeMap == null) {
            workSpaceTypeMap = new HashMap<>();
        }
        String route = workSpace.getRoute();
        Long baseId = workSpace.getBaseId();
        Assert.notNull(route, "error.workspace.route.null." + id);
        List<String> routeList = new ArrayList<>();
        Long projectId = workSpace.getProjectId();
        PermissionConstants.PermissionTargetType permissionTargetType =
                PermissionConstants.PermissionTargetType.getPermissionTargetType(projectId, PermissionConstants.PermissionTargetBaseType.KNOWLEDGE_BASE.toString());
        Assert.notNull(permissionTargetType, BaseConstants.ErrorCode.DATA_INVALID);
        routeList.add(buildTargetParentValue(baseId, permissionTargetType));
        String regex = BaseConstants.Symbol.BACKSLASH + BaseConstants.Symbol.POINT;
        List<Long> parentIds = new ArrayList<>();
        Set<Long> selectInDbIds = new HashSet<>();
        for (String str : route.split(regex)) {
            Long parentId = Long.valueOf(str);
            if (!workSpaceTypeMap.containsKey(parentId)) {
                selectInDbIds.add(parentId);
            }
            parentIds.add(parentId);
        }
        if (!parentIds.isEmpty()) {
            if (!selectInDbIds.isEmpty()) {
                List<WorkSpaceDTO> workSpaceList = this.selectByIds(StringUtils.join(parentIds, BaseConstants.Symbol.COMMA));
                for (WorkSpaceDTO dto : workSpaceList) {
                    workSpaceTypeMap.put(dto.getId(), dto.getType());
                }
            }
            for (Long parentId : parentIds) {
                String type = workSpaceTypeMap.get(parentId);
                PermissionConstants.PermissionTargetType docType;
                boolean isFolder = WorkSpaceType.FOLDER.getValue().equals(type);
                if (isFolder) {
                    docType = PermissionConstants.PermissionTargetType.getPermissionTargetType(projectId, PermissionConstants.PermissionTargetBaseType.FOLDER.toString());
                } else {
                    docType = PermissionConstants.PermissionTargetType.getPermissionTargetType(projectId, PermissionConstants.PermissionTargetBaseType.FILE.toString());
                }
                Assert.notNull(docType, BaseConstants.ErrorCode.DATA_INVALID);
                routeList.add(buildTargetParentValue(parentId, docType));
            }
        }
        redisHelper.lstRightPushAll(key, routeList);
    }

    @Override
    public void reloadWorkSpaceTargetParent(WorkSpaceDTO workSpace) {
        Long id = workSpace.getId();
        Long baseId = workSpace.getBaseId();
        String route = workSpace.getRoute();
        Assert.notNull(id, "error.workspace.load.redis.parent.id.null");
        Assert.notNull(baseId, "error.workspace.load.redis.parent.baseId.null");
        Assert.notNull(route, "error.workspace.load.redis.parent.route.null");
        String key = buildTargetParentCacheKey(id);
        redisHelper.delKey(key);
        loadTargetParentToRedis(key, workSpace, id, null);
    }


    @Override
    public String buildTargetParentCacheKey(Long id) {
        return PermissionConstants.PERMISSION_CACHE_PREFIX +
                PermissionConstants.PermissionRefreshType.TARGET_PARENT.getKebabCaseName() +
                BaseConstants.Symbol.COLON +
                id;
    }

    /**
     * 生成缓存value
     * @param baseId                知识库ID
     * @param permissionTargetType  知识对象基础类型
     * @return                      缓存value
     */
    private String buildTargetParentValue(Long baseId, PermissionConstants.PermissionTargetType permissionTargetType) {
        StringBuilder builder = new StringBuilder();
        builder
                .append(baseId)
                .append(BaseConstants.Symbol.VERTICAL_BAR)
                .append(permissionTargetType.toString())
                .append(BaseConstants.Symbol.VERTICAL_BAR)
                .append(permissionTargetType.getBaseType().getKebabCaseName());
        return builder.toString();
    }


    @Override
    public int selectRecentMaxDepth(Long organizationId, Long projectId, Long baseId, boolean deleteFlag) {
        return Optional.ofNullable(workSpaceMapper.selectRecentMaxDepth(organizationId, projectId, baseId, deleteFlag)).orElse(0);
    }

    @Override
    public List<ImmutableTriple<Long, String, String>> findParentInfoWithCache(Long workSpaceId) {
        if(workSpaceId == null) {
            return Collections.emptyList();
        }
        final String cacheKey = this.buildTargetParentCacheKey(workSpaceId);
        final List<String> cacheResult = this.redisHelper.lstAll(cacheKey);
        if(CollectionUtils.isEmpty(cacheResult)) {
            return Collections.emptyList();
        }
        return cacheResult.stream()
                .filter(StringUtils::isNotBlank)
                .map(value -> {
                    final String[] split = StringUtils.split(value, BaseConstants.Symbol.VERTICAL_BAR);
                    if(ArrayUtils.isEmpty(split)) {
                        return ImmutableTriple.of((Long)null, (String)null, (String)null);
                    } else if(split.length <= 1) {
                        return ImmutableTriple.of(Long.parseLong(split[0]), (String)null, (String)null);
                    } else if(split.length == 2) {
                        return ImmutableTriple.of(Long.parseLong(split[0]), split[1], (String)null);
                    } else {
                        return ImmutableTriple.of(Long.parseLong(split[0]), split[1], split[2]);
                    }
                })
                .collect(Collectors.toList());
    }

    private WorkSpaceInfoVO queryFolderInfo(WorkSpaceDTO workSpaceDTO) {
        WorkSpaceInfoVO workSpaceInfoVO = new WorkSpaceInfoVO();
        workSpaceInfoVO.setWorkSpace(WorkSpaceTreeNodeVO.of(workSpaceDTO, Collections.emptyList()));
        workSpaceInfoVO.setDelete(workSpaceDTO.getDelete());
        return workSpaceInfoVO;
    }

    private WorkSpaceInfoVO queryFileInfo(Long organizationId, Long projectId, Long workSpaceId, WorkSpaceDTO workSpaceDTO) {
        WorkSpaceInfoVO file = this.queryWorkSpaceInfo(organizationId, projectId, workSpaceId, null);
        WorkSpaceDTO spaceDTO = workSpaceMapper.selectByPrimaryKey(workSpaceId);
        FileVO fileDTOByFileKey = expandFileClient.getFileDTOByFileKey(organizationId, workSpaceDTO.getFileKey());

        file.setFileType(CommonUtil.getFileType(fileDTOByFileKey.getFileKey()));
        file.setTitle(spaceDTO.getName());
        file.setUrl(fileDTOByFileKey.getFileUrl());
        file.setKey(CommonUtil.getFileId(fileDTOByFileKey.getFileKey()));

        BeanUtils.copyProperties(file, workSpaceDTO);
        file.setWorkSpace(WorkSpaceTreeNodeVO.of(workSpaceDTO, Collections.emptyList()));

        file.setPageComments(pageCommentRepository.queryByPageId(organizationId, projectId, file.getPageInfo().getId()));
        file.setDelete(workSpaceDTO.getDelete());
        return file;
    }

    private WorkSpaceInfoVO queryWorkSpaceInfoVO(Long organizationId, Long projectId, Long workSpaceId, String searchStr, WorkSpaceDTO workSpaceDTO) {
        WorkSpaceInfoVO workSpaceInfo = workSpaceMapper.queryWorkSpaceInfo(workSpaceId);
        workSpaceInfo.setWorkSpace(WorkSpaceTreeNodeVO.of(workSpaceDTO, Collections.emptyList()));
        //是否有操作的权限（用于项目层只能查看组织层文档，不能操作）
        workSpaceInfo.setIsOperate(!(workSpaceDTO.getProjectId() == null && projectId != null));
        // 处理创建人信息
        PageInfoVO pageInfo = workSpaceInfo.getPageInfo();
        List<Long> userIds = Arrays.asList(workSpaceInfo.getCreatedBy(), workSpaceInfo.getLastUpdatedBy(), pageInfo.getCreatedBy(), pageInfo.getLastUpdatedBy());
        final Map<Long, UserDO> userInfoMap = this.queryUserInfoMap(userIds);
        final UserDO createUser = userInfoMap.get(workSpaceInfo.getCreatedBy());
        workSpaceInfo.setCreateUser(createUser);
        UserDO lastUpdateUser = userInfoMap.get(workSpaceInfo.getLastUpdatedBy());
        if (lastUpdateUser == null) {
            workSpaceInfo.setLastUpdatedUser(createUser);
        } else {
            workSpaceInfo.setLastUpdatedUser(lastUpdateUser);
        }
        pageInfo.setCreateUser(userInfoMap.get(pageInfo.getCreatedBy()));
        pageInfo.setLastUpdatedUser(userInfoMap.get(pageInfo.getLastUpdatedBy()));

        handleHasDraft(workSpaceDTO.getOrganizationId(), workSpaceDTO.getProjectId(), workSpaceInfo);
        handleSearchStrHighlight(searchStr, workSpaceInfo.getPageInfo());
        setUserSettingInfo(organizationId, projectId, workSpaceInfo);
        workSpaceInfo.setPageAttachments(pageAttachmentRepository.queryByList(organizationId, projectId, workSpaceInfo.getPageInfo().getId()));
        workSpaceInfo.setPageComments(pageCommentRepository.queryByPageId(organizationId, projectId, workSpaceInfo.getPageInfo().getId()));
        workSpaceInfo.setDelete(workSpaceDTO.getDelete());
        return workSpaceInfo;
    }

    /**
     * 将知识库对象列表构建为树
     * @param organizationId        组织ID
     * @param projectId             项目ID
     * @param workSpaceList         知识库对象列表
     * @param expandWorkSpaceId     需要展开的知识库对象ID
     * @param generateVirtualRoot   生成虚拟ROOT节点
     * @return                      知识库对象树
     */
    private List<WorkSpaceTreeNodeVO> buildWorkSpaceTree(Long organizationId, Long projectId, List<WorkSpaceDTO> workSpaceList, Long expandWorkSpaceId, boolean generateVirtualRoot) {
        // wsId -> ws entity map
        Map<Long, WorkSpaceTreeNodeVO> workSpaceTreeMap = new HashMap<>(workSpaceList.size());
        // 父子ID映射Map
        Map<Long, List<Long>> groupMap = workSpaceList.stream().collect(Collectors. groupingBy(
                WorkSpaceDTO::getParentId,
                Collectors.mapping(WorkSpaceDTO::getId, Collectors.toList()))
        );
        if(generateVirtualRoot) {
            // 创建并处理虚拟根节点
            WorkSpaceDTO topSpace = new WorkSpaceDTO()
                    .setName(TOP_TITLE)
                    .setParentId(PermissionConstants.EMPTY_ID_PLACEHOLDER)
                    .setId(PermissionConstants.EMPTY_ID_PLACEHOLDER)
                    .setType(WorkSpaceType.FOLDER.getValue());
            List<Long> topChildIds = groupMap.get(PermissionConstants.EMPTY_ID_PLACEHOLDER);
            workSpaceTreeMap.put(PermissionConstants.EMPTY_ID_PLACEHOLDER, WorkSpaceTreeNodeVO.of(topSpace, topChildIds));
        }
        // 如果没有查询到任何子节点, 则进行短路操作
        if(CollectionUtils.isEmpty(workSpaceList)) {
            new ArrayList<>(workSpaceTreeMap.values());
        }
        // 准备辅助数据
        final Set<Long> userIds = workSpaceList.stream()
                .filter(spaceDTO -> StringUtils.equalsIgnoreCase(spaceDTO.getType(), WorkSpaceType.FILE.getValue()))
                .flatMap(workSpace -> Stream.of(workSpace.getCreatedBy(), workSpace.getLastUpdatedBy()))
                .collect(Collectors.toSet());
        final Map<Long, UserDO> userInfoMap = this.queryUserInfoMap(userIds);
        final Map<String, FileVO> fileInfoMap = this.queryFilInfoMap(organizationId, workSpaceList);
        // 子节点构造为Map
        for (WorkSpaceDTO workSpace : workSpaceList) {
            WorkSpaceTreeNodeVO treeNode = WorkSpaceTreeNodeVO.of(workSpace, groupMap.get(workSpace.getId()));
            workSpaceTreeMap.put(workSpace.getId(), treeNode);
            if (StringUtils.equalsIgnoreCase(treeNode.getType(), WorkSpaceType.FILE.getValue())) {
                // 封装onlyOffice预览所需要的数据
                fillFileInfo(fileInfoMap, userInfoMap, workSpace, treeNode);
            }
        }
        // 设置展开状态
        workSpaceTreeMap = this.setWorkSpaceTreeExpand(organizationId, projectId, expandWorkSpaceId, workSpaceTreeMap);
        // return
        return new ArrayList<>(workSpaceTreeMap.values());
    }

    /**
     * 根据组织ID+项目ID+知识库ID查询知识库
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @param baseId            知识库ID
     * @return                  知识库
     */
    private KnowledgeBaseDTO findKnowledgeBase(Long organizationId, Long projectId, Long baseId) {
        KnowledgeBaseDTO knowledgeBase = new KnowledgeBaseDTO();
        knowledgeBase.setOrganizationId(organizationId);
        knowledgeBase.setProjectId(projectId);
        knowledgeBase.setId(baseId);
        knowledgeBase = knowledgeBaseRepository.findKnowledgeBaseByCondition(knowledgeBase);
        return knowledgeBase;
    }

    /**
     * 获取知识库对象树类型, 组织/项目/共享
     * @param projectId     当前项目ID
     * @param knowledgeBase 当前知识库ID
     * @return              知识库对象树类型
     */
    private static String generateTreeTypeCode(Long projectId, KnowledgeBaseDTO knowledgeBase) {
        if (knowledgeBase.getProjectId() == null) {
            return WorkSpaceTreeType.ORGANIZATION.getCode();
        } else if(Objects.equals(projectId, knowledgeBase.getProjectId())){
            return WorkSpaceTreeType.PROJECT.getCode();
        } else {
            return WorkSpaceTreeType.SHARE.getCode();
        }
    }

    /**
     * 深度遍历
     * @param workSpaceVO workSpaceVO
     * @param groupMap groupMap
     */
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

    private void fillParentPath(List<WorkSpaceRecentVO> recentList) {
        recentList.forEach(workSpaceRecentVO -> {
            List<String> reParentList = new ArrayList<>();
            List<String> parentPath = getParentPath(workSpaceRecentVO.getParentId(), reParentList);
            if (org.springframework.util.CollectionUtils.isEmpty(parentPath)) {
                workSpaceRecentVO.setParentPath(Collections.emptyList());
            } else {
                Collections.reverse(parentPath);
                workSpaceRecentVO.setParentPath(parentPath);
            }
        });
    }

    private List<String> getParentPath(Long workSpaceId, List<String> reParentList) {
        if (workSpaceId.equals(PermissionConstants.EMPTY_ID_PLACEHOLDER)) {
            return reParentList;
        } else {
            WorkSpaceDTO spaceDTO = workSpaceMapper.selectByPrimaryKey(workSpaceId);
            if (spaceDTO == null) {
                return reParentList;
            }
            reParentList.add(spaceDTO.getName());
            return getParentPath(spaceDTO.getParentId(), reParentList);
        }
    }

    /**
     * 调用该方法时需要注意清理
     * @param organizationId
     * @param projectId
     * @param baseId
     * @return
     */
    private List<WorkSpaceVO> listAllSpaceByOptions(Long organizationId, Long projectId, Long baseId) {
        List<WorkSpaceVO> result = new ArrayList<>();
        List<WorkSpaceDTO> workSpaceDTOList = workSpaceMapper.queryAll(organizationId, projectId, baseId, null, null);
        List<WorkSpaceDTO> filterPermissionWorkSpaces = new ArrayList<>();
        for (WorkSpaceDTO workSpace : workSpaceDTOList) {
            Long id = workSpace.getId();
            String type = workSpace.getType();
            PermissionConstants.PermissionTargetBaseType baseType = WorkSpaceType.queryPermissionTargetBaseTypeByType(type);
            PermissionConstants.ActionPermission actionPermission = WorkSpaceType.queryReadActionByType(type);
            if (baseType == null || actionPermission == null) {
                continue;
            }
            boolean hasPermission =
                    permissionCheckDomainService.checkPermission(organizationId, projectId, baseType.toString(), null, id, actionPermission.getCode(), false);
            if (hasPermission) {
                filterPermissionWorkSpaces.add(workSpace);
            }
        }
        // 清除用户信息缓存
        UserInfoVO.clearCurrentUserInfo();
        if (EncryptContext.isEncrypt()) {
            filterPermissionWorkSpaces.forEach(w -> {
                String route = w.getRoute();
                route = Optional.ofNullable(StringUtils.split(route, BaseConstants.Symbol.POINT))
                        .map(list -> Stream.of(list)
                                .map(str -> encryptionService.encrypt(str, ""))
                                .collect(Collectors.joining(BaseConstants.Symbol.POINT)))
                        .orElse(null);
                w.setRoute(route);
            });
        }
        Map<Long, List<WorkSpaceVO>> groupMap = filterPermissionWorkSpaces.stream().collect(Collectors.
                groupingBy(
                        WorkSpaceDTO::getParentId,
                        Collectors.mapping(item ->
                                        new WorkSpaceVO(
                                                item.getId(),
                                                item.getName(),
                                                item.getRoute(),
                                                item.getType(),
                                                CommonUtil.getFileType(item.getFileKey())
                                        ),
                                Collectors.toList()
                        )
                )
        );
        for (WorkSpaceDTO workSpaceDTO : filterPermissionWorkSpaces) {
            if (Objects.equals(workSpaceDTO.getParentId(), 0L)) {
                WorkSpaceVO workSpaceVO = new WorkSpaceVO(workSpaceDTO.getId(), workSpaceDTO.getName(), workSpaceDTO.getRoute(), workSpaceDTO.getType(), CommonUtil.getFileType(workSpaceDTO.getFileKey()));
                workSpaceVO.setChildren(groupMap.get(workSpaceDTO.getId()));
                dfs(workSpaceVO, groupMap);
                result.add(workSpaceVO);
            }
        }
        return result;
    }

    /**
     * 判断是否有草稿数据
     *
     * @param organizationId organizationId
     * @param projectId      projectId
     * @param workSpaceInfo  workSpaceInfo
     */
    private void handleHasDraft(Long organizationId, Long projectId, WorkSpaceInfoVO workSpaceInfo) {
        PageContentDTO draft = pageRepository.queryDraftContent(organizationId, projectId, workSpaceInfo.getPageInfo().getId());
        if (draft != null) {
            workSpaceInfo.setHasDraft(true);
            workSpaceInfo.setCreateDraftDate(draft.getLastUpdateDate());
        } else {
            workSpaceInfo.setHasDraft(false);
        }
    }

    /**
     * 应用于全文检索，根据检索内容高亮内容
     *
     * @param searchStr searchStr
     * @param pageInfo  pageInfo
     */
    private void handleSearchStrHighlight(String searchStr, PageInfoVO pageInfo) {
        if (searchStr != null && !"".equals(searchStr)) {
            String highlightContent = esRestUtil.highlightContent(searchStr, pageInfo.getContent());
            pageInfo.setHighlightContent(highlightContent != null && !highlightContent.equals("") ? highlightContent : pageInfo.getContent());
        }
    }

    private void setUserSettingInfo(Long organizationId, Long projectId, WorkSpaceInfoVO workSpaceInfoVO) {
        CustomUserDetails customUserDetails = DetailsHelper.getUserDetails();
        if (customUserDetails == null) {
            return;
        }
        Long userId = customUserDetails.getUserId();
        List<UserSettingDTO> userSettingDTOList = userSettingRepository.selectByOption(organizationId, projectId, SETTING_TYPE_EDIT_MODE, userId);
        if (userSettingDTOList.size() == 1) {
            workSpaceInfoVO.setUserSettingVO(modelMapper.map(userSettingDTOList.get(0), UserSettingVO.class));
        }
    }

    /**
     * 获取更新人和创建人信息
     * @param userIds   用户ID集合
     * @return          更新人和创建人信息 userId -> userDO
     */
    private Map<Long, UserDO> queryUserInfoMap(Collection<Long> userIds) {
        if(CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        final List<UserDO> userInfoList = iamRemoteRepository.listUsersByIds(userIds, false);
        if (CollectionUtils.isEmpty(userInfoList)) {
            return Collections.emptyMap();
        }
        return userInfoList.stream().collect(Collectors.toMap(UserDO::getId, Function.identity()));
    }


    /**
     * 获取文件信息
     * @param organizationId    组织ID
     * @param workSpaceList     知识库对象列表
     * @return                  文件信息 fileKey -> 文件信息 map
     */
    private Map<String, FileVO> queryFilInfoMap(Long organizationId, List<WorkSpaceDTO> workSpaceList) {
        if(CollectionUtils.isEmpty(workSpaceList)) {
            return Collections.emptyMap();
        }
        List<String> fileKeys = workSpaceList.stream()
                .filter(spaceDTO -> StringUtils.equalsIgnoreCase(spaceDTO.getType(), WorkSpaceType.FILE.getValue()))
                .map(WorkSpaceDTO::getFileKey)
                .collect(Collectors.toList());
        List<FileVO> fileInfoList = expandFileClient.queryFileDTOByFileKeys(organizationId, fileKeys);
        if (CollectionUtils.isEmpty(fileInfoList)) {
            return Collections.emptyMap();
        }
        return fileInfoList.stream().collect(Collectors.toMap(FileVO::getFileKey, Function.identity()));
    }

    /**
     * 设置知识库对象树的展开状态
     * @param organizationId        组织ID
     * @param projectId             项目ID
     * @param expandWorkSpaceId     展开的知识库对象ID
     * @param workSpaceTreeMap      知识库对象树节点Map
     * @return                      处理后的知识库对象树节点Map
     */
    private Map<Long, WorkSpaceTreeNodeVO> setWorkSpaceTreeExpand(Long organizationId, Long projectId, Long expandWorkSpaceId, Map<Long, WorkSpaceTreeNodeVO> workSpaceTreeMap) {
        if (expandWorkSpaceId == null || PermissionConstants.EMPTY_ID_PLACEHOLDER.equals(expandWorkSpaceId)) {
            return workSpaceTreeMap;
        }
        // 找到展开的部分的末级节点
        WorkSpaceDTO expandWorkSpaceRoot = this.baseQueryById(organizationId, projectId, expandWorkSpaceId);
        if(expandWorkSpaceRoot == null) {
            return workSpaceTreeMap;
        }
        // 设置上级节点展开
        List<Long> expandIds = Stream.of(StringUtils.split(expandWorkSpaceRoot.getRoute(), BaseConstants.Symbol.POINT))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        for (Long expandId : expandIds) {
            WorkSpaceTreeNodeVO treeVO = workSpaceTreeMap.get(expandId);
            if (treeVO != null) {
                treeVO.setIsExpanded(true);
            }
        }
        // 设置末级节点收起
        WorkSpaceTreeNodeVO treeNode = workSpaceTreeMap.get(expandWorkSpaceId);
        if (treeNode != null) {
            treeNode.setIsExpanded(false);
            treeNode.setIsClick(true);
        }

        return workSpaceTreeMap;
    }

    private void fillFileInfo(Map<String, FileVO> fileVOMap, Map<Long, UserDO> userDOMap, WorkSpaceDTO workSpaceDTO, WorkSpaceTreeNodeVO treeVO) {
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
            workSpaceInfoVO.setSubFiles((long) files.size());
            workSpaceInfoVO.setSubDocuments((long) documents.size());
            workSpaceInfoVO.setSubFolders((long) folders.size());
        }
    }

    private Boolean isApproved(Long organizationId, Long projectId, WorkSpaceDTO workSpaceDTO) {
        Long id = workSpaceDTO.getId();
        String type = workSpaceDTO.getType();
        PermissionConstants.PermissionTargetBaseType baseType = WorkSpaceType.queryPermissionTargetBaseTypeByType(type);
        if (baseType == null) {
            return false;
        } else {
            PermissionConstants.ActionPermission actionPermission = WorkSpaceType.queryReadActionByType(type);
            if (actionPermission == null) {
                return false;
            }
            return permissionCheckDomainService.checkPermission(organizationId, projectId, baseType.toString(), null, id, actionPermission.getCode());
        }
    }
}
