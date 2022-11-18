package io.choerodon.kb.app.service.impl;

import static org.hzero.core.base.BaseConstants.ErrorCode.FORBIDDEN;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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
    /**
     * 查询类型--MD文档/文件/文件夹
     */
    private static final String SEARCH_TYPE_PAGE = "page";
    /**
     * 查询类型--模板
     */
    private static final String SEARCH_TYPE_TEMPLATE = "template";
    /**
     * 查询类型--知识库
     */
    private static final String SEARCH_TYPE_BASE = "base";
    private static final String FIELD_SEARCH_TYPE = "type";
    private static final String FIELD_BELONG_TO_BASE_NAME = "belongToBaseName";
    private static final String[] WORK_SPACE_TYPE = {WorkSpaceType.DOCUMENT.getValue(), WorkSpaceType.FILE.getValue(), WorkSpaceType.FOLDER.getValue()};

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
        if (SEARCH_TYPE_BASE.equals(type)) {
            Assert.isTrue(permissionCheckDomainService.checkPermission(organizationId,
                    projectId,
                    PermissionConstants.PermissionTargetBaseType.KNOWLEDGE_BASE.toString(),
                    null,
                    id,
                    PermissionConstants.ActionPermission.KNOWLEDGE_BASE_RECOVER.getCode()), FORBIDDEN);
            knowledgeBaseService.restoreKnowledgeBase(organizationId, projectId, id);
        }
        Set<String> workSpaceType = Arrays.stream(WorkSpaceType.values()).map(WorkSpaceType::getValue).collect(Collectors.toSet());
        if (SEARCH_TYPE_PAGE.equals(type) || SEARCH_TYPE_TEMPLATE.equals(type) || workSpaceType.contains(type)) {
            workSpaceService.restoreWorkSpaceAndPage(organizationId, projectId, id, baseId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWorkSpaceAndPage(Long organizationId, Long projectId, String type, Long id) {
        if (SEARCH_TYPE_BASE.equals(type)) {
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
        if (SEARCH_TYPE_PAGE.equals(type) || SEARCH_TYPE_TEMPLATE.equals(type) || workSpaceType.contains(type)) {
            workSpaceService.deleteWorkSpaceAndPage(organizationId, projectId, id);
        }
    }


    @Override
    public Page<RecycleVO> pageList(Long projectId,
                                    Long organizationId,
                                    PageRequest pageRequest,
                                    SearchDTO searchDTO) {
        String searchType = null;
        String workSpaceType = null;
        String belongToBaseName = null;
        if (searchDTO != null) {
            Map<String, Object> searchArgs = searchDTO.getSearchArgs();
            if (searchArgs != null) {
                // search type需要特殊处理
                // 前端传过来的是 document/file/folder/template/base
                // 后端需要的是 page/template/base
                // 需要把 document/file/folder 映射为 page
                // 详见 https://choerodon.com.cn/#/agile/work-list/issue?type=project&id=261445508798373888&name=%E6%95%8F%E6%8D%B7%E5%8D%8F%E4%BD%9C%E7%BB%84&category=AGILE&organizationId=1128&paramIssueId=374532770002821120&paramName=yq-pm-4324
                searchType = (String) searchArgs.get(FIELD_SEARCH_TYPE);
                if(StringUtils.isBlank(searchType)) {
                    searchType = null;
                }
                if(searchType != null && ArrayUtils.contains(WORK_SPACE_TYPE, searchType)) {
                    workSpaceType = searchType;
                    searchType = SEARCH_TYPE_PAGE;
                    searchArgs.put(FIELD_SEARCH_TYPE, searchType);
                }
                belongToBaseName = (String) searchArgs.get(FIELD_BELONG_TO_BASE_NAME);
            }
        }
        if (SEARCH_TYPE_BASE.equals(searchType) && belongToBaseName != null) {
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
        final String finalWorkSpaceType = workSpaceType;
        if (searchType == null) {
            //查询全部
            page = PageHelper.doPage(pageRequest, () -> knowledgeBaseMapper.listRecycleData(organizationId, projectId, null, null, searchDTO, userInfo, userInfo.getAdminFlag(), rowNums));
        } else {
            switch (searchType) {
                case SEARCH_TYPE_BASE:
                    page = PageHelper.doPage(pageRequest, () -> knowledgeBaseMapper.listRecycleKnowledgeBase(organizationId, projectId, searchDTO, userInfo, userInfo.getAdminFlag()));
                    break;
                case SEARCH_TYPE_PAGE:
                    page = PageHelper.doPage(pageRequest, () -> knowledgeBaseMapper.listRecycleWorkSpace(organizationId, projectId, SEARCH_TYPE_PAGE, finalWorkSpaceType, searchDTO, userInfo, userInfo.getAdminFlag(), rowNums));
                    break;
                case SEARCH_TYPE_TEMPLATE:
                    page = PageHelper.doPage(pageRequest, () -> knowledgeBaseMapper.listRecycleWorkSpace(organizationId, projectId, SEARCH_TYPE_TEMPLATE, null, searchDTO, userInfo, userInfo.getAdminFlag(), rowNums));
                    break;
                default:
                    throw new CommonException(BaseConstants.ErrorCode.DATA_INVALID);
            }
        }
        for (RecycleVO recycleVO : page.getContent()) {
            if (recycleVO.getType() == null) {
                Long thisOrganizationId = recycleVO.getOrganizationId();
                Long thisProjectId = recycleVO.getProjectId();
                if (PermissionConstants.EMPTY_ID_PLACEHOLDER.equals(thisOrganizationId)
                        || PermissionConstants.EMPTY_ID_PLACEHOLDER.equals(thisProjectId)) {
                    recycleVO.setType(SEARCH_TYPE_TEMPLATE);
                } else {
                    recycleVO.setType(recycleVO.getWorkSpaceType());
                }
            }
        }
        knowledgeBaseAssembler.handleUserInfo(page);
        // 处理权限
        for (RecycleVO recycle : page) {
            final String recycleType = recycle.getType();
            final String permissionActionRange = SEARCH_TYPE_BASE.equals(recycleType) ?
                    PermissionConstants.ActionPermission.ActionPermissionRange.ACTION_RANGE_KNOWLEDGE_BASE :
                    recycle.getWorkSpaceType();
            final String targetBaseType = SEARCH_TYPE_BASE.equals(recycleType) ?
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
