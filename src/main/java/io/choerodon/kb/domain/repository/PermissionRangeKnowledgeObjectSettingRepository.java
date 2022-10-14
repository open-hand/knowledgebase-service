package io.choerodon.kb.domain.repository;

import java.util.List;
import java.util.Set;

import io.choerodon.kb.api.vo.permission.PermissionSearchVO;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.entity.UserInfo;
import io.choerodon.kb.infra.enums.PermissionConstants;

/**
 * 权限范围知识对象设置 领域资源库
 *
 * @author zongqi.hao@zknow.com 2022-09-23
 */
public interface PermissionRangeKnowledgeObjectSettingRepository extends PermissionRangeBaseRepository {
    List<PermissionRange> queryObjectSettingCollaborator(Long organizationId, Long projectId, PermissionSearchVO searchVO);

    void remove(Long organizationId, Long projectId, Long targetValue);

    List<PermissionRange> selectFolderAndFileByTargetValues(Long organizationId, Long projectId, Set<PermissionConstants.PermissionTargetType> resourceTargetTypes, Set<String> workspaceIds);

    /**
     * 根据userInfo查询权限范围
     *
     * @param organizationId
     * @param projectId
     * @param targetType
     * @param targetValue
     * @param userInfo
     * @return
     */
    List<PermissionRange> queryByUser(Long organizationId,
                                      Long projectId,
                                      String targetType,
                                      Long targetValue,
                                      UserInfo userInfo);

    /**
     * 判断是否拥有当前知识库读权限
     *
     * @param organizationId 组织ID
     * @param projectId      项目ID
     * @param baseId         知识库ID
     * @param userInfo       用户信息
     * @return 查询结果
     */
    boolean hasKnowledgeBasePermission(Long organizationId,
                                       Long projectId,
                                       Long baseId,
                                       UserInfo userInfo);

    /**
     * 查询已有协作者接口
     *
     * @param organizationId 租户id
     * @param projectId      项目id
     * @param searchVO       查询实体
     * @return List
     */
    List<PermissionRange> queryCollaborator(Long organizationId, Long projectId, PermissionSearchVO searchVO);
}
