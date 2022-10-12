package io.choerodon.kb.domain.service;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.validation.constraints.NotBlank;

import io.choerodon.kb.api.vo.permission.PermissionCheckVO;

/**
 * 知识库鉴权 Domain Service
 * @author gaokuo.dai@zknow.com 2022-10-12
 */
public interface PermissionCheckDomainService {

    /**
     * 知识库鉴权
     * @param organizationId        组织ID
     * @param projectId             项目ID
     * @param targetType            控制对象类型
     * @param targetValue           控制对象ID
     * @param knowledgeBaseId       所属知识库ID
     * @param permissionsWaitCheck  待鉴权的权限
     * @return                      鉴权结果
     */
    List<PermissionCheckVO> checkPermission(
            @Nonnull Long organizationId,
            Long projectId,
            @NotBlank String targetType,
            @Nonnull Long targetValue,
            @Nonnull Long knowledgeBaseId,
            Collection<PermissionCheckVO> permissionsWaitCheck
    );

    /**
     * 知识库鉴权
     * @param organizationId            组织ID
     * @param projectId                 项目ID
     * @param targetType                控制对象类型
     * @param targetValue               控制对象ID
     * @param knowledgeBaseId           所属知识库ID
     * @param permissionCodeWaitCheck   待鉴权的权限
     * @return                          鉴权结果
     */
    boolean checkPermission(
            @Nonnull Long organizationId,
            Long projectId,
            @NotBlank String targetType,
            @Nonnull Long targetValue,
            @Nonnull Long knowledgeBaseId,
            @Nonnull String permissionCodeWaitCheck
    );

}
