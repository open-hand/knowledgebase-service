package io.choerodon.kb.app.service.impl;

import static io.choerodon.kb.infra.enums.PermissionConstants.ActionPermission;
import static io.choerodon.kb.infra.enums.PermissionConstants.PermissionTargetBaseType;
import static org.hzero.core.base.BaseConstants.ErrorCode.FORBIDDEN;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.vo.KnowledgeBaseInfoVO;
import io.choerodon.kb.api.vo.KnowledgeBaseListVO;
import io.choerodon.kb.api.vo.PageCreateWithoutContentVO;
import io.choerodon.kb.api.vo.permission.PermissionCheckVO;
import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.choerodon.kb.api.vo.permission.UserInfoVO;
import io.choerodon.kb.app.service.KnowledgeBaseService;
import io.choerodon.kb.app.service.PageService;
import io.choerodon.kb.app.service.SecurityConfigService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.app.service.assembler.KnowledgeBaseAssembler;
import io.choerodon.kb.domain.service.PermissionCheckDomainService;
import io.choerodon.kb.domain.service.PermissionRangeKnowledgeObjectSettingService;
import io.choerodon.kb.infra.dto.KnowledgeBaseDTO;
import io.choerodon.kb.infra.enums.OpenRangeType;
import io.choerodon.kb.infra.enums.PermissionConstants;
import io.choerodon.kb.infra.enums.WorkSpaceType;
import io.choerodon.kb.infra.mapper.KnowledgeBaseMapper;

import org.hzero.core.base.BaseConstants;

/**
 * @author zhaotianxin
 * @since 2019/12/30
 */
