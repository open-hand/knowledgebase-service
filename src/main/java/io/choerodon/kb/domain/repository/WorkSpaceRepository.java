package io.choerodon.kb.domain.repository;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutableTriple;

import io.choerodon.kb.infra.dto.WorkSpaceDTO;

import org.hzero.mybatis.base.BaseRepository;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface WorkSpaceRepository extends BaseRepository<WorkSpaceDTO> {

    /**
     * 查询所有错误数据<br/>
     * 非顶层的且数据有错误的(路由中不含父级id的数据)
     *
     * @return 所有错误数据
     */
    List<WorkSpaceDTO> selectErrorRoute();

    /**
     * 根据id集合查询名称
     *
     * @param workSpaceIds id集合
     * @return 名称集合
     */
    List<WorkSpaceDTO> selectWorkSpaceNameByIds(Collection<Long> workSpaceIds);

    /**
     * 重新加载workspace子节点和父节点集合映射到redis
     */
    void reloadTargetParentMappingToRedis();

    /**
     * 删除workspace子节点和父节点集合映射的redis缓存
     *
     * @param id id
     */
    void delTargetParentRedisCache(Long id);

    /**
     * 更新某一个workspace的父级数据缓存
     *
     * @param workSpace workspace
     */
    void reloadWorkSpaceTargetParent(WorkSpaceDTO workSpace);

    /**
     * 查询最大层数
     */
    int selectRecentMaxDepth(Long organizationId, Long projectId, Long baseId, boolean deleteFlag);

    /**
     * 从缓存中查询某一文档对象的所有父级
     * @param workSpaceId 文档对象ID
     * @return 查询结果: List&lt;ImmutableTriple&lt;父级对象ID, 父级对象权限控制类型, 父级对象权限控制基础类型&gt;&gt;, List里第一条是知识库的信息, 最后一条是自己的信息
     */
    List<ImmutableTriple<Long, String, String>> findParentInfoWithCache(Long workSpaceId);

    /**
     * 生成缓存key
     * @param id    ws对象主键
     * @return      缓存key
     */
    String buildTargetParentCacheKey(Long id);
}
