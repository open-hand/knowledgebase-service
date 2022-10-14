package io.choerodon.kb.infra.repository.impl;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.api.vo.permission.CollaboratorVO;
import io.choerodon.kb.api.vo.permission.RoleVO;
import io.choerodon.kb.api.vo.permission.UserInfoVO;
import io.choerodon.kb.api.vo.permission.WorkGroupVO;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.repository.IamRemoteRepository;
import io.choerodon.kb.domain.repository.PermissionRangeBaseRepository;
import io.choerodon.kb.infra.enums.PermissionConstants;
import io.choerodon.kb.infra.feign.vo.UserDO;
import io.choerodon.kb.infra.mapper.PermissionRangeMapper;

import org.hzero.core.base.BaseConstants;
import org.hzero.core.redis.RedisHelper;
import org.hzero.core.util.Pair;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.hzero.mybatis.domian.Condition;
import org.hzero.mybatis.util.Sqls;

/**
 * 知识库权限应用范围 资源库基础实现
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
public abstract class PermissionRangeBaseRepositoryImpl extends BaseRepositoryImpl<PermissionRange> implements PermissionRangeBaseRepository {

    @Autowired
    protected PermissionRangeMapper permissionRangeMapper;
    @Autowired
    protected IamRemoteRepository iamRemoteRepository;
    @Autowired
    protected RedisHelper redisHelper;

    /**
     * 缓存KEY
     */
    private final String REDIS_KEY_PREFIX = PermissionConstants.PERMISSION_CACHE_PREFIX + PermissionConstants.PermissionRefreshType.RANGE.getKebabCaseName();

    @Override
    public List<PermissionRange> assemblyRangeData(Long organizationId, List<PermissionRange> permissionRanges) {
        // 取出需要组装的数据集
        List<PermissionRange> result = new ArrayList<>();
        Map<String, List<PermissionRange>> rangeTypeGroupMap = permissionRanges.stream().collect(Collectors.groupingBy(PermissionRange::getRangeType));
        for (Map.Entry<String, List<PermissionRange>> rangeTypeGroup : rangeTypeGroupMap.entrySet()) {
            List<PermissionRange> ranges = rangeTypeGroup.getValue();
            Set<Long> collaboratorIds = ranges.stream().map(PermissionRange::getRangeValue).collect(Collectors.toSet());
            switch (PermissionConstants.PermissionRangeType.of(rangeTypeGroup.getKey())) {
                case USER:
                    List<UserDO> userDOS = iamRemoteRepository.listUsersByIds(collaboratorIds, false);
                    Map<Long, UserDO> userDOMap = userDOS.stream().collect(Collectors.toMap(UserDO::getId, Function.identity()));
                    for (PermissionRange range : ranges) {
                        UserDO userDO = userDOMap.get(range.getRangeValue());
                        if (userDO != null) {
                            range.setCollaborator(CollaboratorVO.ofUser(userDO));
                            result.add(range);
                        }
                    }
                    break;
                case ROLE:
                    List<RoleVO> roleVOS = iamRemoteRepository.listRolesByIds(organizationId, collaboratorIds);
//                 TODO 填充聚合信息 eg. 角色下包含的人数
                    Map<Long, RoleVO> roleVOMap = roleVOS.stream().collect(Collectors.toMap(RoleVO::getId, Function.identity()));
                    for (PermissionRange range : ranges) {
                        RoleVO roleVO = roleVOMap.get(range.getRangeValue());
                        if (roleVO != null) {
                            range.setCollaborator(CollaboratorVO.ofRole(roleVO));
                            result.add(range);
                        }
                    }
                    break;
                case WORK_GROUP:
                    List<WorkGroupVO> workGroupVOList = iamRemoteRepository.listWorkGroups(organizationId);
//                 TODO 填充聚合信息 eg. 角色下包含的人数
                    Map<Long, WorkGroupVO> workGroupVOMap = workGroupVOList.stream().collect(Collectors.toMap(WorkGroupVO::getId, Function.identity()));
                    for (PermissionRange range : ranges) {
                        WorkGroupVO workGroupVO = workGroupVOMap.get(range.getRangeValue());
                        if (workGroupVO != null) {
                            range.setCollaborator(CollaboratorVO.ofWorkGroup(workGroupVO));
                            result.add(range);
                        }
                    }
                    break;
                default:
                    result.addAll(ranges);
                    break;
            }
        }
        return result;
    }

    @Override
    public UserInfoVO queryUserInfo(Long organizationId,
                                    Long projectId) {
        CustomUserDetails customUserDetails = DetailsHelper.getUserDetails();
        UserInfoVO userInfo = iamRemoteRepository.queryUserInfo(customUserDetails.getUserId(), organizationId, projectId);
        Assert.notNull(userInfo, "error.permission.range.user.not.existed");
        userInfo.setAdminFlag(customUserDetails.getAdmin());
        return userInfo;
    }

    @Override
    public String findPermissionRoleCodeWithCache(Long organizationId, Long projectId, String targetType, Long targetValue, String rangeType, Long rangeValue) {
        return this.findPermissionRoleCodeWithCache(
                organizationId,
                projectId,
                targetType,
                targetValue,
                rangeType,
                rangeValue,
                true
        );
    }
    @Override
    public Set<Pair<Pair<String, Long>, String>> batchQueryPermissionRoleCodeWithCache(Long organizationId, Long projectId, String targetType, Long targetValue, Collection<Pair<String, Long>> rangePairs) {
        if(CollectionUtils.isEmpty(rangePairs)) {
            return Collections.emptySet();
        }
        if(
                organizationId == null
                        || projectId == null
                        || StringUtils.isBlank(targetType)
                        || targetValue == null
        ) {
            return rangePairs.stream().map(rangePair -> Pair.of(rangePair, (String)null)).collect(Collectors.toSet());
        }
        final Set<Pair<Pair<String, Long>, String>> result = rangePairs.stream()
                .map(rangePair -> Pair.of(
                                rangePair,
                                this.findPermissionRoleCodeWithCache(
                                        organizationId,
                                        projectId,
                                        targetType,
                                        targetValue,
                                        rangePair.getFirst(),
                                        rangePair.getSecond(),
                                        false
                                )
                        )
                )
                .collect(Collectors.toSet());
        this.redisHelper.setExpire(
                this.buildCacheKey(
                        organizationId,
                        projectId,
                        targetType,
                        targetValue
                ),
                PermissionConstants.PERMISSION_CACHE_EXPIRE
        );
        return result;
    }

    @Override
    public void clearCache() {
        Set<String> removeKeys = redisHelper.keys(REDIS_KEY_PREFIX + BaseConstants.Symbol.STAR);
        if (CollectionUtils.isNotEmpty(removeKeys)) {
            redisHelper.delKeys(removeKeys);
        }
    }

    @Override
    public void clearCache(Long organizationId, Long projectId, List<PermissionRange> permissionRanges) {
        if(organizationId == null || projectId == null || CollectionUtils.isEmpty(permissionRanges)) {
            return;
        }
        this.redisHelper.delKeys(
                permissionRanges.stream()
                        .map(pr -> Pair.of(pr.getTargetType(), pr.getTargetValue()))
                        .distinct()
                        .map(pair -> this.buildCacheKey(organizationId, projectId, pair.getFirst(), pair.getSecond()))
                        .collect(Collectors.toSet())
        );
    }

    /**
     * 生成缓存key
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @param targetType        控制对象类型
     * @param targetValue       控制对象ID
     * @return                  缓存key
     */
    private String buildCacheKey(Long organizationId, Long projectId, String targetType, Long targetValue) {
        return REDIS_KEY_PREFIX + BaseConstants.Symbol.COLON
                + organizationId + BaseConstants.Symbol.COLON
                + projectId  + BaseConstants.Symbol.COLON
                + targetType + BaseConstants.Symbol.COLON
                + targetValue;
    }

    /**
     * 生成缓存hash key
     * @param rangeType     授权对象类型
     * @param rangeValue    授权对象ID
     * @return              缓存hash key
     */
    private String buildCacheHashKey(String rangeType, Long rangeValue) {
        return StringUtils.EMPTY + rangeType + BaseConstants.Symbol.MIDDLE_LINE + rangeValue;
    }

    /**
     * 通过缓存查询授权角色, 查不到会去DB查询
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @param targetType        控制对象类型
     * @param targetValue       控制对象ID
     * @param rangeType         授权对象类型
     * @param rangeValue        授权对象ID
     * @param resetExpire       是否重置缓存失效时间
     * @return                  授权角色Code
     */
    private String findPermissionRoleCodeWithCache(Long organizationId, Long projectId, String targetType, Long targetValue, String rangeType, Long rangeValue, boolean resetExpire) {
        // 基础校验
        if(
                organizationId == null
                        || projectId == null
                        || StringUtils.isBlank(targetType)
                        || targetValue == null
                        || StringUtils.isBlank(rangeType)
                        || rangeValue == null
        ) {
            return null;
        }
        // 生成缓存key
        final String cacheKey = this.buildCacheKey(organizationId, projectId, targetType, targetValue);
        final String cacheHashKey = this.buildCacheHashKey(rangeType, rangeValue);
        // 从缓存中读取数据
        String result = this.redisHelper.hshGet(cacheKey, cacheHashKey);
        // 缓存数据处理
        if(result == null) {
            // 如果没有数据, 则触发从数据库加载
            this.loadToCache(organizationId, projectId, targetType, targetValue, rangeType, rangeValue, false);
            // 加载到缓存之后重新从缓存查询一次
            result = this.findPermissionRoleCodeWithCache(organizationId, projectId, targetType, targetValue, rangeType, rangeValue, false);
        } else if(PermissionConstants.PERMISSION_CACHE_INVALID_PLACEHOLDER.equals(result)) {
            // 如果有数据但是是INVALID, 返回空
            result = null;
        }
        // 重置缓存失效时间
        if(resetExpire) {
            this.redisHelper.setExpire(cacheKey, PermissionConstants.PERMISSION_CACHE_EXPIRE);
        }
        return result;
    }

    /**
     * 从数据库中加载缓存需要的数据
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @param targetType        控制对象类型
     * @param targetValue       控制对象ID
     * @param rangeType         授权对象类型
     * @param rangeValue        授权对象ID
     * @param setExpire         是否设置缓存失效时间
     */
    private void loadToCache(Long organizationId, Long projectId, String targetType, Long targetValue, String rangeType, Long rangeValue, boolean setExpire) {
        // 基础校验
        if(
                organizationId == null
                        || projectId == null
                        || StringUtils.isBlank(targetType)
                        || targetValue == null
                        || StringUtils.isBlank(rangeType)
                        || rangeValue == null
        ) {
            return;
        }
        // 查询数据库中的数据
        final List<PermissionRange> permissionRanges = this.selectByCondition(Condition.builder(PermissionRange.class).andWhere(Sqls.custom()
                .andEqualTo(PermissionRange.FIELD_ORGANIZATION_ID, organizationId)
                .andEqualTo(PermissionRange.FIELD_PROJECT_ID, projectId)
                .andEqualTo(PermissionRange.FIELD_TARGET_TYPE, targetType)
                .andEqualTo(PermissionRange.FIELD_TARGET_VALUE, targetValue)
        ).build());

        final String cacheKey = this.buildCacheKey(organizationId, projectId, targetType, targetValue);
        boolean containsThisHashKey = false;

        // 从缓存中加载已有数据
        Map<String, String> cacheMap = Optional.ofNullable(this.redisHelper.hshGetAll(cacheKey)).orElse(new HashMap<>());
        // 数据库数据处理
        for (PermissionRange permissionRange : permissionRanges) {
            final String rangeTypeInDb = permissionRange.getRangeType();
            final Long rangeValueInDb = permissionRange.getRangeValue();
            if(!containsThisHashKey && Objects.equals(rangeTypeInDb, rangeType) && Objects.equals(rangeValueInDb, rangeValue)) {
                // 如果数据库中没有这条hash key对应的数据, 则标记为无效数据
                containsThisHashKey = true;
            }
            cacheMap.put(this.buildCacheHashKey(rangeTypeInDb, rangeValueInDb), permissionRange.getPermissionRoleCode());
        }
        if(!containsThisHashKey) {
            // 将无效的数据标记为INVALID并放入缓存中
            cacheMap.put(this.buildCacheHashKey(rangeType, rangeValue), PermissionConstants.PERMISSION_CACHE_INVALID_PLACEHOLDER);
        }
        // 写缓存
        this.redisHelper.hshPutAll(cacheKey, cacheMap);
        // 设置缓存失效时间
        if(setExpire) {
            this.redisHelper.setExpire(cacheKey, PermissionConstants.PERMISSION_CACHE_EXPIRE);
        }
    }

}
