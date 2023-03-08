package io.choerodon.kb.app.service.impl;

import static io.choerodon.kb.infra.enums.PermissionConstants.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import io.choerodon.kb.infra.enums.OpenRangeType;
import org.hzero.mybatis.domian.Condition;
import org.hzero.mybatis.util.Sqls;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.kb.api.vo.InitKnowledgeBaseTemplateVO;
import io.choerodon.kb.api.vo.KnowledgeBaseInfoVO;
import io.choerodon.kb.api.vo.PageCreateVO;
import io.choerodon.kb.api.vo.ProjectDTO;
import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.choerodon.kb.app.service.DataFixService;
import io.choerodon.kb.app.service.KnowledgeBaseService;
import io.choerodon.kb.app.service.KnowledgeBaseTemplateService;
import io.choerodon.kb.app.service.PageService;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.repository.IamRemoteRepository;
import io.choerodon.kb.domain.repository.KnowledgeBaseRepository;
import io.choerodon.kb.domain.repository.PermissionRangeKnowledgeObjectSettingRepository;
import io.choerodon.kb.domain.repository.WorkSpaceRepository;
import io.choerodon.kb.domain.service.PermissionRangeKnowledgeBaseSettingService;
import io.choerodon.kb.domain.service.PermissionRangeKnowledgeObjectSettingService;
import io.choerodon.kb.domain.service.PermissionRefreshCacheDomainService;
import io.choerodon.kb.infra.dto.KnowledgeBaseDTO;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.enums.WorkSpaceType;
import io.choerodon.kb.infra.feign.vo.OrganizationSimplifyDTO;
import io.choerodon.kb.infra.mapper.WorkSpaceMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import org.hzero.core.base.AopProxy;
import org.hzero.core.base.BaseConstants;


