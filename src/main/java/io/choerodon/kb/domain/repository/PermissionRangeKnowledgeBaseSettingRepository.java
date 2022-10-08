package io.choerodon.kb.domain.repository;

import io.choerodon.kb.api.vo.permission.OrganizationPermissionSettingVO;

/**
 * 权限范围知识库配置 领域资源库
 * @author zongqi.hao@zknow.com 2022-09-23
 */
public interface PermissionRangeKnowledgeBaseSettingRepository extends PermissionRangeBaseRepository {

    /**
     * 查询设置-组织知识库权限设置
     *
     * @param organizationId 组织id
     * @return 查询结果
     */
    OrganizationPermissionSettingVO queryOrgPermissionSetting(Long organizationId);

}
