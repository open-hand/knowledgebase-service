package io.choerodon.kb.domain.service;

import io.choerodon.kb.api.vo.permission.OrganizationPermissionSettingVO;

/**
 * 权限范围知识库配置 领域Service
 * @author gaokuo.dai@zknow.com 2022-09-27
 */
public interface PermissionRangeKnowledgeBaseSettingService {

    /**
     * 组织创建时初始化组织默认知识库设置
     * @param organizationId 组织ID
     */
    void initPermissionRangeOnOrganizationCreate(Long organizationId);

    void save(Long organizationId, OrganizationPermissionSettingVO organizationPermissionSetting);
}
