package io.choerodon.kb.app.service.impl;


import io.choerodon.kb.api.vo.ProjectDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.domain.Page;
import io.choerodon.kb.app.service.ProjectOperateService;
import io.choerodon.kb.infra.feign.BaseFeignClient;
import io.choerodon.kb.infra.feign.vo.ProjectDO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * @author zhaotianxin
 * @since 2020/1/3
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectOperateServiceImpl implements ProjectOperateService {
    @Autowired
    private BaseFeignClient baseFeignClient;

    @Override
    public Page<ProjectDO> pageProjectInfo(Long organizationId,
                                           Long projectId,
                                           PageRequest pageRequest,
                                           ProjectDTO project) {
        return baseFeignClient.pagingQueryAndTop(organizationId, pageRequest.getPage(), pageRequest.getSize(), project).getBody();
    }
}
