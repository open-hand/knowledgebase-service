package io.choerodon.kb.app.service.impl;

import static org.hzero.core.base.BaseConstants.ErrorCode.FORBIDDEN;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.core.domain.Page;
import io.choerodon.kb.api.vo.RecycleVO;
import io.choerodon.kb.api.vo.SearchDTO;
import io.choerodon.kb.api.vo.permission.UserInfoVO;
import io.choerodon.kb.app.service.KnowledgeBaseService;
import io.choerodon.kb.app.service.RecycleService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.app.service.assembler.KnowledgeBaseAssembler;
import io.choerodon.kb.domain.repository.PermissionRangeKnowledgeObjectSettingRepository;
import io.choerodon.kb.domain.repository.WorkSpaceRepository;
import io.choerodon.kb.domain.service.PermissionCheckDomainService;
import io.choerodon.kb.infra.enums.PermissionConstants;
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
    @Autowired
    private PermissionCheckDomainService permissionCheckDomainService;

    @Override
    public void restoreWorkSpaceAndPage(Long organizationId, Long projectId, String type, Long id, Long baseId) {
        if (TYPE_BASE.equals(type)) {
            Assert.isTrue(permissionCheckDomainService.checkPermission(organizationId,
                    projectId,
                    PermissionConstants.PermissionTargetBaseType.KNOWLEDGE_BASE.toString(),
                    null,
                    id,
                    PermissionConstants.ActionPermission.KNOWLEDGE_BASE_RECOVER.getCode()), FORBIDDEN);
            knowledgeBaseService.restoreKnowledgeBase(organizationId, projectId, id);
        }
        Set<String> workSpaceType = Arrays.stream(WorkSpaceType.values()).map(WorkSpaceType::getValue).collect(Collectors.toSet());
        if (TYPE_PAGE.equals(type) || TYPE_TEMPLATE.equals(type) || workSpaceType.contains(type)) {
            workSpaceService.restoreWorkSpaceAndPage(organizationId, projectId, id, baseId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWorkSpaceAndPage(Long organizationId, Long projectId, String type, Long id) {
        if (TYPE_BASE.equals(type)) {
            Assert.isTrue(permissionCheckDomainService.checkPermission(organizationId,
                    projectId,
                    PermissionConstants.PermissionTargetBaseType.KNOWLEDGE_BASE.toString(),
                    null,
                    id,
                    PermissionConstants.ActionPermission.KNOWLEDGE_BASE_DELETE.getCode()), FORBIDDEN);
            knowledgeBaseService.deleteKnowledgeBase(organizationId, projectId, id);
            return;
        }
        Set<String> workSpaceType = Arrays.stream(WorkSpaceType.values()).map(WorkSpaceType::getValue).collect(Collectors.toSet());
        if (TYPE_PAGE.equals(type) || TYPE_TEMPLATE.equals(type) || workSpaceType.contains(type)) {
            workSpaceService.deleteWorkSpaceAndPage(organizationId, projectId, id);
        }
    }


    @Override
    public Page<RecycleVO> pageList(Long projectId, Long organizationId, PageRequest pageRequest, SearchDTO searchDTO) {
        final List<RecycleVO> recycles = new ArrayList<>();
        final UserInfoVO userInfo = permissionRangeKnowledgeObjectSettingRepository.queryUserInfo(organizationId, projectId);

        final Map<String, Object> searchArgs = searchDTO.getSearchArgs();
        final Object searchType = Optional.ofNullable(searchArgs).map(sa -> sa.get(SEARCH_TYPE)).orElse(null);
        if (searchArgs == null || TYPE_BASE.equals(searchType)) {
            final List<RecycleVO> knowledgeBaseRecycles = knowledgeBaseMapper.queryAllDelete(organizationId, projectId, searchDTO, userInfo, userInfo.getAdminFlag());
            knowledgeBaseRecycles.forEach(e -> e.setType(TYPE_BASE));
            recycles.addAll(knowledgeBaseRecycles);
        }
        if (searchArgs == null || TYPE_PAGE.equals(searchType)) {
            final List<Integer> rowNums = new ArrayList<>();
            int maxDepth = workSpaceRepository.selectRecentMaxDepth(organizationId, projectId, null, true);
            for (int i = 2; i <= maxDepth; i++) {
                rowNums.add(i);
            }
            final List<RecycleVO> workSpaceRecycles = workSpaceMapper.queryAllDeleteOptions(organizationId, projectId, searchDTO, userInfo, rowNums, userInfo.getAdminFlag());
            recycles.addAll(workSpaceRecycles);
        }
        if (searchArgs == null || TYPE_TEMPLATE.equals(searchType)) {
            final List<RecycleVO> templateRecycles = queryTemplate(projectId, organizationId, searchDTO);
            recycles.addAll(templateRecycles);
        }

        recycles.sort(Comparator.comparing(RecycleVO::getLastUpdateDate).reversed());
        final Page<RecycleVO> pageResult = PageUtils.createPageFromList(recycles, pageRequest);
        knowledgeBaseAssembler.handleUserInfo(pageResult);
        // 处理权限
        for (RecycleVO recycle : pageResult) {
            final String recycleType = recycle.getType();
            final String permissionActionRange = TYPE_BASE.equals(recycleType) ?
                    PermissionConstants.ActionPermission.ActionPermissionRange.ACTION_RANGE_KNOWLEDGE_BASE :
                    recycleType;
            final String targetBaseType = TYPE_BASE.equals(recycleType) ?
                    PermissionConstants.PermissionTargetBaseType.KNOWLEDGE_BASE.toString() :
                    Objects.requireNonNull(WorkSpaceType.toTargetBaseType(recycleType)).toString();
            recycle.setPermissionCheckInfos(
                    this.permissionCheckDomainService.checkPermission(
                            organizationId,
                            projectId,
                            targetBaseType,
                            null,
                            recycle.getId(),
                            PermissionConstants.ActionPermission.generatePermissionCheckVOList(permissionActionRange),
                            false,
                            true
                    )
            );
        }
        UserInfoVO.clearCurrentUserInfo();
        return pageResult;
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
