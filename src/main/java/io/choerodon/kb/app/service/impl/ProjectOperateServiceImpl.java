package io.choerodon.kb.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import io.choerodon.core.domain.Page;
import io.choerodon.kb.app.service.ProjectOperateService;
import io.choerodon.kb.infra.feign.BaseFeignClient;
import io.choerodon.kb.infra.feign.vo.ProjectDO;
import io.choerodon.kb.infra.utils.PageUtils;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
    public Page<ProjectDO> pageProjectInfo(Long organizationId, Long projectId, PageRequest pageRequest) {
        List<ProjectDO> list = baseFeignClient.listProjectsByOrgId(organizationId).getBody();
        if(CollectionUtils.isEmpty(list)){
            return PageUtils.createPageFromList(new ArrayList<>(), pageRequest);
        }
        List<ProjectDO> collect = list.stream().filter(v -> (v.getEnabled() && !projectId.equals(v.getId()))).collect(Collectors.toList());
        return PageUtils.createPageFromList(collect, pageRequest);
    }
}
