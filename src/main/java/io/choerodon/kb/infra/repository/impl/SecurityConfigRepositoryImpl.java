package io.choerodon.kb.infra.repository.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
        return null;
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
        // 生成缓存key
        final String cacheKey = this.buildCacheKey(organizationId, projectId, targetType, targetValue);
        // 从缓存中读取数据
        String result = this.redisHelper.hshGet(cacheKey, permissionCode);
        // 缓存数据处理
        if(result == null) {
            // 如果没有数据, 则触发从数据库加载
            this.loadToCache(organizationId, projectId, targetType, targetValue, permissionCode, false);
            // 加载到缓存之后重新从缓存查询一次
            result = String.valueOf(this.findAuthorizeFlagWithCache(organizationId, projectId, targetType, targetValue, permissionCode, false));
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
     * @param permissionCode    操作权限Code
     * @param setExpire         是否设置缓存失效时间
     */
    private void loadToCache(Long organizationId, Long projectId, String targetType, Long targetValue, String permissionCode, boolean setExpire) {
        // 基础校验
        if(
                organizationId == null
                        || projectId == null
                        || StringUtils.isBlank(targetType)
                        || targetValue == null
                        || StringUtils.isBlank(permissionCode)
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
        boolean containsThisHashKey = false;

        // 从缓存中加载已有数据
        Map<String, String> cacheMap = Optional.ofNullable(this.redisHelper.hshGetAll(cacheKey)).orElse(new HashMap<>());
        // 数据库数据处理
        for (SecurityConfig securityConfig : securityConfigs) {
            final String permissionCodeInDb = securityConfig.getPermissionCode();
            if(!containsThisHashKey && Objects.equals(permissionCodeInDb, permissionCode)) {
                // 如果数据库中没有这条hash key对应的数据, 则标记为无效数据
                containsThisHashKey = true;
            }
            cacheMap.put(permissionCodeInDb, String.valueOf(securityConfig.getAuthorizeFlag()));
        }
        if(!containsThisHashKey) {
            // 将无效的数据标记为INVALID并放入缓存中
            cacheMap.put(permissionCode, PermissionConstants.PERMISSION_CACHE_INVALID_PLACEHOLDER);
        }
        // 写缓存
        this.redisHelper.hshPutAll(cacheKey, cacheMap);
        // 设置缓存失效时间
        if(setExpire) {
            this.redisHelper.setExpire(cacheKey, PermissionConstants.PERMISSION_CACHE_EXPIRE);
        }
    }
}
