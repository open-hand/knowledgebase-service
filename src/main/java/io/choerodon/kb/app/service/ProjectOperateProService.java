package io.choerodon.kb.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.kb.api.vo.ProjectDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * @author superlee
 * @since 2022-05-12
 */
public interface ProjectOperateProService {

    /**
     * 分页查询组织下的项目(不包含本项目)
     * @param organizationId
     * @param projectId
     * @param pageRequest
     * @return
     */
    Page<ProjectDTO> pageProjectInfo(Long organizationId,
                                    Long projectId,
                                    PageRequest pageRequest,
                                    ProjectDTO project);
}
