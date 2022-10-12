package io.choerodon.kb.domain.repository;

import java.util.Collection;
import java.util.List;

import io.choerodon.kb.infra.dto.WorkSpaceDTO;

import org.hzero.mybatis.base.BaseRepository;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface WorkSpaceRepository extends BaseRepository<WorkSpaceDTO> {

    /**
     * 查询所有错误数据<br/>
     * 非顶层的且数据有错误的(路由中不含父级id的数据)
     * @return 所有错误数据
     */
    List<WorkSpaceDTO> selectErrorRoute();

    /**
     * 根据id集合查询名称
     * @param workSpaceIds  id集合
     * @return              名称集合
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
     * @param workSpace workspace
     */
    void reloadWorkSpaceTargetParent(WorkSpaceDTO workSpace);
}
