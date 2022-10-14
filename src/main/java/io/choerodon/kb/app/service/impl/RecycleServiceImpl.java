package io.choerodon.kb.app.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.kb.api.vo.RecycleVO;
import io.choerodon.kb.api.vo.SearchDTO;
import io.choerodon.kb.app.service.KnowledgeBaseService;
import io.choerodon.kb.app.service.RecycleService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.app.service.assembler.KnowledgeBaseAssembler;
import io.choerodon.kb.domain.entity.UserInfo;
import io.choerodon.kb.domain.repository.PermissionRangeKnowledgeObjectSettingRepository;
import io.choerodon.kb.domain.repository.WorkSpaceRepository;
import io.choerodon.kb.infra.enums.WorkSpaceType;
import io.choerodon.kb.infra.mapper.KnowledgeBaseMapper;
import io.choerodon.kb.infra.mapper.WorkSpaceMapper;
import io.choerodon.kb.infra.utils.PageUtils;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * @author: 25499
 * @date: 2020/1/3 10:25
 * @description:
 */
@Service
public class RecycleServiceImpl implements RecycleService {
    @Autowired
    private KnowledgeBaseMapper knowledgeBaseMapper;
    private static final String TYPE_PAGE = "page";
    private static final String TYPE_TEMPLATE = "template";
    private static final String TYPE_BASE = "base";
    private static final String SEARCH_TYPE = "type";

    @Autowired
    private WorkSpaceMapper workSpaceMapper;
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;
    @Autowired
    private WorkSpaceService workSpaceService;
    @Autowired
    private KnowledgeBaseAssembler knowledgeBaseAssembler;
    @Autowired
    private PermissionRangeKnowledgeObjectSettingRepository permissionRangeKnowledgeObjectSettingRepository;
    @Autowired
    private WorkSpaceRepository workSpaceRepository;

    @Override
    public void restoreWorkSpaceAndPage(Long organizationId, Long projectId, String type, Long id, Long baseId) {
        if (TYPE_BASE.equals(type)) {
            knowledgeBaseService.restoreKnowledgeBase(organizationId, projectId, id);
        }
        List<String> workSpaceType = new ArrayList<>();
        WorkSpaceType[] values = WorkSpaceType.values();
        for (WorkSpaceType value : values) {
            workSpaceType.add(value.getValue());
        }
        if (TYPE_PAGE.equals(type)
                || TYPE_TEMPLATE.equals(type)
                || workSpaceType.contains(type)) {
            workSpaceService.restoreWorkSpaceAndPage(organizationId, projectId, id, baseId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWorkSpaceAndPage(Long organizationId, Long projectId, String type, Long id) {
        if (TYPE_BASE.equals(type)) {
            knowledgeBaseService.deleteKnowledgeBase(organizationId, projectId, id);
            return;
        }
        List<String> workSpaceType = new ArrayList<>();
        WorkSpaceType[] values = WorkSpaceType.values();
        for (WorkSpaceType value : values) {
            workSpaceType.add(value.getValue());
        }
        if (TYPE_PAGE.equals(type) || TYPE_TEMPLATE.equals(type)
                || workSpaceType.contains(type)) {
            workSpaceService.deleteWorkSpaceAndPage(organizationId, projectId, id);
        }
    }


    @Override
    public Page<RecycleVO> pageList(Long projectId, Long organizationId, PageRequest pageRequest, SearchDTO searchDTO) {
        List<RecycleVO> recycleList = new ArrayList<>();
        List<Integer> rowNums = new ArrayList<>();
        UserInfo userInfo = permissionRangeKnowledgeObjectSettingRepository.queryUserInfo(organizationId, projectId);
        int maxDepth = workSpaceRepository.selectRecentMaxDepth(organizationId, projectId, null, true);
        for (int i = 2; i <= maxDepth; i++) {
            rowNums.add(i);
        }
        if (!ObjectUtils.isEmpty(searchDTO.getSearchArgs()) && TYPE_BASE.equals(searchDTO.getSearchArgs().get(SEARCH_TYPE))) {
            recycleList = knowledgeBaseMapper.queryAllDetele(organizationId, projectId, searchDTO, userInfo, userInfo.getAdminFlag());
            recycleList.forEach(e -> e.setType(TYPE_BASE));
        } else if (!ObjectUtils.isEmpty(searchDTO.getSearchArgs()) && TYPE_PAGE.equals(searchDTO.getSearchArgs().get(SEARCH_TYPE))) {
            recycleList = workSpaceMapper.queryAllDeleteOptions(organizationId, projectId, searchDTO, userInfo, rowNums, userInfo.getAdminFlag());
            recycleList.forEach(e -> e.setType(TYPE_PAGE));
        } else if (!ObjectUtils.isEmpty(searchDTO.getSearchArgs()) && TYPE_TEMPLATE.equals(searchDTO.getSearchArgs().get(SEARCH_TYPE))) {
            queryTemplate(projectId, organizationId, searchDTO);
        } else {
            recycleList = knowledgeBaseMapper.queryAllDetele(organizationId, projectId, searchDTO, userInfo, userInfo.getAdminFlag());
            recycleList.forEach(e -> e.setType(TYPE_BASE));
            List<RecycleVO> recyclePageList = workSpaceMapper.queryAllDeleteOptions(organizationId, projectId, searchDTO, userInfo, rowNums, userInfo.getAdminFlag());
            recyclePageList.forEach(e -> e.setType(TYPE_PAGE));
            recycleList.addAll(recyclePageList);
            List<RecycleVO> templates = queryTemplate(projectId, organizationId, searchDTO);
            recycleList.addAll(templates);
        }

        knowledgeBaseAssembler.handleUserInfo(recycleList);
        recycleList.sort(Comparator.comparing(RecycleVO::getLastUpdateDate).reversed());
        return PageUtils.createPageFromList(recycleList, pageRequest);
    }

    private List<RecycleVO> queryTemplate(Long projectId, Long organizationId, SearchDTO searchDTO) {
        List<RecycleVO> templates = new ArrayList<>();
        if (organizationId != null && projectId != null) {
            templates = workSpaceMapper.queryAllDeleteOptions(0L, projectId, searchDTO, null, new ArrayList<>(), true);
        }
        if (organizationId != null && projectId == null) {
            templates = workSpaceMapper.queryAllDeleteOptions(organizationId, 0L, searchDTO, null, new ArrayList<>(), true);
        }
        templates.forEach(e -> e.setType(TYPE_TEMPLATE));
        return templates;
    }
}
