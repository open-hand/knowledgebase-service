package io.choerodon.kb.app.service;

import com.github.pagehelper.PageInfo;
import io.choerodon.kb.infra.feign.vo.ProjectDO;
import org.springframework.data.domain.Pageable;

/**
 * @author zhaotianxin
 * @since 2020/1/3
 */
public interface ProjectOperateService {
    /**
     * 分页查询组织下的项目(不包含本项目)
     * @param organizationId
     * @param projectId
     * @param pageable
     * @return
     */
    PageInfo<ProjectDO> pageProjectInfo(Long organizationId, Long projectId, Pageable pageable);
}
