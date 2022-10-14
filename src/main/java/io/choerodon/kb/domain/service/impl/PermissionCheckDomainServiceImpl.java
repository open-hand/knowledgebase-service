package io.choerodon.kb.domain.service.impl;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.api.vo.permission.PermissionCheckVO;
import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.choerodon.kb.domain.entity.PermissionCheckReader;
import io.choerodon.kb.domain.entity.UserInfo;
import io.choerodon.kb.domain.repository.PermissionRangeKnowledgeObjectSettingRepository;
import io.choerodon.kb.domain.repository.WorkSpaceRepository;
import io.choerodon.kb.domain.service.PermissionCheckDomainService;
import io.choerodon.kb.infra.enums.PermissionConstants;
import io.choerodon.kb.infra.permission.checker.PermissionChecker;

import org.hzero.core.base.BaseConstants;
import org.hzero.core.redis.RedisHelper;

/**
 * 知识库鉴权 Domain Service 实现类
 *
 * @author gaokuo.dai@zknow.com 2022-10-12
 */
@Service
public class PermissionCheckDomainServiceImpl implements PermissionCheckDomainService {

    @Autowired
    private RedisHelper redisHelper;
    @Autowired
    private WorkSpaceRepository workSpaceRepository;
    @Autowired
    private PermissionRangeKnowledgeObjectSettingRepository permissionRangeKnowledgeObjectSettingRepository;


    private final Set<PermissionChecker> permissionCheckers;

    public PermissionCheckDomainServiceImpl(@Autowired Set<PermissionChecker> permissionCheckers) {
        this.permissionCheckers = Optional.ofNullable(permissionCheckers).orElse(Collections.emptySet());
    }

    @Override
    public List<PermissionCheckVO> checkPermission(
            @Nonnull Long organizationId,
            Long projectId,
            String targetBaseType,
            String targetType,
            @Nonnull Long targetValue,
            Collection<PermissionCheckVO> permissionsWaitCheck
    ) {
        // 基础校验
        if (CollectionUtils.isEmpty(permissionsWaitCheck)) {
            return Collections.emptyList();
        }
        Assert.notNull(organizationId, BaseConstants.ErrorCode.NOT_NULL);
        if (projectId == null) {
            projectId = PermissionConstants.EMPTY_ID_PLACEHOLDER;
        }
        // 处理targetBaseType
        if(StringUtils.isBlank(targetType)) {
            targetType = new PermissionDetailVO()
                    .setBaseTargetType(targetBaseType)
                    .transformBaseTargetType(projectId)
                    .getTargetType();
        }
        Assert.isTrue(StringUtils.isNotBlank(targetBaseType) || StringUtils.isNotBlank(targetType), BaseConstants.ErrorCode.NOT_NULL);
        Assert.notNull(targetValue, BaseConstants.ErrorCode.NOT_NULL);

        // 当前用户没有登录, 直接按无权限处理
        final CustomUserDetails userDetails = DetailsHelper.getUserDetails();
        if(userDetails == null) {
            return permissionsWaitCheck.stream()
                    .peek(permissionCheck -> permissionCheck.setApprove(Boolean.FALSE).setControllerType(PermissionConstants.PermissionRole.NULL))
                    .collect(Collectors.toList());
        }
        // 如果用户是超管, 则直接放行
        if(Boolean.TRUE.equals(userDetails.getAdmin())) {
            return permissionsWaitCheck.stream()
                    .peek(permissionCheck -> permissionCheck.setApprove(Boolean.TRUE).setControllerType(PermissionConstants.PermissionRole.MANAGER))
                    .collect(Collectors.toList());
        }

        Long finalProjectId = projectId;
        String finalTargetType = targetType;
        // 预留下后续reactive化改造空间
        return this.permissionCheckers.stream()
                .map(checker -> checker.checkPermission(userDetails, organizationId, finalProjectId, finalTargetType, targetValue, permissionsWaitCheck))
                .collect(PermissionCheckVO.permissionCombiner);
    }

