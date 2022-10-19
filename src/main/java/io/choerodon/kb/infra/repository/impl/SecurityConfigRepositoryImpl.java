package io.choerodon.kb.infra.repository.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import io.choerodon.kb.api.vo.permission.PermissionSearchVO;
import io.choerodon.kb.domain.entity.SecurityConfig;
import io.choerodon.kb.domain.repository.SecurityConfigRepository;
import io.choerodon.kb.infra.enums.PermissionConstants;

import org.hzero.core.base.BaseConstants;
import org.hzero.core.redis.RedisHelper;
import org.hzero.core.util.Pair;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.hzero.mybatis.domian.Condition;
import org.hzero.mybatis.util.Sqls;

/**
 * 知识库安全设置 资源库实现
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
@Repository
public class SecurityConfigRepositoryImpl extends BaseRepositoryImpl<SecurityConfig> implements SecurityConfigRepository {

    @Autowired
    private RedisHelper redisHelper;

    /**
     * 缓存KEY
     */
    private final String REDIS_KEY_PREFIX = PermissionConstants.PERMISSION_CACHE_PREFIX + PermissionConstants.PermissionRefreshType.SECURITY_CONFIG.getKebabCaseName();


    @Override
    public List<SecurityConfig> queryByTarget(Long organizationId, Long projectId, PermissionSearchVO searchVO) {
        searchVO.transformBaseTargetType(projectId);
        Condition condition = getCondition();
        Condition.Criteria criteria = condition.createCriteria();
        criteria.andEqualTo(SecurityConfig.FIELD_ORGANIZATION_ID, organizationId);
        criteria.andEqualTo(SecurityConfig.FIELD_PROJECT_ID, projectId);
        criteria.andEqualTo(SecurityConfig.FIELD_TARGET_TYPE, searchVO.getTargetType());
        criteria.andEqualTo(SecurityConfig.FIELD_TARGET_VALUE, searchVO.getTargetValue());
        return selectByCondition(condition);
    }

    @Override
    public Integer findAuthorizeFlagWithCache(Long organizationId, Long projectId, String targetType, Long targetValue, String permissionCode) {
        return this.findAuthorizeFlagWithCache(
                organizationId,
                projectId,
                targetType,
                targetValue,
                permissionCode,
                true
        );
    }

    @Override
    public Set<Pair<String, Integer>> batchQueryAuthorizeFlagWithCache(Long organizationId, Long projectId, String targetType, Long targetValue, Collection<String> permissionCodes) {
        if(CollectionUtils.isEmpty(permissionCodes)) {
            return Collections.emptySet();
        }
        if(
                organizationId == null
                        || projectId == null
                        || StringUtils.isBlank(targetType)
                        || targetValue == null
        ) {
            return permissionCodes.stream().map(permissionCode -> Pair.of(permissionCode, (Integer)null)).collect(Collectors.toSet());
        }
        // 实际鉴权时区分了document.*和file.*, 但是数据库中统一都存成了file.*, 所以需要进行一次转化
        // 下载权限在鉴权时区分了*.*-download, 但是数据库中统一都存成了*.download, 所以需要进行一次转化
        Map<String, String> originCodeToQueryCodeMap = new HashMap<>(permissionCodes.size());
        for (String permissionCode : permissionCodes) {
            final String[] split = StringUtils.split(permissionCode, BaseConstants.Symbol.POINT);
            Assert.isTrue((split != null && split.length >= 2), BaseConstants.ErrorCode.DATA_INVALID);
            String prefix = split[0];
            String actionCode = split[1];
            if(PermissionConstants.ActionPermission.ActionPermissionRange.ACTION_RANGE_DOCUMENT.equals(prefix)) {
                prefix = PermissionConstants.ActionPermission.ActionPermissionRange.ACTION_RANGE_FILE;
            }
            if(actionCode.endsWith(PermissionConstants.SecurityConfigAction.DOWNLOAD.toString().toLowerCase())) {
                actionCode = PermissionConstants.SecurityConfigAction.DOWNLOAD.toString().toLowerCase();
            }
            final String queryCode = prefix + BaseConstants.Symbol.POINT + actionCode;
            originCodeToQueryCodeMap.put(permissionCode, queryCode);
        }

        // 全量加载缓存中该key的数据
        final String cacheKey = this.buildCacheKey(organizationId, projectId, targetType, targetValue);
        final Map<String, String> dataInCache = this.redisHelper.hshGetAll(cacheKey);

        // 判断是否有miss的查询条件
        final Set<String> missHashKey = new HashSet<>(permissionCodes.size());
        Set<Pair<String, Integer>> result = new HashSet<>(permissionCodes.size());
        for (String permissionCode : permissionCodes) {
            final String cacheValue = dataInCache.get(originCodeToQueryCodeMap.get(permissionCode));
            if(cacheValue == null) {
                missHashKey.add(permissionCode);
            } else {
                result.add(Pair.of(permissionCode, PermissionConstants.PERMISSION_CACHE_INVALID_PLACEHOLDER.equals(cacheValue) ? null : Integer.parseInt(cacheValue)));
            }
        }
        // 如果有miss
        if(CollectionUtils.isNotEmpty(missHashKey)) {
            // 触发从数据库的重新加载
            this.loadToCache(
                    organizationId,
                    projectId,
                    targetType,
                    targetValue,
                    missHashKey,
                    false
            );
            // 然后再重新查询
            result = this.batchQueryAuthorizeFlagWithCache(
                    organizationId,
                    projectId,
                    targetType,
                    targetValue,
                    permissionCodes
            );
        }

        // 重置过期时间
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
    public void clearCache(Long organizationId, Long projectId, List<SecurityConfig> securityConfigs) {
        if(organizationId == null || projectId == null || CollectionUtils.isEmpty(securityConfigs)) {
            return;
        }
        this.redisHelper.delKeys(
                securityConfigs.stream()
                        .map(pr -> Pair.of(pr.getTargetType(), pr.getTargetValue()))
                        .distinct()
                        .map(pair -> this.buildCacheKey(organizationId, projectId, pair.getFirst(), pair.getSecond()))
                        .collect(Collectors.toSet())
        );
    }

    /**
     * 通过缓存查询是否授权操作, 查不到会去DB查询
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @param targetType        控制对象类型
     * @param targetValue       控制对象ID
     * @param permissionCode    操作权限Code
     * @param resetExpire       是否重置缓存失效时间
     * @return                  是否授权操作
     */
    private Integer findAuthorizeFlagWithCache(Long organizationId, Long projectId, String targetType, Long targetValue, String permissionCode, boolean resetExpire) {
        // 基础校验
        if(
                organizationId == null
                        || projectId == null
                        || StringUtils.isBlank(targetType)
                        || targetValue == null
                        || StringUtils.isBlank(permissionCode)
        ) {
            return null;
        }
        // 实际鉴权时区分了document.*和file.*, 但是数据库中统一都存成了file.*, 所以需要进行一次转化
        // 下载权限在鉴权时区分了*.*-download, 但是数据库中统一都存成了*.download, 所以需要进行一次转化
        final String[] split = StringUtils.split(permissionCode, BaseConstants.Symbol.POINT);
        Assert.isTrue((split != null && split.length >= 2), BaseConstants.ErrorCode.DATA_INVALID);
        String prefix = split[0];
        String actionCode = split[1];
        if(PermissionConstants.ActionPermission.ActionPermissionRange.ACTION_RANGE_DOCUMENT.equals(prefix)) {
            prefix = PermissionConstants.ActionPermission.ActionPermissionRange.ACTION_RANGE_DOCUMENT;
        }
        if(actionCode.endsWith(PermissionConstants.SecurityConfigAction.DOWNLOAD.toString().toLowerCase())) {
            actionCode = PermissionConstants.SecurityConfigAction.DOWNLOAD.toString().toLowerCase();
        }
        final String queryCode = prefix + BaseConstants.Symbol.POINT + actionCode;
        // 生成缓存key
        final String cacheKey = this.buildCacheKey(organizationId, projectId, targetType, targetValue);
        // 从缓存中读取数据
        String result = this.redisHelper.hshGet(cacheKey, queryCode);
        // 缓存数据处理
        if(result == null) {
            // 如果没有数据, 则触发从数据库加载
            this.loadToCache(organizationId, projectId, targetType, targetValue, Collections.singleton(permissionCode), false);
            // 加载到缓存之后重新从缓存查询一次
            final Integer innerResult = this.findAuthorizeFlagWithCache(organizationId, projectId, targetType, targetValue, permissionCode, false);
            result = Optional.ofNullable(innerResult).map(String::valueOf).orElse(null);
        } else if(PermissionConstants.PERMISSION_CACHE_INVALID_PLACEHOLDER.equals(result)) {
            // 如果有数据但是是INVALID, 返回空
            result = null;
        }
        // 重置缓存失效时间
        if(resetExpire) {
            this.redisHelper.setExpire(cacheKey, PermissionConstants.PERMISSION_CACHE_EXPIRE);
        }
        return Optional.ofNullable(result).map(Integer::parseInt).orElse(null);
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
     * 从数据库中加载缓存需要的数据
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @param targetType        控制对象类型
     * @param targetValue       控制对象ID
     * @param permissionCodes   操作权限Code集合
     * @param setExpire         是否设置缓存失效时间
     */
    private void loadToCache(Long organizationId, Long projectId, String targetType, Long targetValue, Collection<String> permissionCodes, boolean setExpire) {
        // 基础校验
        if(
                organizationId == null
                        || projectId == null
                        || StringUtils.isBlank(targetType)
                        || targetValue == null
                        || CollectionUtils.isEmpty(permissionCodes)
        ) {
            return;
        }
        // 查询数据库中的数据
        final List<SecurityConfig> securityConfigs = this.selectByCondition(Condition.builder(SecurityConfig.class).andWhere(Sqls.custom()
                .andEqualTo(SecurityConfig.FIELD_ORGANIZATION_ID, organizationId)
                .andEqualTo(SecurityConfig.FIELD_PROJECT_ID, projectId)
                .andEqualTo(SecurityConfig.FIELD_TARGET_TYPE, targetType)
                .andEqualTo(SecurityConfig.FIELD_TARGET_VALUE, targetValue)
        ).build());

        final String cacheKey = this.buildCacheKey(organizationId, projectId, targetType, targetValue);

        // 从缓存中加载已有数据
        Map<String, String> cacheMap = Optional.ofNullable(this.redisHelper.hshGetAll(cacheKey)).orElse(new HashMap<>());
        // 数据库数据处理
        for (SecurityConfig securityConfig : securityConfigs) {
            final String permissionCodeInDb = securityConfig.getPermissionCode();
            cacheMap.put(permissionCodeInDb, String.valueOf(securityConfig.getAuthorizeFlag()));
        }
        // 判断传入值是否为无效值
        for (String permissionCode : permissionCodes) {
            boolean containsThisHashKey = false;
            for (SecurityConfig securityConfig : securityConfigs) {
                final String permissionCodeInDb = securityConfig.getPermissionCode();
                if(Objects.equals(permissionCodeInDb, permissionCode)) {
                    // 如果数据库中没有这条hash key对应的数据, 则标记为无效数据
                    containsThisHashKey = true;
                    break;
                }
            }
            if(!containsThisHashKey) {
                // 将无效的数据标记为INVALID并放入缓存中
                cacheMap.put(permissionCode, PermissionConstants.PERMISSION_CACHE_INVALID_PLACEHOLDER);
            }
        }
        // 写缓存
        this.redisHelper.hshPutAll(cacheKey, cacheMap);
        // 设置缓存失效时间
        if(setExpire) {
            this.redisHelper.setExpire(cacheKey, PermissionConstants.PERMISSION_CACHE_EXPIRE);
        }
    }
}
