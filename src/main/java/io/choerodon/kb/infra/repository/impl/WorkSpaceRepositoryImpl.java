package io.choerodon.kb.infra.repository.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import io.choerodon.core.domain.Page;
import io.choerodon.kb.domain.repository.WorkSpaceRepository;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.enums.PermissionConstants;
import io.choerodon.kb.infra.enums.WorkSpaceType;
import io.choerodon.kb.infra.mapper.WorkSpaceMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;

import org.hzero.core.base.BaseConstants;
import org.hzero.core.redis.RedisHelper;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;

/**
 * Copyright (c) 2022. Hand Enterprise Solution Company. All right reserved.
 *
 * @author zongqi.hao@zknow.com
 * @since 2022/8/29
 */
@Repository
public class WorkSpaceRepositoryImpl extends BaseRepositoryImpl<WorkSpaceDTO> implements WorkSpaceRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkSpaceRepositoryImpl.class);

    @Autowired
    private WorkSpaceMapper workSpaceMapper;
    @Autowired
    private RedisHelper redisHelper;


    @Override
    public List<WorkSpaceDTO> selectErrorRoute() {
        return workSpaceMapper.selectErrorRoute();
    }

    @Override
    public List<WorkSpaceDTO> selectWorkSpaceNameByIds(Collection<Long> workSpaceIds) {
        if (CollectionUtils.isEmpty(workSpaceIds)) {
            return Collections.emptyList();
        }
        return this.workSpaceMapper.selectWorkSpaceNameByIds(workSpaceIds);
    }

    @Override
    public void reloadTargetParentMappingToRedis() {
        StringBuilder builder =
                new StringBuilder(PermissionConstants.PERMISSION_CACHE_PREFIX)
                        .append(PermissionConstants.PermissionRefreshType.TARGET_PARENT.getKebabCaseName());
        String dirPath = builder.toString();
        builder.append(BaseConstants.Symbol.STAR);
        String dirRegex = builder.toString();
        Set<String> keys = redisHelper.keys(dirRegex);
        if (CollectionUtils.isNotEmpty(keys)) {
            redisHelper.delKeys(keys);
        }
        int page = 0;
        int size = 1000;
        int totalPage = 1;
        Map<Long, String> workSpaceTypeMap = new HashMap<>();
        while (page + 1 <= totalPage) {
            Page<WorkSpaceDTO> workSpacePage = PageHelper.doPage(page, size, this::selectAll);
            List<WorkSpaceDTO> workSpaceList = workSpacePage.getContent();
            for (WorkSpaceDTO workSpace : workSpaceList) {
                Long id = workSpace.getId();
                workSpaceTypeMap.put(id, workSpace.getType());
                String key = dirPath + BaseConstants.Symbol.COLON + id;
                loadTargetParentToRedis(key, workSpace, id, workSpaceTypeMap);
            }
            LOGGER.info("workspace父子级映射第【{}】页加载redis完成，共【{}】页，总计【{}】条，步长【{}】",
                    workSpacePage.getNumber() + 1,
                    workSpacePage.getTotalPages(),
                    workSpacePage.getTotalElements(),
                    size);
            totalPage = workSpacePage.getTotalPages();
            page++;
        }
    }

    @Override
    public void delTargetParentRedisCache(Long id) {
        String key = buildTargetParentCacheKey(id);
        redisHelper.delKey(key);
    }

    /**
     * redis value为list string,由根节点依此向下顺序存放
     * parentId:{@link PermissionConstants.PermissionTargetType}:{@link PermissionConstants.PermissionTargetBaseType kebabCaseName}
     *
     * @param key              redis key => knowledge:permission:target-parent:#{workSpaceId}
     * @param workSpace        文档/文件夹
     * @param id               文档/文件夹id
     * @param workSpaceTypeMap workspace 和 type映射，加速刷新缓存速度
     */
    private void loadTargetParentToRedis(String key,
                                         WorkSpaceDTO workSpace,
                                         Long id,
                                         Map<Long, String> workSpaceTypeMap) {
        if (workSpaceTypeMap == null) {
            workSpaceTypeMap = new HashMap<>();
        }
        String route = workSpace.getRoute();
        Long baseId = workSpace.getBaseId();
        Assert.notNull(route, "error.workspace.route.null." + id);
        List<String> routeList = new ArrayList<>();
        Long projectId = workSpace.getProjectId();
        PermissionConstants.PermissionTargetType permissionTargetType =
                PermissionConstants.PermissionTargetType.getPermissionTargetType(projectId, PermissionConstants.PermissionTargetBaseType.KNOWLEDGE_BASE.toString());
        Assert.notNull(permissionTargetType, BaseConstants.ErrorCode.DATA_INVALID);
        routeList.add(buildTargetParentValue(baseId, permissionTargetType));
        String regex = BaseConstants.Symbol.BACKSLASH + BaseConstants.Symbol.POINT;
        List<Long> parentIds = new ArrayList<>();
        Set<Long> selectInDbIds = new HashSet<>();
        for (String str : route.split(regex)) {
            Long parentId = Long.valueOf(str);
            if (!workSpaceTypeMap.containsKey(parentId)) {
                selectInDbIds.add(parentId);
            }
            parentIds.add(parentId);
        }
        if (!parentIds.isEmpty()) {
            if (!selectInDbIds.isEmpty()) {
                List<WorkSpaceDTO> workSpaceList = this.selectByIds(StringUtils.join(parentIds, BaseConstants.Symbol.COMMA));
                for (WorkSpaceDTO dto : workSpaceList) {
                    workSpaceTypeMap.put(dto.getId(), dto.getType());
                }
            }
            for (Long parentId : parentIds) {
                String type = workSpaceTypeMap.get(parentId);
                PermissionConstants.PermissionTargetType docType;
                boolean isFolder = WorkSpaceType.FOLDER.getValue().equals(type);
                if (isFolder) {
                    docType = PermissionConstants.PermissionTargetType.getPermissionTargetType(projectId, PermissionConstants.PermissionTargetBaseType.FOLDER.toString());
                } else {
                    docType = PermissionConstants.PermissionTargetType.getPermissionTargetType(projectId, PermissionConstants.PermissionTargetBaseType.FILE.toString());
                }
                Assert.notNull(docType, BaseConstants.ErrorCode.DATA_INVALID);
                routeList.add(buildTargetParentValue(parentId, docType));
            }
        }
        redisHelper.lstRightPushAll(key, routeList);
    }

    @Override
    public void reloadWorkSpaceTargetParent(WorkSpaceDTO workSpace) {
        Long id = workSpace.getId();
        Long baseId = workSpace.getBaseId();
        String route = workSpace.getRoute();
        Assert.notNull(id, "error.workspace.load.redis.parent.id.null");
        Assert.notNull(baseId, "error.workspace.load.redis.parent.baseId.null");
        Assert.notNull(route, "error.workspace.load.redis.parent.route.null");
        String key = buildTargetParentCacheKey(id);
        redisHelper.delKey(key);
        loadTargetParentToRedis(key, workSpace, id, null);
    }


    @Override
    public String buildTargetParentCacheKey(Long id) {
        return PermissionConstants.PERMISSION_CACHE_PREFIX +
                PermissionConstants.PermissionRefreshType.TARGET_PARENT.getKebabCaseName() +
                BaseConstants.Symbol.COLON +
                id;
    }

    /**
     * 生成缓存value
     * @param baseId                知识库ID
     * @param permissionTargetType  知识对象基础类型
     * @return                      缓存value
     */
    private String buildTargetParentValue(Long baseId, PermissionConstants.PermissionTargetType permissionTargetType) {
        StringBuilder builder = new StringBuilder();
        builder
                .append(baseId)
                .append(BaseConstants.Symbol.VERTICAL_BAR)
                .append(permissionTargetType.toString())
                .append(BaseConstants.Symbol.VERTICAL_BAR)
                .append(permissionTargetType.getBaseType().getKebabCaseName());
        return builder.toString();
    }


    @Override
    public int selectRecentMaxDepth(Long organizationId, Long projectId, Long baseId, boolean deleteFlag) {
        return Optional.ofNullable(workSpaceMapper.selectRecentMaxDepth(organizationId, projectId, baseId, deleteFlag)).orElse(0);
    }

    @Override
    public List<ImmutableTriple<Long, String, String>> findParentInfoWithCache(Long workSpaceId) {
        if(workSpaceId == null) {
            return Collections.emptyList();
        }
        final String cacheKey = this.buildTargetParentCacheKey(workSpaceId);
        final List<String> cacheResult = this.redisHelper.lstAll(cacheKey);
        if(CollectionUtils.isEmpty(cacheResult)) {
            return Collections.emptyList();
        }
        return cacheResult.stream()
                .filter(StringUtils::isNotBlank)
                .map(value -> {
                    final String[] split = StringUtils.split(value, BaseConstants.Symbol.VERTICAL_BAR);
                    if(ArrayUtils.isEmpty(split)) {
                        return ImmutableTriple.of((Long)null, (String)null, (String)null);
                    } else if(split.length <= 1) {
                        return ImmutableTriple.of(Long.parseLong(split[0]), (String)null, (String)null);
                    } else if(split.length == 2) {
                        return ImmutableTriple.of(Long.parseLong(split[0]), split[1], (String)null);
                    } else {
                        return ImmutableTriple.of(Long.parseLong(split[0]), split[1], split[2]);
                    }
                })
                .collect(Collectors.toList());
    }
}
