package io.choerodon.kb.app.service.impl;

import java.util.List;

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

import io.choerodon.core.domain.Page;
import io.choerodon.kb.api.vo.InitKnowledgeBaseTemplateVO;
import io.choerodon.kb.api.vo.KnowledgeBaseInfoVO;
import io.choerodon.kb.api.vo.PageCreateVO;
import io.choerodon.kb.api.vo.ProjectDTO;
import io.choerodon.kb.app.service.DataFixService;
import io.choerodon.kb.app.service.KnowledgeBaseService;
import io.choerodon.kb.app.service.KnowledgeBaseTemplateService;
import io.choerodon.kb.app.service.PageService;
import io.choerodon.kb.domain.repository.IamRemoteRepository;
import io.choerodon.kb.domain.repository.WorkSpaceRepository;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.feign.vo.OrganizationSimplifyDTO;
import io.choerodon.kb.infra.mapper.WorkSpaceMapper;

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
                KnowledgeBaseInfoVO baseInfoVO = knowledgeBaseService.create(e.getTenantId(), null, knowledgeBaseInfoVO);
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
                KnowledgeBaseInfoVO baseInfoVO = knowledgeBaseService.create(e.getOrganizationId(), e.getId(), knowledgeBaseInfoVO);
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
                KnowledgeBaseInfoVO knowledgeBaseInfoVO = knowledgeBaseService.create(0L, 0L, modelMapper.map(v, KnowledgeBaseInfoVO.class));
                List<PageCreateVO> templatePage = v.getTemplatePage();
                if (!CollectionUtils.isEmpty(templatePage)) {
                    // 创建知识库下面的模板
                    templatePage.forEach(pageCreateVO -> {
                        pageCreateVO.setBaseId(knowledgeBaseInfoVO.getId());
                        pageService.createPageWithContent(0L, 0L, pageCreateVO);
                    });
                }
            });
        }
    }

}
