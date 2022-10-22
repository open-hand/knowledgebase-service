package io.choerodon.kb.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
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
import io.choerodon.kb.app.service.*;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.repository.*;
import io.choerodon.kb.domain.service.PermissionRangeKnowledgeBaseSettingService;
import io.choerodon.kb.domain.service.PermissionRangeKnowledgeObjectSettingService;
import io.choerodon.kb.infra.dto.KnowledgeBaseDTO;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.enums.WorkSpaceType;
import io.choerodon.kb.infra.feign.vo.OrganizationSimplifyDTO;
import io.choerodon.kb.infra.mapper.WorkSpaceMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import org.hzero.core.base.AopProxy;
import org.hzero.core.base.BaseConstants;

import static io.choerodon.kb.infra.enums.PermissionConstants.PermissionTargetBaseType;
import static io.choerodon.kb.infra.enums.PermissionConstants.PermissionRole;
import static io.choerodon.kb.infra.enums.PermissionConstants.PermissionRangeType;

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
                PermissionDetailVO permissionDetailVO =
                        PermissionDetailVO.of(baseTargetType, id, null, null);
                permissionDetailVO.setBaseTargetType(baseTargetType);
                permissionRangeKnowledgeObjectSettingService.saveRangeAndSecurity(organizationId, projectId, permissionDetailVO);
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
                //组织/项目下公开
                PermissionRange permissionRange =
                        PermissionRange.of(
                                organizationId,
                                projectId,
                                baseTargetType,
                                id,
                                PermissionRangeType.PUBLIC.toString(),
                                0L,
                                PermissionRole.MANAGER);
                List<PermissionRange> permissionRanges=  new ArrayList<>();
                permissionRanges.add(permissionRange);
                PermissionDetailVO permissionDetailVO =
                        PermissionDetailVO.of(baseTargetType, id, permissionRanges, null);
                permissionDetailVO.setBaseTargetType(baseTargetType);
                permissionRangeKnowledgeObjectSettingService.saveRangeAndSecurity(organizationId, projectId, permissionDetailVO);
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

}
