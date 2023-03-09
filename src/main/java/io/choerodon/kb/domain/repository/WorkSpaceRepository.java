package io.choerodon.kb.domain.repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutableTriple;

import io.choerodon.core.domain.Page;
import io.choerodon.kb.api.vo.*;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import org.hzero.mybatis.base.BaseRepository;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface WorkSpaceRepository extends BaseRepository<WorkSpaceDTO> {

    String ERROR_WORKSPACE_ILLEGAL = "error.workspace.illegal";
    String ERROR_WORKSPACE_NOTFOUND = "error.workspace.notFound";
    String KNOWLEDGE_UPLOAD_FILE = "knowledge-upload-file";
    String ERROR_WORKSPACE_INSERT = "error.workspace.insert";
    String ERROR_WORKSPACE_UPDATE = "error.workspace.update";

    WorkSpaceDTO baseCreate(WorkSpaceDTO workSpaceDTO);

    WorkSpaceDTO baseUpdate(WorkSpaceDTO workSpaceDTO);

    /**
     * 根据主键查询知识库对象, 校验项目层组织层权限
     *
     * @param organizationId 组织ID
     * @param projectId      项目ID
     * @param workSpaceId    知识库对象ID
     * @return 查询结果
     */
    WorkSpaceDTO baseQueryById(Long organizationId, Long projectId, Long workSpaceId);

    /**
     * 根据主键查询知识库对象, 校验项目层组织层权限，可以查询项目层权限
     *
     * @param organizationId 组织IDId
     * @param projectId      项目ID
     * @param workSpaceId    知识库对象ID
     * @return 查询结果
     */
    WorkSpaceDTO baseQueryByIdWithOrg(Long organizationId, Long projectId, Long workSpaceId);

    /**
     * 校验知识库对象ID是否存在, 不存在会报错
     *
     * @param organizationId 组织IDIdd
     * @param projectId      项目ID
     * @param workSpaceId    知识库对象ID
     */
    void checkExistsById(Long organizationId, Long projectId, Long workSpaceId);

    /**
     * 根据知识库对象ID查询所有子对象
     *
     * @param workSpaceId 知识库对象ID
     * @return 所有子对象
     */
    List<WorkSpaceDTO> queryAllChildByWorkSpaceId(Long workSpaceId);

    /**
     * 查询知识库对象详情
     *
     * @param organizationId  组织IDIdd
     * @param projectId       项目ID
     * @param workSpaceId     知识库对象ID
     * @param searchStr       查询条件
     * @param checkPermission 是否进行权限校验
     * @return 查询结果
     */
    WorkSpaceInfoVO queryWorkSpaceInfo(Long organizationId, Long projectId, Long workSpaceId, String searchStr, boolean checkPermission);

    /**
     * 所属知识库是否存在
     *
     * @param organizationId 组织IDIdd
     * @param projectId      项目ID
     * @param workSpaceId    知识库对象ID
     * @return 所属知识库是否存在
     */
    boolean belongToBaseExist(Long organizationId, Long projectId, Long workSpaceId);

    /**
     * 树形查询知识库对象所有子级
     *
     * @param workSpaceId 知识库对象ID
     * @param needChild   展示子级
     * @return 查询结果
     */
    WorkSpaceTreeVO queryAllChildTreeByWorkSpaceId(Long workSpaceId, boolean needChild);

    /**
     * 树形查询知识库下所有对象
     *
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @param knowledgeBaseId   知识库ID
     * @param expandWorkSpaceId 展开的知识库对象ID
     * @param excludeType       排除的类型
     * @return 查询结果
     */
    WorkSpaceTreeVO queryAllTreeList(Long organizationId, Long projectId, Long knowledgeBaseId, Long expandWorkSpaceId, String excludeType);


    /**
     * 条件查询所有的知识库对象
     *
     * @param organizationId  组织ID
     * @param projectId       项目ID
     * @param knowledgeBaseId 知识库ID
     * @param workSpaceId     知识库对象ID
     * @param excludeType     排除的类型
     * @return 查询结果
     */
    List<WorkSpaceVO> queryAllSpaceByOptions(Long organizationId, Long projectId, Long knowledgeBaseId, Long workSpaceId, String excludeType);

    /**
     * 根据主键批量查询知识库对象
     *
     * @param projectId    项目ID
     * @param workSpaceIds 知识库对象ID集合
     * @return 查询结果
     */
    List<WorkSpaceVO> querySpaceByIds(Long projectId, Collection<Long> workSpaceIds);

    /**
     * 校验用户是否有该组织的权限
     *
     * @param organizationId 组织ID
     */
    void checkOrganizationPermission(Long organizationId);

    /**
     * 分页查询最近文档
     *
     * @param organizationId  组织ID
     * @param projectId       项目ID
     * @param knowledgeBaseId 知识库ID
     * @param pageRequest     分页参数
     * @return 查询结果
     */
    Page<WorkSpaceRecentInfoVO> recentUpdateList(Long organizationId, Long projectId, Long knowledgeBaseId, PageRequest pageRequest);

    /**
     * 查询系统预置的模板
     *
     * @param knowledgeBaseIds 知识库ID集合
     * @return 查询结果
     */
    List<KnowledgeBaseTreeVO> listSystemTemplateBase(Collection<Long> knowledgeBaseIds);

    /**
     * 判断是不是操作模板
     *
     * @param workSpace 待检查的数据
     * @return 是不是操作模板
     */
    boolean isTemplate(WorkSpaceDTO workSpace);

    /**
     * 查询项目的所有知识库下面的文档
     *
     * @param organizationId 组织IId
     * @param projectId      项目ID
     * @return 查询结果
     */
    List<WorkSpaceVO> listAllSpace(Long organizationId, Long projectId);

    /**
     * 分页查询文件夹
     *
     * @param organizationId 组织ID
     * @param projectId      项目ID
     * @param id             这啥ID
     * @param pageRequest    分页参数
     * @return 查询结果
     */
    Page<WorkSpaceInfoVO> pageQueryFolder(Long organizationId, Long projectId, Long id, PageRequest pageRequest);

    /**
     * 查询附件上传状态
     *
     * @param projectId      项目ID
     * @param organizationId 组织ID
     * @param refId          refId
     * @param sourceType     sourceType
     * @return 查询结果
     */
    UploadFileStatusVO queryUploadStatus(Long projectId, Long organizationId, Long refId, String sourceType);

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
     *
     * @param workSpaceId 文档对象ID
     * @return 查询结果: List&lt;ImmutableTriple&lt;父级对象ID, 父级对象权限控制类型, 父级对象权限控制基础类型&gt;&gt;, List里第一条是知识库的信息, 最后一条是自己的信息
     */
    List<ImmutableTriple<Long, String, String>> findParentInfoWithCache(Long workSpaceId);

    /**
     * 生成缓存key
     *
     * @param id ws对象主键
     * @return 缓存key
     */
    String buildTargetParentCacheKey(Long id);

    List<WorkSpaceDTO> listByKnowledgeBaseIds(Set<Long> knowledgeBaseIds);

    /**
     * 将知识库对象列表构建为树
     *
     * @param organizationId      组织ID
     * @param projectId           项目ID
     * @param workSpaceList       知识库对象列表
     * @param expandWorkSpaceId   需要展开的知识库对象ID
     * @return 知识库对象树
     */
    List<WorkSpaceTreeNodeVO> buildWorkSpaceTree(Long organizationId,
                                                 Long projectId,
                                                 List<WorkSpaceDTO> workSpaceList,
                                                 Long expandWorkSpaceId);

    List<WorkSpaceVO> queryDefaultTemplate(Long organizationId, Long projectId, String params);

}
