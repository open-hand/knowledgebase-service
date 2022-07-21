package io.choerodon.kb.app.service.impl;

import java.util.List;

import io.choerodon.kb.app.service.KnowledgeBaseTemplateService;
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
import io.choerodon.kb.app.service.PageService;
import io.choerodon.kb.infra.feign.BaseFeignClient;
import io.choerodon.kb.infra.feign.vo.OrganizationSimplifyDTO;
import io.choerodon.kb.infra.mapper.WorkSpaceMapper;

/**
 * @author: 25499
 * @date: 2020/1/6 18:05
 * @description:
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class DataFixServiceImpl implements DataFixService {
    private static final Logger logger = LoggerFactory.getLogger(DataFixServiceImpl.class);

    @Autowired
    private WorkSpaceMapper workSpaceMapper;
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;
    @Autowired
    private BaseFeignClient baseFeignClient;
    @Autowired
    private PageService pageService;

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private KnowledgeBaseTemplateService knowledgeBaseTemplateService;

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

    private void fixOrgWorkSpace() {
        //组织
        Page<OrganizationSimplifyDTO> pageResult = baseFeignClient.getAllOrgsList(0, 0).getBody();
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
        List<ProjectDTO> projectDTOS = baseFeignClient.getAllProList().getBody();
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