@Service
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    @Autowired
    private KnowledgeBaseMapper knowledgeBaseMapper;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private WorkSpaceService workSpaceService;
    @Autowired
    private PageService pageService;
    @Autowired
    private KnowledgeBaseAssembler knowledgeBaseAssembler;
    @Autowired
    private PermissionRangeKnowledgeObjectSettingService permissionRangeKnowledgeObjectSettingService;
    @Autowired
    private PermissionCheckDomainService permissionCheckDomainService;
    @Autowired
    private SecurityConfigService securityConfigService;

    private static final String BASE_SETTING_ACTION = ActionPermission.KNOWLEDGE_BASE_SETTINGS.getCode();
    private static final String BASE_COLLABORATORS_ACTION = ActionPermission.KNOWLEDGE_BASE_COLLABORATORS.getCode();
    private static final String BASE_SECURITY_CONFIG_ACTION = ActionPermission.KNOWLEDGE_BASE_SECURITY_SETTINGS.getCode();
    private static final String BASE_DELETE = ActionPermission.KNOWLEDGE_BASE_DELETE.getCode();


    @Override
    public KnowledgeBaseDTO queryById(Long id) {
        return knowledgeBaseMapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeBaseDTO baseInsert(KnowledgeBaseDTO knowledgeBaseDTO) {
        if (ObjectUtils.isEmpty(knowledgeBaseDTO)) {
            throw new CommonException("error.insert.knowledge.base.is.null");
        }
        if (knowledgeBaseMapper.insertSelective(knowledgeBaseDTO) != 1) {
            throw new CommonException("error.insert.knowledge.base");
        }
        return knowledgeBaseMapper.selectByPrimaryKey(knowledgeBaseDTO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeBaseDTO baseUpdate(KnowledgeBaseDTO knowledgeBaseDTO) {
        if (ObjectUtils.isEmpty(knowledgeBaseDTO)) {
            throw new CommonException("error.update.knowledge.base.is.null");
        }
        if (knowledgeBaseMapper.updateByPrimaryKeySelective(knowledgeBaseDTO) != 1) {
            throw new CommonException("error.update.knowledge.base");
        }
        return knowledgeBaseMapper.selectByPrimaryKey(knowledgeBaseDTO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeBaseInfoVO create(Long organizationId,
                                      Long projectId,
                                      KnowledgeBaseInfoVO knowledgeBaseInfoVO,
                                      boolean initFlag) {
        if (!initFlag) {
            // 鉴权
            Assert.isTrue(
                    this.permissionCheckDomainService.checkPermission(
                            organizationId,
                            projectId,
                            PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_CREATE,
                            null,
                            PermissionConstants.EMPTY_ID_PLACEHOLDER,
                            PermissionConstants.ACTION_PERMISSION_CREATE_KNOWLEDGE_BASE
                    ),
                    FORBIDDEN
            );
        }
        KnowledgeBaseDTO knowledgeBase = modelMapper.map(knowledgeBaseInfoVO, KnowledgeBaseDTO.class);
        knowledgeBase.setProjectId(projectId);
        knowledgeBase.setOrganizationId(organizationId);
        // 公开范围
        knowledgeBase = processKnowledgeBaseOpenRangeProject(knowledgeBaseInfoVO, knowledgeBase);
        // 插入数据库
        knowledgeBase = baseInsert(knowledgeBase);
        // 先初始化权限配置，后续步骤才能进行
        PermissionDetailVO permissionDetailVO = knowledgeBaseInfoVO.getPermissionDetailVO();
        permissionDetailVO.setTargetValue(knowledgeBase.getId());
        permissionRangeKnowledgeObjectSettingService.saveRangeAndSecurity(organizationId, projectId, permissionDetailVO, false);
        // 是否按模板创建知识库
        if (knowledgeBaseInfoVO.getTemplateBaseId() != null) {
            pageService.createByTemplate(organizationId, projectId, knowledgeBase.getId(), knowledgeBaseInfoVO.getTemplateBaseId(), initFlag);
        }
        //创建知识库的同时需要创建一个默认的文件夹
        // 取消文件夹的创建
//        this.createDefaultFolder(organizationId, projectId, knowledgeBase, initFlag);
        //返回给前端
        return knowledgeBaseAssembler.dtoToInfoVO(knowledgeBase);
    }

//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void createDefaultFolder(Long organizationId,
//                                    Long projectId,
//                                    KnowledgeBaseDTO knowledgeBaseInfo,
//                                    boolean initFlag) {
//        workSpaceService.createWorkSpaceAndPage(
//                organizationId,
//                projectId,
//                new PageCreateWithoutContentVO()
//                        .setParentWorkspaceId(PermissionConstants.EMPTY_ID_PLACEHOLDER)
//                        .setType(WorkSpaceType.FOLDER.getValue())
//                        .setOrganizationId(organizationId)
//                        .setTitle(knowledgeBaseInfo.getName())
//                        .setBaseId(knowledgeBaseInfo.getId())
//                        .setDescription(knowledgeBaseInfo.getDescription()),
//                initFlag
//
//        );
//    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeBaseInfoVO update(Long organizationId, Long projectId, KnowledgeBaseInfoVO knowledgeBaseInfoVO) {
        Long knowledgeBaseId = knowledgeBaseInfoVO.getId();
        Map<String, Boolean> checkResultMap =
                checkPermissionByCodes(
                        organizationId,
                        projectId,
                        PermissionTargetBaseType.KNOWLEDGE_BASE.toString(),
                        knowledgeBaseId,
                        Arrays.asList(BASE_SETTING_ACTION, BASE_COLLABORATORS_ACTION, BASE_SECURITY_CONFIG_ACTION));
        //无权限
        Assert.isTrue(!checkResultMap.isEmpty(), FORBIDDEN);
        knowledgeBaseInfoVO.setProjectId(projectId);
        knowledgeBaseInfoVO.setOrganizationId(organizationId);
        KnowledgeBaseDTO knowledgeBaseDTO = modelMapper.map(knowledgeBaseInfoVO, KnowledgeBaseDTO.class);
        knowledgeBaseDTO = processKnowledgeBaseOpenRangeProject(knowledgeBaseInfoVO, knowledgeBaseDTO);
        if (Boolean.TRUE.equals(checkResultMap.get(BASE_COLLABORATORS_ACTION))) {
            //有权限更改协作者
            permissionRangeKnowledgeObjectSettingService.saveRange(organizationId, projectId, knowledgeBaseInfoVO.getPermissionDetailVO(), false);
        }
        if (Boolean.TRUE.equals(checkResultMap.get(BASE_SECURITY_CONFIG_ACTION))) {
            //有权限更改安全设置
            securityConfigService.saveSecurity(organizationId, projectId, knowledgeBaseInfoVO.getPermissionDetailVO(), false);
        }
        if (Boolean.TRUE.equals(checkResultMap.get(BASE_SETTING_ACTION))) {
            return knowledgeBaseAssembler.dtoToInfoVO(baseUpdate(knowledgeBaseDTO));
        } else {
            return knowledgeBaseAssembler.dtoToInfoVO(queryById(knowledgeBaseDTO.getId()));
        }
    }

    private Map<String, Boolean> checkPermissionByCodes(Long organizationId,
                                                        Long projectId,
                                                        String baseTargetType,
                                                        Long targetValue,
                                                        List<String> actionCodes) {
        if (CollectionUtils.isEmpty(actionCodes)) {
            return Collections.emptyMap();
        }
        List<PermissionCheckVO> checkInfos = new ArrayList<>();
        for (String actionCode : actionCodes) {
            checkInfos.add(new PermissionCheckVO().setPermissionCode(actionCode));
        }
        return
                permissionCheckDomainService.checkPermission(organizationId, projectId, baseTargetType, null, targetValue, checkInfos)
                        .stream()
                        .collect(Collectors.toMap(PermissionCheckVO::getPermissionCode, PermissionCheckVO::getApprove));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeKnowledgeBase(Long organizationId, Long projectId, Long baseId) {
        Assert.isTrue(
                permissionCheckDomainService.checkPermission(organizationId, projectId, PermissionTargetBaseType.KNOWLEDGE_BASE.toString(), null, baseId, BASE_DELETE),
                FORBIDDEN);
        KnowledgeBaseDTO knowledgeBaseDTO = new KnowledgeBaseDTO();
        knowledgeBaseDTO.setOrganizationId(organizationId);
        knowledgeBaseDTO.setProjectId(projectId);
        knowledgeBaseDTO.setId(baseId);
        knowledgeBaseDTO = knowledgeBaseMapper.selectOne(knowledgeBaseDTO);
        knowledgeBaseDTO.setDelete(true);
        baseUpdate(knowledgeBaseDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteKnowledgeBase(Long organizationId, Long projectId, Long baseId) {
        // 删除知识库权限配置信息
        permissionRangeKnowledgeObjectSettingService.removePermissionRange(organizationId, projectId != null ? projectId : 0, PermissionConstants.PermissionTargetBaseType.KNOWLEDGE_BASE, baseId);
        // 彻底删除知识库下面所有的文件
        workSpaceService.deleteWorkSpaceByBaseId(organizationId, projectId, baseId);
        // 删除知识库
        knowledgeBaseMapper.deleteByPrimaryKey(baseId);
    }

    @Override
    public List<List<KnowledgeBaseListVO>> queryKnowledgeBaseWithRecent(Long organizationId, Long projectId) {
        List<KnowledgeBaseListVO> knowledgeBaseList = knowledgeBaseMapper.queryKnowledgeBaseList(projectId, organizationId);
        knowledgeBaseAssembler.addUpdateUser(knowledgeBaseList, organizationId);
        List<KnowledgeBaseListVO> selfKnowledgeBaseList = new ArrayList<>();
        List<KnowledgeBaseListVO> otherKnowledgeBaseList = new ArrayList<>();
        if (projectId != null) {
            final Map<Boolean, List<KnowledgeBaseListVO>> groupByIsProjectKnowledgeBase =
                    knowledgeBaseList
                            .stream()
                            .collect(Collectors.groupingBy(knowledgeBase -> Objects.equals(projectId, knowledgeBase.getProjectId())));
            Optional.ofNullable(groupByIsProjectKnowledgeBase.get(Boolean.TRUE)).ifPresent(selfKnowledgeBaseList::addAll);
            Optional.ofNullable(groupByIsProjectKnowledgeBase.get(Boolean.FALSE)).ifPresent(otherKnowledgeBaseList::addAll);
        } else {
            selfKnowledgeBaseList.addAll(knowledgeBaseList);
        }
        // 处理权限
        selfKnowledgeBaseList = selfKnowledgeBaseList.stream().map(selfKnowledgeBase ->
                        // 鉴权
                        selfKnowledgeBase.setPermissionCheckInfos(this.permissionCheckDomainService.checkPermission(
                                organizationId,
                                projectId,
                                PermissionConstants.PermissionTargetBaseType.KNOWLEDGE_BASE.toString(),
                                null,
                                selfKnowledgeBase.getId(),
                                PermissionConstants.ActionPermission.generatePermissionCheckVOList(ActionPermission.ActionPermissionRange.ACTION_RANGE_KNOWLEDGE_BASE),
                                false,
                                false,
                                true
                        )))
                // 过滤掉没有任何权限的
                .filter(selfKnowledgeBase -> PermissionCheckVO.hasAnyPermission(selfKnowledgeBase.getPermissionCheckInfos()))
                .collect(Collectors.toList());
        // 清除用户信息缓存
        UserInfoVO.clearCurrentUserInfo();

        return Arrays.asList(selfKnowledgeBaseList, otherKnowledgeBaseList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreKnowledgeBase(Long organizationId, Long projectId, Long baseId) {
        KnowledgeBaseDTO knowledgeBaseDTO = new KnowledgeBaseDTO();
        knowledgeBaseDTO.setProjectId(projectId);
        knowledgeBaseDTO.setOrganizationId(organizationId);
        knowledgeBaseDTO.setId(baseId);
        knowledgeBaseDTO = knowledgeBaseMapper.selectOne(knowledgeBaseDTO);
        knowledgeBaseDTO.setDelete(false);
        if (ObjectUtils.isEmpty(knowledgeBaseDTO)) {
            throw new CommonException("error.update.knowledge.base.is.null");
        }
        baseUpdate(knowledgeBaseDTO);

    }

    /**
     * 设置知识库公开范围
     *
     * @param knowledgeBaseInfoVO knowledgeBaseInfoVO
     * @param knowledgeBaseDTO    knowledgeBaseDTO
     * @return knowledgeBaseDTO
     */
    private KnowledgeBaseDTO processKnowledgeBaseOpenRangeProject(KnowledgeBaseInfoVO knowledgeBaseInfoVO, KnowledgeBaseDTO knowledgeBaseDTO) {
        if (OpenRangeType.RANGE_PROJECT.getType().equals(knowledgeBaseInfoVO.getOpenRange())) {
            List<Long> rangeProjectIds = knowledgeBaseInfoVO.getRangeProjectIds();
            if (CollectionUtils.isEmpty(rangeProjectIds)) {
                throw new CommonException("error.range.project.of.at.least.one.project");
            }
            knowledgeBaseDTO.setRangeProject(StringUtils.join(rangeProjectIds, BaseConstants.Symbol.COMMA));
        }
        return knowledgeBaseDTO;
    }

}
