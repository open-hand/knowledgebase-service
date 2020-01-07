package io.choerodon.kb.app.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.vo.InitKnowledgeBaseTemplateVO;
import io.choerodon.kb.api.vo.KnowledgeBaseInfoVO;
import io.choerodon.kb.api.vo.PageCreateVO;
import io.choerodon.kb.app.service.DataMigrateService;
import io.choerodon.kb.app.service.KnowledgeBaseService;
import io.choerodon.kb.app.service.PageService;
import io.choerodon.kb.infra.utils.HtmlUtil;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.HtmlUtils;

/**
 * @author: 25499
 * @date: 2020/1/6 18:05
 * @description:
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class DataMigrateServiceImpl implements DataMigrateService {
    private final static Logger logger = LoggerFactory.getLogger(DataMigrateServiceImpl.class);
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Autowired
    private PageService pageService;

    @Autowired
    private ModelMapper modelMapper;
    @Override
    public void migrateWorkSpace(){
        // 创建初始化模板
        initKnowledgeBaseTemplate();
    }

    private void initKnowledgeBaseTemplate() {
        List<InitKnowledgeBaseTemplateVO>  list = buildInitData();
        logger.info("=======================>>>Init knowledgeBaseTemplate:{}", list.size());
        if (!CollectionUtils.isEmpty(list)) {
            list.forEach(v -> {
                v .setOpenRange("range_public");
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

    private List<InitKnowledgeBaseTemplateVO>  buildInitData()  {
        List<InitKnowledgeBaseTemplateVO>  list = new ArrayList<>();
        try{
            String template = HtmlUtil.loadHtmlTemplate("/htmlTemplate/initTemplate.html");
            String[] split = template.split("<div/>");
            InitKnowledgeBaseTemplateVO knowledgeBaseTemplateA = new InitKnowledgeBaseTemplateVO();
            knowledgeBaseTemplateA.setName("模板库A");
            List<PageCreateVO> pageCreateVOSA = new ArrayList<>();
            pageCreateVOSA.add(new PageCreateVO(0L,"产品需求文档PRD", split[0]));
            pageCreateVOSA.add(new PageCreateVO(0L,"会议纪要",split[1]));
            knowledgeBaseTemplateA.setTemplatePage(pageCreateVOSA);
            list.add(knowledgeBaseTemplateA);

            InitKnowledgeBaseTemplateVO knowledgeBaseTemplateB = new InitKnowledgeBaseTemplateVO();
            knowledgeBaseTemplateB.setName("模板库B");
            List<PageCreateVO> pageCreateVOSB = new ArrayList<>();
            pageCreateVOSB.add(new PageCreateVO(0L,"技术文档",split[2]));
            pageCreateVOSB.add(new PageCreateVO(0L,"竞品分析",split[3]));
            knowledgeBaseTemplateB.setTemplatePage(pageCreateVOSB);
            list.add(knowledgeBaseTemplateB);

            InitKnowledgeBaseTemplateVO knowledgeBaseTemplateC = new InitKnowledgeBaseTemplateVO();
            knowledgeBaseTemplateC.setName("模板库C");
            List<PageCreateVO> pageCreateVOSC = new ArrayList<>();
            pageCreateVOSC.add(new PageCreateVO(0L,"产品规划",split[4]));
            knowledgeBaseTemplateC.setTemplatePage(pageCreateVOSC);
            list.add(knowledgeBaseTemplateC);
        }catch (IOException e){
          throw new CommonException(e);
        }
      return  list;
    }


}
