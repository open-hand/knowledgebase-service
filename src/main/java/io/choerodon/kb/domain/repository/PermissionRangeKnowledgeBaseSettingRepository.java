package io.choerodon.kb.domain.repository;

import java.util.List;

import io.choerodon.kb.api.vo.permission.OrganizationPermissionSettingVO;
import io.choerodon.kb.domain.entity.PermissionRange;

/**
 * 知识库权限创建范围资源库
 *
 * @author zongqi.hao@zknow.com
 */
public interface PermissionRangeKnowledgeBaseSettingRepository extends PermissionRangeBaseRepository {

    /**
     * 查询设置-组织知识库权限设置
     *
     * @param organizationId 组织id
     * @return 查询结果
     */
    OrganizationPermissionSettingVO queryOrgPermissionSetting(Long organizationId);

//    /**
//     * 查询组织知识库设置
//     *
//     * @param organizationId 租户id
//     * @return 组织层知识库配置集
//     */
//    List<PermissionRange> selectOrgSetting(Long organizationId);

    /**
     * 初始化组织成知识库创建和默认设置
     *
     * @param organizationId 租户id
     * @param defaultRanges  组装好的默认权限数据
     */
    void initOrganizationPermissionRangeKnowledgeBaseSetting(Long organizationId, List<PermissionRange> defaultRanges);
}
