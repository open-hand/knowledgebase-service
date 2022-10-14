package io.choerodon.kb.app.service;

import io.choerodon.kb.api.vo.permission.PermissionDetailVO;

/**
 * 知识库安全设置应用服务
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
public interface SecurityConfigService {

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
