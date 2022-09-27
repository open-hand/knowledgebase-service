package io.choerodon.kb.domain.service;

public interface PermissionRangeKnowledgeBaseSettingService {

    /**
     * 组织创建时初始化组织默认知识库设置
     * @param organizationId 组织ID
     */
    void initPermissionRangeOnOrganizationCreate(Long organizationId);

}
