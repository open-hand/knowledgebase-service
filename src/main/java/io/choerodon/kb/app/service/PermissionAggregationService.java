package io.choerodon.kb.app.service;

import io.choerodon.kb.api.vo.WorkSpaceTreeNodeVO;
import io.choerodon.kb.infra.enums.PermissionConstants;

/**
 * Copyright (c) 2022. Zknow Enterprise Solution. All right reserved.
 *
 * @author zongqi.hao@zknow.com
 * @since 2022/9/29
 */
public interface PermissionAggregationService {

    /**
     * 根据上级自动生成权限
     *
     * @param organizationId 组织id
     * @param projectId      项目id
     * @param targetBaseType 基础目标类型
     * @param workSpace      workSpace信息
     */
    void autoGeneratePermission(Long organizationId,
                                Long projectId,
                                PermissionConstants.PermissionTargetBaseType targetBaseType,
                                WorkSpaceTreeNodeVO workSpace);
}
