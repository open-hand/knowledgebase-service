package io.choerodon.kb.app.service.impl;

import static org.hzero.core.base.BaseConstants.ErrorCode.FORBIDDEN;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.vo.RecycleVO;
import io.choerodon.kb.api.vo.SearchDTO;
import io.choerodon.kb.api.vo.permission.PermissionCheckVO;
import io.choerodon.kb.api.vo.permission.UserInfoVO;
import io.choerodon.kb.app.service.KnowledgeBaseService;
import io.choerodon.kb.app.service.RecycleService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.app.service.assembler.KnowledgeBaseAssembler;
import io.choerodon.kb.domain.repository.PermissionRangeKnowledgeObjectSettingRepository;
import io.choerodon.kb.domain.repository.WorkSpaceRepository;
import io.choerodon.kb.domain.service.PermissionCheckDomainService;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.enums.PermissionConstants;
import io.choerodon.kb.infra.enums.WorkSpaceType;
import io.choerodon.kb.infra.mapper.KnowledgeBaseMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import org.hzero.core.base.BaseConstants;

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
    public Page<RecycleVO> pageList(Long projectId,
                                    Long organizationId,
                                    PageRequest pageRequest,
                                    SearchDTO searchDTO) {
        String searchType = null;
        String belongToBaseName = null;
        if (searchDTO != null) {
            Map<String, Object> searchArgs = searchDTO.getSearchArgs();
            if (searchArgs != null) {
                searchType = (String) searchArgs.get(SEARCH_TYPE);
                belongToBaseName = (String) searchArgs.get("belongToBaseName");
            }
        }
        if (TYPE_BASE.equals(searchType) && belongToBaseName != null) {
            //筛选条件矛盾，返回空
            return new Page<>();
        }
        final UserInfoVO userInfo = permissionRangeKnowledgeObjectSettingRepository.queryUserInfo(organizationId, projectId);
        final List<Integer> rowNums = new ArrayList<>();
        int maxDepth = workSpaceRepository.selectRecentMaxDepth(organizationId, projectId, null, true);
        for (int i = 2; i <= maxDepth; i++) {
            rowNums.add(i);
        }
        Page<RecycleVO> page;
        if (searchType == null) {
            //查询全部
            page =
                    PageHelper.doPage(pageRequest, () -> knowledgeBaseMapper.listRecycleData(organizationId, projectId, null, searchDTO, userInfo, userInfo.getAdminFlag(), rowNums));
        } else {
            switch (searchType) {
                case TYPE_BASE:
                    page = PageHelper.doPage(pageRequest, () -> knowledgeBaseMapper.listRecycleKnowledgeBase(organizationId, projectId, searchDTO, userInfo, userInfo.getAdminFlag()));
                    break;
                case TYPE_PAGE:
                    page = PageHelper.doPage(pageRequest, () -> knowledgeBaseMapper.listRecycleWorkSpace(organizationId, projectId, TYPE_PAGE, searchDTO, userInfo, userInfo.getAdminFlag(), rowNums));
                    break;
                case TYPE_TEMPLATE:
                    page = PageHelper.doPage(pageRequest, () -> knowledgeBaseMapper.listRecycleWorkSpace(organizationId, projectId, TYPE_TEMPLATE, searchDTO, userInfo, userInfo.getAdminFlag(), rowNums));
                    break;
                default:
                    throw new CommonException(BaseConstants.ErrorCode.DATA_INVALID);
            }
        }
        page.getContent().forEach(x -> {
            if (x.getType() == null) {
                Long thisOrganizationId = x.getOrganizationId();
                Long thisProjectId = x.getProjectId();
                if (PermissionConstants.EMPTY_ID_PLACEHOLDER.equals(thisOrganizationId)
                        || PermissionConstants.EMPTY_ID_PLACEHOLDER.equals(thisProjectId)) {
                    x.setType(TYPE_TEMPLATE);
                } else {
                    x.setType(x.getWorkSpaceType());
                }
            }
        });
        knowledgeBaseAssembler.handleUserInfo(page);
        // 处理权限
        for (RecycleVO recycle : page) {
            final String recycleType = recycle.getType();
            final String permissionActionRange = TYPE_BASE.equals(recycleType) ?
                    PermissionConstants.ActionPermission.ActionPermissionRange.ACTION_RANGE_KNOWLEDGE_BASE :
                    recycle.getWorkSpaceType();
            final String targetBaseType = TYPE_BASE.equals(recycleType) ?
                    PermissionConstants.PermissionTargetBaseType.KNOWLEDGE_BASE.toString() :
                    Objects.requireNonNull(WorkSpaceType.toTargetBaseType(recycle.getWorkSpaceType())).toString();
            final boolean isTemplate = this.workSpaceRepository.checkIsTemplate(
                    recycle.getOrganizationId(),
                    recycle.getProjectId(),
                    new WorkSpaceDTO().setOrganizationId(recycle.getOrganizationId()).setProjectId(recycle.getProjectId())
            );
            if(isTemplate) {
                recycle.setPermissionCheckInfos(
                        PermissionCheckVO.generateManagerPermission(
                                PermissionConstants.ActionPermission.generatePermissionCheckVOList(permissionActionRange)
                        )
                );
            } else {
                recycle.setPermissionCheckInfos(
                        this.permissionCheckDomainService.checkPermission(
                                organizationId,
                                projectId,
                                targetBaseType,
                                null,
                                recycle.getId(),
                                PermissionConstants.ActionPermission.generatePermissionCheckVOList(permissionActionRange),
                                false
                        )
                );
            }
        }
        UserInfoVO.clearCurrentUserInfo();
        return page;
    }
}