    @Override
    public boolean checkPermission(
            @Nonnull Long organizationId,
            Long projectId,
            String targetBaseType,
            String targetType,
            @Nonnull Long targetValue,
            @Nonnull String permissionCodeWaitCheck
    ) {
        List<PermissionCheckVO> checkInfo = Collections.singletonList(new PermissionCheckVO().setPermissionCode(permissionCodeWaitCheck));
        checkInfo = this.checkPermission(organizationId, projectId, targetBaseType, targetType, targetValue, checkInfo);
        if(CollectionUtils.isEmpty(checkInfo)) {
            return false;
        }
        return Boolean.TRUE.equals(checkInfo.get(0).getApprove());
    }

    @Override
    public boolean checkPermissionReader(@Nonnull Long organizationId,
                                         Long projectId,
                                         PermissionCheckReader permissionCheckReader) {
        return false;
    }

    @Override
    public List<PermissionCheckReader> checkPermissionReader(@Nonnull Long organizationId,
                                                             Long projectId,
                                                             List<PermissionCheckReader> permissionCheckReaders) {
        if (CollectionUtils.isEmpty(permissionCheckReaders)) {
            return Collections.emptyList();
        }
        Assert.notNull(organizationId, BaseConstants.ErrorCode.NOT_NULL);
        if (projectId == null) {
            projectId = PermissionConstants.EMPTY_ID_PLACEHOLDER;
        }
        UserInfo userInfo = permissionRangeKnowledgeObjectSettingRepository.queryUserInfo(organizationId, projectId);
        if (Boolean.TRUE.equals(userInfo.getAdminFlag())) {
            permissionCheckReaders.forEach(x -> x.setApprove(true));
            return permissionCheckReaders;
        }
        List<PermissionCheckReader> permissionCheckReaderList = new ArrayList<>();
        List<PermissionCheckReader> workSpaceCheckReader = new ArrayList<>();
        for (PermissionCheckReader permissionCheckReader : permissionCheckReaders) {
            String targetType = permissionCheckReader.getTargetType();
            Long targetValue = permissionCheckReader.getTargetValue();
            if (targetType == null || targetValue == null) {
                continue;
            }
            PermissionConstants.PermissionTargetType permissionTargetType =
                    PermissionConstants.PermissionTargetType.valueOf(targetType.toUpperCase());
            boolean isKnowledgeBase =
                    PermissionConstants.PermissionTargetBaseType.KNOWLEDGE_BASE.equals(permissionTargetType.getBaseType());
            if (isKnowledgeBase) {
                permissionCheckReaderList.add(permissionCheckReader);
            } else {
                workSpaceCheckReader.add(permissionCheckReader);
            }
        }
        if (!permissionCheckReaders.isEmpty()) {
            //查父级
            permissionCheckReaders.forEach(checker -> {
                Long targetId = checker.getTargetValue();
                String key = workSpaceRepository.buildTargetParentCacheKey(targetId);
                List<String> values = redisHelper.lstAll(key);
                String regex = BaseConstants.Symbol.DOUBLE_SLASH + BaseConstants.Symbol.VERTICAL_BAR;
                values.forEach(value -> {
                    String[] array = value.split(regex);
                    Long parentId = Long.valueOf(array[0]);
                    String targetType = array[1];
                    PermissionCheckReader reader = PermissionCheckReader.of(targetType, parentId, false);
                    if (!permissionCheckReaderList.contains(reader)) {
                        permissionCheckReaderList.add(reader);
                    }
                });
            });
        }
        for (PermissionCheckReader reader : permissionCheckReaderList) {
            //todo 封装一个通用方法
            String key =
                    PermissionConstants.PERMISSION_CACHE_PREFIX
                            + PermissionConstants.PermissionRefreshType.RANGE.getKebabCaseName()
                            + BaseConstants.Symbol.COLON
                            + organizationId
                            + BaseConstants.Symbol.COLON
                            + projectId + BaseConstants.Symbol.COLON
                            + reader.getTargetType()
                            + BaseConstants.Symbol.COLON
                            + reader.getTargetValue();
            //todo 从db or redis获取PermissionRange,判断权限
            Map<String, String> permissionRangeMap = redisHelper.hshGetAll(key);
            reader.setApprove(true);
        }
        //todo 聚合权限，对入参的权限赋值
        permissionCheckReaders.forEach(x -> x.setApprove(true));
        return permissionCheckReaders;
    }

}
