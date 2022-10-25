package io.choerodon.kb.domain.repository;

import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

import io.choerodon.kb.api.vo.permission.PermissionSearchVO;
import io.choerodon.kb.api.vo.permission.UserInfoVO;
import io.choerodon.kb.domain.entity.PermissionRange;
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
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @param targetType
     * @param targetValue
     * @param userInfo
     * @return
     */
    List<PermissionRange> queryByUser(Long organizationId,
                                      Long projectId,
                                      String targetType,
                                      Long targetValue,
                                      UserInfoVO userInfo);

    /**
     * 查询已有协作者接口
     *
     * @param organizationId 组织id
     * @param projectId      项目id
     * @param searchVO       查询实体
     * @return List
     */
    List<PermissionRange> queryCollaborator(Long organizationId, Long projectId, PermissionSearchVO searchVO);

    /**
     * 查询用户在当前对象的可用权限
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @param targetBaseType    控制对象基础类型, 与targetType二选一即可
     * @param targetType        控制对象类型, 与targetBaseType二选一即可
     * @param targetValue       控制对象ID
     * @return                  用户在当前对象的可用权限
     */
    Set<String> queryUserAvailablePermissionRoleCode(
            @Nonnull Long organizationId,
            Long projectId,
            String targetBaseType,
            String targetType,
            @Nonnull Long targetValue
    );
}