/**
 * DataFixService 实现类
 *
 * @author 25499 2020/1/6 18:05
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class DataFixServiceImpl implements DataFixService, AopProxy<DataFixServiceImpl> {
    private static final Logger logger = LoggerFactory.getLogger(DataFixServiceImpl.class);

    @Autowired
    private WorkSpaceMapper workSpaceMapper;
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;
    @Autowired
    private IamRemoteRepository iamRemoteRepository;
    @Autowired
    private PageService pageService;

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private KnowledgeBaseTemplateService knowledgeBaseTemplateService;
    @Autowired
    private WorkSpaceRepository workSpaceRepository;

    @Autowired
    private KnowledgeBaseRepository knowledgeBaseRepository;
    @Autowired
    private PermissionRangeKnowledgeObjectSettingService permissionRangeKnowledgeObjectSettingService;
    @Autowired
    private PermissionRangeKnowledgeBaseSettingService permissionRangeKnowledgeBaseSettingService;
    @Autowired
    private PermissionRangeKnowledgeObjectSettingRepository permissionRangeKnowledgeObjectSettingRepository;
    @Autowired
    private PermissionRefreshCacheDomainService permissionRefreshCacheDomainService;
    private static final int SIZE = 1000;

    @Override
    @Async
    public void fixData() {
        logger.info("==============================>>>>>>>> Data Fix Start <<<<<<<<=================================");
        //1.修复组织workspace
        fixOrgWorkSpace();
        //2.修复项目workspace
        fixProWorkSpace();
        //3.initKnowledgeBaseTemplate
        initKnowledgeBaseTemplate();
        logger.info("==========================>>>>>>>> Data Fix Succeed!!! FINISHED!!! <<<<<<<<========================");
    }

    @Override
    public void fixWorkspaceRoute() {
        logger.info("======================进行知识库路由错误数据修复=====================");
        // 1. 查询所有错误数据 非顶层的且数据有错误的(路由中不含父级id的数据)
        List<WorkSpaceDTO> errorWorkSpaces = workSpaceRepository.selectErrorRoute();
        // 2. 递归查询父级至顶层（因为有父级数据也错误的数据）
        if (CollectionUtils.isEmpty(errorWorkSpaces)) {
            return;
        }
        for (WorkSpaceDTO workSpaceDTO : errorWorkSpaces) {
            List<Long> parentIds = Lists.newArrayList();
            // 按照 1.2.3.4
            assembleParentIds(parentIds, workSpaceDTO.getParentId());
            parentIds.add(workSpaceDTO.getId());
            // 3. 组装路由，更新数据
            String route = Joiner.on(BaseConstants.Symbol.POINT).join(parentIds);
            workSpaceDTO.setRoute(route);
            workSpaceRepository.updateOptional(workSpaceDTO, WorkSpaceDTO.FIELD_ROUTE);
        }
        logger.info("======================进行知识库路由错误数据完成=====================");
    }

    @Override
    public void fixPermission() {
        //修复组织层默认权限
        fixOrganizationDefaultPermission();
        //修复知识库安全设置
        fixKnowledgeBaseSecurityConfig();
        //修复workspace安全设置
        fixWorkspaceSecurityConfig();
        // 刷新缓存
        for (PermissionRefreshType value : PermissionRefreshType.values()) {
            permissionRefreshCacheDomainService.refreshCache(value);
        }
    }

    @Override
    public void fixWorkSpaceTemplate() {
        // organization_id为空, 修数据
        fixWorkSpaceOrgIdIsNULL();
        // 修复平台预置的知识库
        fixDefaultKnowledgeBase();
        // 旧数据中的组织知识库模板，统一修复成组织级场景化模板，每个库生成一个模板kb_knowledge_base，名字叫${原名字}-场景化模板，然后将原来的模板的base_id指向新创建的模板库
        fixOrgWorkSpaceTemplate();
        // 旧数据中的项目知识库模板，统一修复成项目级场景化模板，每个库生成一个模板kb_knowledge_base，名字叫${原名字}-场景化模板，然后将原来的模板的base_id指向新创建的模板库
        fixProjectWorkSpaceTemplate();
    }

    private void fixDefaultKnowledgeBase() {
        // 查询workSpace的预置模板数据
        List<WorkSpaceDTO> workSpaceDTOS = workSpaceRepository.selectByCondition(Condition.builder(WorkSpaceDTO.class)
                .where(Sqls.custom()
                        .andEqualTo(WorkSpaceDTO.ORGANIZATION_ID, 0L)
                        .andEqualTo(WorkSpaceDTO.PROJECT_ID, 0L))
                .build());
        if (CollectionUtils.isEmpty(workSpaceDTOS)) {
            return;
        }
        List<WorkSpaceDTO> spaceDTOS = workSpaceDTOS.stream().filter(workSpaceDTO -> workSpaceDTO.getBaseId() == 0L).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(spaceDTOS)) {
            return;
        }
        KnowledgeBaseDTO knowledgeBase = new KnowledgeBaseDTO();
        knowledgeBase.setProjectId(0L);
        knowledgeBase.setOrganizationId(0L);
        knowledgeBase.setInitCompletionFlag(true);
        knowledgeBase.setName("研发");
        knowledgeBase.setTemplateFlag(true);
        knowledgeBase.setOpenRange(OpenRangeType.RANGE_PRIVATE.getType());
        knowledgeBase.setPublishFlag(true);
        KnowledgeBaseDTO knowledgeBaseDTO = knowledgeBaseService.baseInsert(knowledgeBase);
        spaceDTOS.forEach(workSpaceDTO -> {
            workSpaceDTO.setBaseId(knowledgeBaseDTO.getId());
        });
        workSpaceRepository.batchUpdateByPrimaryKey(spaceDTOS);
    }


    private void fixOrganizationDefaultPermission() {
        logger.info("======================开始修复组织层默认权限=====================");
        int page = 0;
        int size = SIZE;
        int totalPage = 1;
        int currentRow = 1;
        while (true) {
            if (page + 1 > totalPage) {
                break;
            }
            Page<OrganizationSimplifyDTO> organizationPage = iamRemoteRepository.pageOrganizations(page, size);
            logger.info("组织总计【{}】条，共【{}】页，当前第【{}】页，步长【{}】", organizationPage.getTotalElements(), organizationPage.getTotalPages(), organizationPage.getNumber() + 1, size);
            List<OrganizationSimplifyDTO> organizations = organizationPage.getContent();
            for (OrganizationSimplifyDTO org : organizations) {
                Long organizationId = org.getTenantId();
                PermissionRange range = new PermissionRange();
                range.setOrganizationId(organizationId);
                if (permissionRangeKnowledgeObjectSettingRepository.select(range).isEmpty()) {
                    String name = org.getTenantName();
                    logger.info("初始化第【{}】个组织【{}】的默认组织设置", currentRow, name);
                    permissionRangeKnowledgeBaseSettingService.initPermissionRangeOnOrganizationCreate(organizationId);
                }
                currentRow++;
            }
            totalPage = organizationPage.getTotalPages();
            page++;
        }
        logger.info("======================组织层默认权限修复完成=====================");
    }

    private void fixWorkspaceSecurityConfig() {
        logger.info("======================开始修复文档安全设置=====================");
        int page = 0;
        int size = SIZE;
        int totalPage = 1;
        while (true) {
            if (page + 1 > totalPage) {
                break;
            }
            PageRequest pageRequest = new PageRequest(page, size);
            Page<WorkSpaceDTO> workSpacePage = PageHelper.doPage(pageRequest, () -> workSpaceRepository.selectAll());
            logger.info("文档总计【{}】条，共【{}】页，当前第【{}】页，步长【{}】", workSpacePage.getTotalElements(), workSpacePage.getTotalPages(), workSpacePage.getNumber() + 1, size);
            List<WorkSpaceDTO> workSpaceList = workSpacePage.getContent();
            Map<Long, Long> projectOrgIdMap = queryProjectOrgMap(workSpaceList);
            for (WorkSpaceDTO workspace : workSpaceList) {
                Long id = workspace.getId();
                Long projectId = workspace.getProjectId();
                Long organizationId = workspace.getOrganizationId();
                if (organizationId == null) {
                    organizationId = projectOrgIdMap.get(projectId);
                }
                boolean skipFlag = ObjectUtils.isEmpty(projectId) && ObjectUtils.isEmpty(organizationId);
                if (skipFlag) {
                    continue;
                }
                String type = workspace.getType();
                String baseTargetType = PermissionTargetBaseType.FILE.toString();
                if (WorkSpaceType.FOLDER.equals(WorkSpaceType.of(type))) {
                    baseTargetType = PermissionTargetBaseType.FOLDER.toString();
                }
                PermissionTargetType permissionTargetType =
                        PermissionTargetType.getPermissionTargetType(projectId, baseTargetType);
                PermissionDetailVO permissionDetailVO =
                        PermissionDetailVO.of(permissionTargetType.toString(), id, null, null);
                permissionDetailVO.setBaseTargetType(baseTargetType);
                permissionRangeKnowledgeObjectSettingService.saveRangeAndSecurity(organizationId, projectId, permissionDetailVO, false);
            }
            totalPage = workSpacePage.getTotalPages();
            page++;
        }
        logger.info("======================文档安全设置修复完成=====================");
    }

    private Map<Long, Long> queryProjectOrgMap(List<WorkSpaceDTO> workSpaceList) {
        Map<Long, Long> projectOrgIdMap = new HashMap<>();
        Set<Long> projectIds = new HashSet<>();
        for (WorkSpaceDTO workspace : workSpaceList) {
            Long projectId = workspace.getProjectId();
            Long organizationId = workspace.getOrganizationId();
            if (!ObjectUtils.isEmpty(projectId) && ObjectUtils.isEmpty(organizationId)) {
                projectIds.add(projectId);
            }
        }
        if (!projectIds.isEmpty()) {
            projectOrgIdMap.putAll(
                    iamRemoteRepository.queryProjectByIds(projectIds)
                            .stream()
                            .collect(Collectors.toMap(ProjectDTO::getId, ProjectDTO::getOrganizationId)));
        }
        return projectOrgIdMap;
    }

    private void fixKnowledgeBaseSecurityConfig() {
        logger.info("======================开始修复知识库安全设置=====================");
        int page = 0;
        int size = SIZE;
        int totalPage = 1;
        while (true) {
            if (page + 1 > totalPage) {
                break;
            }
            PageRequest pageRequest = new PageRequest(page, size);
            Page<KnowledgeBaseDTO> knowledgeBasePage = PageHelper.doPage(pageRequest, () -> knowledgeBaseRepository.selectAll());
            logger.info("知识库总计【{}】条，共【{}】页，当前第【{}】页，步长【{}】", knowledgeBasePage.getTotalElements(), knowledgeBasePage.getTotalPages(), knowledgeBasePage.getNumber() + 1, size);
            List<KnowledgeBaseDTO> knowledgeBaseList = knowledgeBasePage.getContent();
            for (KnowledgeBaseDTO knowledgeBase : knowledgeBaseList) {
                Long id = knowledgeBase.getId();
                Long projectId = knowledgeBase.getProjectId();
                Long organizationId = knowledgeBase.getOrganizationId();
                String baseTargetType = PermissionTargetBaseType.KNOWLEDGE_BASE.toString();
                PermissionTargetType permissionTargetType =
                        PermissionTargetType.getPermissionTargetType(projectId, baseTargetType);
                //组织/项目下公开
                PermissionRange permissionRange =
                        PermissionRange.of(
                                organizationId,
                                projectId,
                                permissionTargetType.toString(),
                                id,
                                PermissionRangeType.PUBLIC.toString(),
                                0L,
                                PermissionRole.MANAGER);
                List<PermissionRange> permissionRanges = new ArrayList<>();
                permissionRanges.add(permissionRange);
                PermissionDetailVO permissionDetailVO =
                        PermissionDetailVO.of(permissionTargetType.toString(), id, permissionRanges, null);
                permissionDetailVO.setBaseTargetType(baseTargetType);
                permissionRangeKnowledgeObjectSettingService.saveRangeAndSecurity(organizationId, projectId, permissionDetailVO, false);
            }
            totalPage = knowledgeBasePage.getTotalPages();
            page++;
        }
        logger.info("======================知识库安全设置修复完成=====================");
    }

    /**
     * 递归查询路由id
     *
     * @param parentIds 需要组装的list
     * @param id        parentId
     */
    protected void assembleParentIds(List<Long> parentIds, Long id) {
        WorkSpaceDTO parentWS = workSpaceRepository.selectByPrimaryKey(id);
        if (parentWS.getParentId() != 0L) {
            assembleParentIds(parentIds, parentWS.getParentId());
        }
        parentIds.add(id);
    }

    private void fixOrgWorkSpace() {
        //组织
        Page<OrganizationSimplifyDTO> pageResult = iamRemoteRepository.pageOrganizations(0, 0);
        List<OrganizationSimplifyDTO> organizationSimplifyDTOS = pageResult.getContent();
        logger.info("=======================>>>fix.organization.count::{}===============>>>", organizationSimplifyDTOS.size());
        if (!CollectionUtils.isEmpty(organizationSimplifyDTOS)) {
            organizationSimplifyDTOS.forEach(e -> {
                KnowledgeBaseInfoVO knowledgeBaseInfoVO = new KnowledgeBaseInfoVO();
                knowledgeBaseInfoVO.setDescription("组织下默认知识库");
                knowledgeBaseInfoVO.setName(e.getTenantName());
                knowledgeBaseInfoVO.setOpenRange("range_public");
                KnowledgeBaseInfoVO baseInfoVO = knowledgeBaseService.create(e.getTenantId(), null, knowledgeBaseInfoVO, true);
                workSpaceMapper.updateWorkSpace(e.getTenantId(), null, baseInfoVO.getId());
            });
        }
    }

    private void fixProWorkSpace() {
        List<ProjectDTO> projectDTOS = iamRemoteRepository.getAllProjects();
        logger.info("=======================>>>fix.project.count::{}===============>>>", projectDTOS.size());
        if (!CollectionUtils.isEmpty(projectDTOS)) {
            projectDTOS.forEach(e -> {
                KnowledgeBaseInfoVO knowledgeBaseInfoVO = new KnowledgeBaseInfoVO();
                knowledgeBaseInfoVO.setDescription("项目下默认知识库");
                knowledgeBaseInfoVO.setName(e.getName());
                knowledgeBaseInfoVO.setOpenRange("range_private");
                KnowledgeBaseInfoVO baseInfoVO = knowledgeBaseService.create(e.getOrganizationId(), e.getId(), knowledgeBaseInfoVO, true);
                workSpaceMapper.updateWorkSpace(e.getOrganizationId(), e.getId(), baseInfoVO.getId());
                workSpaceMapper.updateWorkSpace(null, e.getId(), baseInfoVO.getId());
            });
        }
    }

    private void initKnowledgeBaseTemplate() {
        List<InitKnowledgeBaseTemplateVO> list = knowledgeBaseTemplateService.buildInitData();
        logger.info("=======================>>>Init knowledgeBaseTemplate:{}", list.size());
        if (!CollectionUtils.isEmpty(list)) {
            list.forEach(v -> {
                v.setOpenRange("range_public");
                // 创建知识库
                KnowledgeBaseInfoVO knowledgeBaseInfoVO = knowledgeBaseService.create(0L, 0L, modelMapper.map(v, KnowledgeBaseInfoVO.class), true);
                List<PageCreateVO> templatePage = v.getTemplatePage();
                if (!CollectionUtils.isEmpty(templatePage)) {
                    // 创建知识库下面的模板
                    templatePage.forEach(pageCreateVO -> {
                        pageCreateVO.setBaseId(knowledgeBaseInfoVO.getId());
                        pageService.createPageWithContent(0L, 0L, pageCreateVO, true);
                    });
                }
            });
        }
    }


    private void fixWorkSpaceOrgIdIsNULL() {
        List<WorkSpaceDTO> workSpaceDTOS = workSpaceRepository.selectByCondition(Condition.builder(WorkSpaceDTO.class)
                .where(Sqls.custom()
                        .andIsNull(WorkSpaceDTO.ORGANIZATION_ID))
                .build());
        if (!CollectionUtils.isEmpty(workSpaceDTOS)) {
            Set<Long> projectIds = workSpaceDTOS.stream().map(WorkSpaceDTO::getProjectId).collect(Collectors.toSet());
            List<ProjectDTO> projectDTOS = iamRemoteRepository.queryProjectByIds(projectIds);
            Map<Long, ProjectDTO> longProjectDTOMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(projectDTOS)) {
                longProjectDTOMap = projectDTOS.stream().collect(Collectors.toMap(ProjectDTO::getId, Function.identity()));
            }
            Map<Long, ProjectDTO> finalLongProjectDTOMap = longProjectDTOMap;
            workSpaceDTOS.forEach(workSpaceDTO -> {
                ProjectDTO projectDTO = finalLongProjectDTOMap.get(workSpaceDTO.getProjectId());
                if (!Objects.isNull(projectDTO)) {
                    workSpaceDTO.setOrganizationId(projectDTO.getOrganizationId());
                    workSpaceRepository.updateByPrimaryKey(workSpaceDTO);
                }
            });
        }
    }

    private void fixOrgWorkSpaceTemplate() {
        List<WorkSpaceDTO> orgWorkSpaceDTOS = workSpaceRepository.selectByCondition(Condition.builder(WorkSpaceDTO.class)
                .where(Sqls.custom()
                        .andNotEqualTo(WorkSpaceDTO.ORGANIZATION_ID, 0L)
                        .andEqualTo(WorkSpaceDTO.PROJECT_ID, 0l))
                .build());
        if (CollectionUtils.isEmpty(orgWorkSpaceDTOS)) {
            return;
        }
        handWorkSpaceTemplate(orgWorkSpaceDTOS);
    }

    private void fixProjectWorkSpaceTemplate() {
        List<WorkSpaceDTO> projectWorkSpaceDTOS = workSpaceRepository.selectByCondition(Condition.builder(WorkSpaceDTO.class)
                .where(Sqls.custom()
                        .andEqualTo(WorkSpaceDTO.ORGANIZATION_ID, 0L)
                        .andNotEqualTo(WorkSpaceDTO.PROJECT_ID, 0l))
                .build());
        if (CollectionUtils.isEmpty(projectWorkSpaceDTOS)) {
            return;
        }
        handWorkSpaceTemplate(projectWorkSpaceDTOS);
    }


    private void handWorkSpaceTemplate(List<WorkSpaceDTO> projectWorkSpaceDTOS) {
        Map<Long, List<WorkSpaceDTO>> listMap = projectWorkSpaceDTOS.stream().collect(Collectors.groupingBy(WorkSpaceDTO::getBaseId));
        listMap.forEach((baseId, workSpaceDTOS) -> {
            KnowledgeBaseDTO knowledgeBaseDTO = knowledgeBaseRepository.selectByPrimaryKey(baseId);
            if (knowledgeBaseDTO == null) {
                return;
            }
            // project_id = null 为组织层知识库，project_id != null 为项目层知识库
            KnowledgeBaseDTO template = new KnowledgeBaseDTO();
            template.setName(knowledgeBaseDTO.getName() + "-场景化模板");
            template.setTemplateFlag(true);
            template.setDelete(false);
            template.setOrganizationId(knowledgeBaseDTO.getOrganizationId());
            template.setProjectId(knowledgeBaseDTO.getProjectId());

            template.setOpenRange(OpenRangeType.RANGE_PRIVATE.getType());
            template.setPublishFlag(true);

            KnowledgeBaseDTO knowledgeBaseTemplate = knowledgeBaseService.createKnowledgeBaseTemplate(template);
            updateWorkSpaceTemplate(workSpaceDTOS, knowledgeBaseTemplate);
        });
    }

    private void updateWorkSpaceTemplate(List<WorkSpaceDTO> workSpaceDTOS, KnowledgeBaseDTO knowledgeBaseTemplate) {
        workSpaceDTOS.forEach(workSpaceDTO -> {
            workSpaceDTO.setBaseId(knowledgeBaseTemplate.getId());
            workSpaceDTO.setTemplateFlag(true);
        });
        workSpaceRepository.batchUpdateOptional(workSpaceDTOS, WorkSpaceDTO.BASE_ID);
    }
}
