package io.choerodon.kb.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.kb.infra.feign.vo.ProjectDO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * @author zhaotianxin
 * @since 2020/1/3
 */
public interface ProjectOperateService {
    /**
     * 分页查询组织下的项目(不包含本项目)
     * @param organizationId
     * @param projectId
     * @param pageRequest
     * @return
     */
    Page<ProjectDO> pageProjectInfo(Long organizationId, Long projectId, PageRequest pageRequest);
}
