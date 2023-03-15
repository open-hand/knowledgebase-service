package io.choerodon.kb.app.service.impl;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.api.vo.*;
import io.choerodon.kb.app.service.*;
import io.choerodon.kb.domain.repository.KnowledgeBaseRepository;
import io.choerodon.kb.domain.repository.WorkSpaceRepository;
import io.choerodon.kb.infra.dto.KnowledgeBaseDTO;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.enums.PlatformTemplateCategory;
import io.choerodon.kb.infra.enums.WorkSpaceType;
import io.choerodon.kb.infra.mapper.WorkSpaceMapper;
import io.choerodon.kb.infra.utils.HtmlUtil;

import org.hzero.core.base.BaseConstants;
import org.hzero.core.redis.RedisHelper;
import org.hzero.core.util.JsonUtils;
import org.hzero.websocket.helper.SocketSendHelper;

/**
 * @author zhaotianxin
 * @date 2021/11/23 10:01
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class KnowledgeBaseTemplateServiceImpl implements KnowledgeBaseTemplateService {
    private static final Long ROOT_ID = BaseConstants.DEFAULT_TENANT_ID;
    private static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseTemplateServiceImpl.class);

    @Autowired
    private WorkSpaceMapper workSpaceMapper;
    @Autowired
    private PageService pageService;
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private WorkSpaceRepository workSpaceRepository;
    @Autowired
    private WorkSpaceService workSpaceService;
    @Autowired
    private SocketSendHelper socketSendHelper;
    @Autowired
    private KnowledgeBaseRepository knowledgeBaseRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RecycleService recycleService;
    @Autowired
    private RedisHelper redisHelper;

    @Override
    public void initWorkSpaceTemplate() {
        // 查询当前数据库系统预置的模版是否存在
        WorkSpaceDTO workSpaceDTO = new WorkSpaceDTO();
        workSpaceDTO.setOrganizationId(0L);
        workSpaceDTO.setProjectId(0L);
        List<WorkSpaceDTO> workSpaceDTOS = workSpaceMapper.select(workSpaceDTO);
        if (CollectionUtils.isEmpty(workSpaceDTOS)) {
            initTemplate(false);
        }
    }

    private void initTemplate(boolean skipKnowledgeBase) {
        List<InitKnowledgeBaseTemplateVO> list = this.buildInitData();
        logger.info("=======================>>>Init knowledgeBaseTemplate:{}", list.size());
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (InitKnowledgeBaseTemplateVO initVO : list) {
            initVO.setOpenRange("range_public");
            Long baseId;
            if (!skipKnowledgeBase) {
                KnowledgeBaseInfoVO knowledgeBaseInfoVO = this.knowledgeBaseService.create(0L, 0L, this.modelMapper.map(initVO, KnowledgeBaseInfoVO.class), false);
                baseId = knowledgeBaseInfoVO.getId();
            } else {
                baseId = BaseConstants.DEFAULT_TENANT_ID;
            }
            List<PageCreateVO> templatePage = initVO.getTemplatePage();
            if (CollectionUtils.isNotEmpty(templatePage)) {
                for (PageCreateVO pageCreateVO : templatePage) {
                    pageCreateVO.setBaseId(baseId);
                    pageService.createPageWithContent(0L, 0L, pageCreateVO, false);
                }
            }
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
            pageCreateVOSA.add(
                    new PageCreateVO(0L, "会议纪要", meetingMinutes, split[2], WorkSpaceType.DOCUMENT.getValue())
                            .setTemplateFlag(true)
                            .setTemplateCategory(PlatformTemplateCategory.DEVELOP_MANAGER.toString()));
            pageCreateVOSA.add(
                    new PageCreateVO(0L, "产品规划会", productPlan, split[4], WorkSpaceType.DOCUMENT.getValue())
                            .setTemplateFlag(true)
                            .setTemplateCategory(PlatformTemplateCategory.DEVELOP_MANAGER.toString()));
            pageCreateVOSA.add(
                    new PageCreateVO(0L, "敏捷迭代回顾会议", reviewConference, split[5], WorkSpaceType.DOCUMENT.getValue())
                            .setTemplateFlag(true)
                            .setTemplateCategory(PlatformTemplateCategory.DEVELOP_MANAGER.toString()));
            knowledgeBaseTemplateA.setTemplatePage(pageCreateVOSA);
            list.add(knowledgeBaseTemplateA);

            InitKnowledgeBaseTemplateVO knowledgeBaseTemplateB = new InitKnowledgeBaseTemplateVO();
            knowledgeBaseTemplateB.setName("产品研发");
            List<PageCreateVO> pageCreateVOSB = new ArrayList<>();
            String prdDescription = "PRD可以将产品设计思路清晰的展现给团队人员，便于他们快速理解产品，同时可以记录需求的变更历史，以便于快速了解功能的变化。";
            String technicalDocuments = "产品开发过程中所用框架的说明，接口设计说明，帮助前后端开发人员快速了解技术相关的设计。";
            String competitiveAnalysis = "包含对市场的分析，竞品情况的了解，分析出自己产品的优劣势，产出分析结果和应对建议。";
            pageCreateVOSB.add(
                    new PageCreateVO(0L, "技术文档", technicalDocuments, split[1], WorkSpaceType.DOCUMENT.getValue())
                            .setTemplateFlag(true)
                            .setTemplateCategory(PlatformTemplateCategory.DEVELOP_MANAGER.toString()));
            pageCreateVOSB.add(
                    new PageCreateVO(0L, "竞品分析", competitiveAnalysis, split[3], WorkSpaceType.DOCUMENT.getValue())
                            .setTemplateFlag(true)
                            .setTemplateCategory(PlatformTemplateCategory.DEVELOP_MANAGER.toString()));
            pageCreateVOSB.add(
                    new PageCreateVO(0L, "产品需求文档PRD", prdDescription, split[0], WorkSpaceType.DOCUMENT.getValue())
                            .setTemplateFlag(true)
                            .setTemplateCategory(PlatformTemplateCategory.DEVELOP_MANAGER.toString()));
            knowledgeBaseTemplateB.setTemplatePage(pageCreateVOSB);
            list.add(knowledgeBaseTemplateB);

            InitKnowledgeBaseTemplateVO knowledgeBaseTemplateC = new InitKnowledgeBaseTemplateVO();
            knowledgeBaseTemplateC.setName("产品测试");
            List<PageCreateVO> pageCreateVOSC = new ArrayList<>();
            String testPlan = "根据产品质量等级结合产品研发现状，确定测试范围、确定测试需求、制定测试策略、确定测试方法、确定测试资源、制定测试风险应对方案、评估测试交付件，预估迭代功能测试和SIT/UAT测试的工作量，进行人员和进度的安排。";
            pageCreateVOSC.add(
                    new PageCreateVO(0L, "产品测试计划", testPlan, split[6], WorkSpaceType.DOCUMENT.getValue())
                            .setTemplateFlag(true)
                            .setTemplateCategory(PlatformTemplateCategory.DEVELOP_MANAGER.toString()));
            knowledgeBaseTemplateC.setTemplatePage(pageCreateVOSC);
            list.add(knowledgeBaseTemplateC);
        } catch (IOException e) {
            throw new CommonException(e);
        }
        return list;
    }

    @Override
    public void initPlatformDocTemplate() {
        WorkSpaceDTO workSpace = new WorkSpaceDTO();
        workSpace.setProjectId(BaseConstants.DEFAULT_TENANT_ID);
        workSpace.setOrganizationId(BaseConstants.DEFAULT_TENANT_ID);
        if (workSpaceMapper.select(workSpace).isEmpty()) {
            //为空，需要初始化
            initTemplate(true);
        }
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void copyKnowledgeBaseFromTemplate(Long organizationId,
                                              Long projectId,
                                              KnowledgeBaseInfoVO knowledgeBaseInfoVO,
                                              Long knowledgeBaseId,
                                              Long targetWorkSpaceId,
                                              boolean createKnowledgeBase) {
        Set<Long> templateBaseIds = knowledgeBaseInfoVO.getTemplateBaseIds();
        Set<Long> templateWorkSpaceIds = knowledgeBaseInfoVO.getTemplateWorkSpaceIds();
        String uuid = knowledgeBaseInfoVO.getUuid();
        KnowledgeBaseInitProgress progress = new KnowledgeBaseInitProgress(knowledgeBaseId, uuid);
        progress.setKnowledgeBaseId(knowledgeBaseId);
        if (CollectionUtils.isEmpty(templateBaseIds) && CollectionUtils.isEmpty(templateWorkSpaceIds)) {
            //发送成功消息
            sendMsgByStatus(progress, KnowledgeBaseInitProgress.Status.SUCCEED.toString().toLowerCase());
            return;
        }
        try {
            List<WorkSpaceDTO> workSpaces = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(templateBaseIds)) {
                workSpaces.addAll(workSpaceRepository.listByKnowledgeBaseIds(templateBaseIds));
            }
            if (CollectionUtils.isNotEmpty(templateWorkSpaceIds)) {
                workSpaces.addAll(workSpaceRepository.selectByIds(StringUtils.join(templateWorkSpaceIds, BaseConstants.Symbol.COMMA)));
            }
            if (CollectionUtils.isEmpty(workSpaces)) {
                //发送成功消息
                sendMsgByStatus(progress, KnowledgeBaseInitProgress.Status.SUCCEED.toString().toLowerCase());
                return;
            }
            if (createKnowledgeBase) {
                //设置为为初始化状态
                updateInitCompletionFlag(knowledgeBaseId, false);
            }
            progress.setTotal(workSpaces.size());
            Map<Long, List<WorkSpaceDTO>> workSpaceMap =
                    workSpaces.stream().collect(Collectors.groupingBy(WorkSpaceDTO::getBaseId));
            for (Map.Entry<Long, List<WorkSpaceDTO>> entry : workSpaceMap.entrySet()) {
                List<WorkSpaceDTO> workSpaceList = entry.getValue();
                List<WorkSpaceTreeNodeVO> nodeList = workSpaceRepository.buildWorkSpaceTree(organizationId, projectId, workSpaceList, null);
                Map<Long, WorkSpaceTreeNodeVO> treeMap = nodeList.stream().collect(Collectors.toMap(WorkSpaceTreeNodeVO::getId, Function.identity()));
                Map<Long, Long> oldParentAndNewParentMapping = initOldParentAndNewParentMapping(targetWorkSpaceId);
                cloneWorkSpace(ROOT_ID, treeMap, organizationId, projectId, knowledgeBaseId, progress, oldParentAndNewParentMapping);
            }
            //复制结束，设置初始化成功
            if (createKnowledgeBase) {
                updateInitCompletionFlag(knowledgeBaseId, true);
            }
        } catch (Exception e) {
            //如果有异常，则回滚，删除知识库
            if (createKnowledgeBase) {
                knowledgeBaseService.removeKnowledgeBase(organizationId, projectId, knowledgeBaseId);
                recycleService.deleteWorkSpaceAndPage(organizationId, projectId, "base", knowledgeBaseId);
            }
            sendMsgByStatus(progress, KnowledgeBaseInitProgress.Status.FAILED.toString().toLowerCase());
            logger.error("copy from template base error: ", e);
        }
        sendMsgByStatus(progress, KnowledgeBaseInitProgress.Status.SUCCEED.toString().toLowerCase());
    }

    private Map<Long, Long> initOldParentAndNewParentMapping(Long targetWorkSpaceId) {
        //如果目标对象为文件夹，则初始化在文件夹下面，否则放到根目录下
        Long dirId = 0L;
        Map<Long, Long> oldParentAndNewParentMapping = new HashMap<>();
        if (targetWorkSpaceId != null) {
            WorkSpaceDTO workSpaceDTO = workSpaceRepository.selectByPrimaryKey(targetWorkSpaceId);
            if (workSpaceDTO != null && WorkSpaceType.FOLDER.getValue().equals(workSpaceDTO.getType())) {
                dirId = targetWorkSpaceId;
            }
        }
        oldParentAndNewParentMapping.put(0L, dirId);
        return oldParentAndNewParentMapping;
    }

    private void updateInitCompletionFlag(Long knowledgeBaseId, boolean initCompletionFlag) {
        KnowledgeBaseDTO knowledgeBase = knowledgeBaseRepository.selectByPrimaryKey(knowledgeBaseId);
        knowledgeBase.setInitCompletionFlag(initCompletionFlag);
        knowledgeBaseRepository.updateByPrimaryKeySelective(knowledgeBase);
    }

    private void sendMsgByStatus(KnowledgeBaseInitProgress progress,
                                 String status) {
        progress.setProgress(100D);
        progress.setStatus(status);
        sendMsgAndSaveRedis(progress);
    }

    private void cloneWorkSpace(Long workSpaceId,
                                Map<Long, WorkSpaceTreeNodeVO> treeMap,
                                Long organizationId,
                                Long projectId,
                                Long knowledgeBaseId,
                                KnowledgeBaseInitProgress progress,
                                Map<Long, Long> oldParentAndNewParentMapping) {
        WorkSpaceTreeNodeVO node = treeMap.get(workSpaceId);
        if (node == null) {
            return;
        }
        boolean isRoot = ROOT_ID.equals(workSpaceId);
        if (!isRoot) {
            Long oldParentId = node.getParentId();
            Long newParentId = oldParentAndNewParentMapping.get(oldParentId);
            if (newParentId == null) {
                throw new CommonException("error.clone.parent.id.null");
            }
            //clone
            String type = node.getType();
            if (WorkSpaceType.FOLDER.getValue().equals(type)) {
                WorkSpaceInfoVO workSpaceInfo = workSpaceService.cloneFolder(organizationId, projectId, workSpaceId, newParentId, knowledgeBaseId);
                oldParentAndNewParentMapping.put(workSpaceId, workSpaceInfo.getWorkSpace().getId());
            } else {
                WorkSpaceInfoVO workSpaceInfo = workSpaceService.clonePage(organizationId, projectId, workSpaceId, newParentId, knowledgeBaseId);
                oldParentAndNewParentMapping.put(workSpaceId, workSpaceInfo.getId());
            }
            boolean sendMsg = progress.increasePointer();
            if (sendMsg) {
                sendMsgAndSaveRedis(progress);
            }
        }
        List<Long> children = node.getChildren();
        if (CollectionUtils.isNotEmpty(children)) {
            for (Long childId : children) {
                cloneWorkSpace(childId, treeMap, organizationId, projectId, knowledgeBaseId, progress, oldParentAndNewParentMapping);
            }
        }
    }

    private void sendMsgAndSaveRedis(KnowledgeBaseInitProgress progress) {
        String message = null;
        try {
            message = objectMapper.writeValueAsString(progress);
        } catch (JsonProcessingException e) {
            throw new CommonException("object to json error", e);
        }
        Long userId = DetailsHelper.getUserDetails().getUserId();
        String websocketKey = progress.getWebsocketKey();
        socketSendHelper.sendByUserId(userId, websocketKey, message);
        String uuid = progress.getUuid();
        String redisKey = userId + BaseConstants.Symbol.COLON + uuid;
        redisHelper.strSet(redisKey, JsonUtils.toJson(progress), 1L, TimeUnit.DAYS);
    }
}
