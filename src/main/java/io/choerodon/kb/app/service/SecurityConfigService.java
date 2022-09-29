package io.choerodon.kb.app.service;

import java.util.List;

import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.choerodon.kb.api.vo.permission.PermissionSearchVO;
import io.choerodon.kb.domain.entity.SecurityConfig;

/**
 * 知识库安全设置应用服务
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
public interface SecurityConfigService {

    /**
     * 根据target查询
     *  @param organizationId   租户id
     * @param projectId         项目id
     * @param searchVO          查询target参数
     * @return 查询结果
     */
    List<SecurityConfig> queryByTarget(Long organizationId, Long projectId, PermissionSearchVO searchVO);

    /**
     * 保存安全设置
     *
     * @param organizationId        组织ID
     * @param projectId             项目ID
     * @param permissionDetailVO    知识库对象权限详情数据
     * @return 处理后的知识库对象权限详情
     */
    PermissionDetailVO saveSecurity(Long organizationId, Long projectId, PermissionDetailVO permissionDetailVO);
}
