package io.choerodon.kb.infra.repository.impl;

import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import io.choerodon.kb.domain.entity.PermissionRoleConfig;
import io.choerodon.kb.domain.repository.PermissionRoleConfigRepository;
import io.choerodon.kb.infra.enums.PermissionConstants;

import org.hzero.core.base.BaseConstants;
import org.hzero.core.redis.RedisHelper;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;

/**
 * 知识库权限矩阵 资源库实现
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
@Repository
public class PermissionRoleConfigRepositoryImpl extends BaseRepositoryImpl<PermissionRoleConfig> implements PermissionRoleConfigRepository {

    @Autowired
    private RedisHelper redisHelper;

    private final String REDIS_KEY_PREFIX = PermissionConstants.PERMISSION_CACHE_PREFIX + PermissionConstants.PermissionRefreshType.ROLE_CONFIG.getKebabCaseName();

    @Override
    public void reloadCache() {
        // 移除旧缓存
        Set<String> removeKeys = redisHelper.keys(REDIS_KEY_PREFIX + BaseConstants.Symbol.STAR);
        if (CollectionUtils.isNotEmpty(removeKeys)) {
            redisHelper.delKeys(removeKeys);
        }
        // DB查询数据
        final List<PermissionRoleConfig> allConfigs = this.selectAll();
        if(CollectionUtils.isEmpty(allConfigs)) {
            return;
        }
        // 添加新缓存
        for (PermissionRoleConfig config : allConfigs) {
            this.redisHelper.hshPut(
                    this.generateCacheKey(
                            config.getOrganizationId(),
                            config.getProjectId(),
                            config.getTargetType(),
                            config.getPermissionRoleCode()
                    ),
                    config.getPermissionCode(),
                    String.valueOf(config.getAuthorizeFlag())
            );
        }
    }

    @Override
    public PermissionRoleConfig findByUniqueKey(PermissionRoleConfig permissionRoleConfig) {
        if(permissionRoleConfig == null) {
            return null;
        }
        final PermissionRoleConfig queryParam = permissionRoleConfig.generateUniqueQueryParam();
        if(queryParam == null) {
            return null;
        }
        return this.selectOne(queryParam);
    }

    /**
     * 拼接缓存KEY
     * @param organizationId        组织ID
     * @param projectId             项目ID
     * @param targetBaseType        控制对象基础类型
     * @param permissionRoleCode    授权角色
     * @return                      缓存KEY
     */
    private String generateCacheKey(Long organizationId, Long projectId, String targetBaseType, String permissionRoleCode) {
        // 基础校验
        Assert.notNull(organizationId, BaseConstants.ErrorCode.NOT_NULL);
        Assert.notNull(projectId, BaseConstants.ErrorCode.NOT_NULL);
        Assert.hasText(targetBaseType, BaseConstants.ErrorCode.NOT_NULL);
        Assert.hasText(permissionRoleCode, BaseConstants.ErrorCode.NOT_NULL);
        // 拼接
        return this.REDIS_KEY_PREFIX + BaseConstants.Symbol.COLON
                + organizationId + BaseConstants.Symbol.COLON
                + projectId + BaseConstants.Symbol.COLON
                + targetBaseType + BaseConstants.Symbol.COLON
                + permissionRoleCode;
    }

}
