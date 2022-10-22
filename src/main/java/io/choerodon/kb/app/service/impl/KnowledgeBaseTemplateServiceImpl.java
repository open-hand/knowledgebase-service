package io.choerodon.kb.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.vo.InitKnowledgeBaseTemplateVO;
import io.choerodon.kb.api.vo.KnowledgeBaseInfoVO;
import io.choerodon.kb.api.vo.PageCreateVO;
import io.choerodon.kb.app.service.KnowledgeBaseService;
import io.choerodon.kb.app.service.PageService;
import io.choerodon.kb.app.service.KnowledgeBaseTemplateService;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.enums.WorkSpaceType;
import io.choerodon.kb.infra.mapper.WorkSpaceMapper;
import io.choerodon.kb.infra.utils.HtmlUtil;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhaotianxin
 * @date 2021/11/23 10:01
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class KnowledgeBaseTemplateServiceImpl implements KnowledgeBaseTemplateService {
    private static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseTemplateServiceImpl.class);
    @Autowired
    private WorkSpaceMapper workSpaceMapper;
    @Autowired
    private PageService pageService;
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public void initWorkSpaceTemplate() {
        // 查询当前数据库系统预置的模版是否存在
        WorkSpaceDTO workSpaceDTO = new WorkSpaceDTO();
        workSpaceDTO.setOrganizationId(0L);
        workSpaceDTO.setProjectId(0L);
        List<WorkSpaceDTO> workSpaceDTOS = workSpaceMapper.select(workSpaceDTO);
        if(CollectionUtils.isEmpty(workSpaceDTOS)){
            initTemplate();
        }
    }

    private void initTemplate() {
        List<InitKnowledgeBaseTemplateVO> list = this.buildInitData();
        logger.info("=======================>>>Init knowledgeBaseTemplate:{}", list.size());
        if (!CollectionUtils.isEmpty(list)) {
            list.forEach((v) -> {
                v.setOpenRange("range_public");
                KnowledgeBaseInfoVO knowledgeBaseInfoVO = this.knowledgeBaseService.create(0L, 0L, (KnowledgeBaseInfoVO)this.modelMapper.map(v, KnowledgeBaseInfoVO.class), true);
                List<PageCreateVO> templatePage = v.getTemplatePage();
                if (!CollectionUtils.isEmpty(templatePage)) {
                    templatePage.forEach((pageCreateVO) -> {
                        pageCreateVO.setBaseId(knowledgeBaseInfoVO.getId());
                        pageService.createPageWithContent(0L, 0L, pageCreateVO, true);
                    });
                }
            });
        }

    }

    @Override
    public List<InitKnowledgeBaseTemplateVO> buildInitData() {
        List<InitKnowledgeBaseTemplateVO> list = new ArrayList<>();
        try {
            String template = HtmlUtil.loadHtmlTemplate("/htmlTemplate/InitTemplate.html");
            String[] split = template.split("<div/>");
            InitKnowledgeBaseTemplateVO knowledgeBaseTemplateA = new InitKnowledgeBaseTemplateVO();
            knowledgeBaseTemplateA.setName("会议记录");
            List<PageCreateVO> pageCreateVOSA = new ArrayList<>();
            String meetingMinutes = "记录重大会议的参会情况狂，以及会议内容、会议讨论输出的内容。";
            String productPlan = "产品规划会产出当前迭代的完成任务项，以及下个迭代预计进行的任务项。";
            String reviewConference = "敏捷过程中的一个重要会议，总结陈述迭代进行的优缺点，以及对应的整改方案。";
            pageCreateVOSA.add(new PageCreateVO(0L, "会议纪要", meetingMinutes, split[2], WorkSpaceType.DOCUMENT.getValue()));
            pageCreateVOSA.add(new PageCreateVO(0L, "产品规划会", productPlan, split[4], WorkSpaceType.DOCUMENT.getValue()));
            pageCreateVOSA.add(new PageCreateVO(0L, "敏捷迭代回顾会议", reviewConference, split[5], WorkSpaceType.DOCUMENT.getValue()));
            knowledgeBaseTemplateA.setTemplatePage(pageCreateVOSA);
            list.add(knowledgeBaseTemplateA);

            InitKnowledgeBaseTemplateVO knowledgeBaseTemplateB = new InitKnowledgeBaseTemplateVO();
            knowledgeBaseTemplateB.setName("产品研发");
            List<PageCreateVO> pageCreateVOSB = new ArrayList<>();
            String prdDescription = "PRD可以将产品设计思路清晰的展现给团队人员，便于他们快速理解产品，同时可以记录需求的变更历史，以便于快速了解功能的变化。";
            String technicalDocuments = "产品开发过程中所用框架的说明，接口设计说明，帮助前后端开发人员快速了解技术相关的设计。";
            String competitiveAnalysis = "包含对市场的分析，竞品情况的了解，分析出自己产品的优劣势，产出分析结果和应对建议。";
            pageCreateVOSB.add(new PageCreateVO(0L, "技术文档", technicalDocuments, split[1], WorkSpaceType.DOCUMENT.getValue()));
            pageCreateVOSB.add(new PageCreateVO(0L, "竞品分析", competitiveAnalysis, split[3], WorkSpaceType.DOCUMENT.getValue()));
            pageCreateVOSB.add(new PageCreateVO(0L, "产品需求文档PRD", prdDescription, split[0], WorkSpaceType.DOCUMENT.getValue()));
            knowledgeBaseTemplateB.setTemplatePage(pageCreateVOSB);
            list.add(knowledgeBaseTemplateB);

            InitKnowledgeBaseTemplateVO knowledgeBaseTemplateC = new InitKnowledgeBaseTemplateVO();
            knowledgeBaseTemplateC.setName("产品测试");
            List<PageCreateVO> pageCreateVOSC = new ArrayList<>();
            String testPlan = "根据产品质量等级结合产品研发现状，确定测试范围、确定测试需求、制定测试策略、确定测试方法、确定测试资源、制定测试风险应对方案、评估测试交付件，预估迭代功能测试和SIT/UAT测试的工作量，进行人员和进度的安排。";
            pageCreateVOSC.add(new PageCreateVO(0L, "产品测试计划", testPlan, split[6], WorkSpaceType.DOCUMENT.getValue()));
            knowledgeBaseTemplateC.setTemplatePage(pageCreateVOSC);
            list.add(knowledgeBaseTemplateC);
        } catch (IOException e) {
            throw new CommonException(e);
        }
        return list;
    }
}
