package io.choerodon.kb.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.github.pagehelper.PageInfo;
import io.choerodon.kb.app.service.BaseService;
import io.choerodon.kb.infra.feign.BaseFeignClient;
import io.choerodon.kb.infra.feign.vo.ProjectDO;
import io.choerodon.kb.infra.utils.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * @author zhaotianxin
 * @since 2020/1/3
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BaseServiceImpl implements BaseService {
    @Autowired
    private BaseFeignClient baseFeignClient;

    @Override
    public PageInfo<ProjectDO> pageProjectInfo(Long organizationId, Long projectId, Pageable pageable) {
        List<ProjectDO> list = baseFeignClient.listProjectsByOrgId(organizationId).getBody();
        if(CollectionUtils.isEmpty(list)){
            return PageUtils.createPageFromList(new ArrayList<>(),pageable);
        }
        List<ProjectDO> collect = list.stream().filter(v -> (v.getEnabled() && !projectId.equals(v.getId()))).collect(Collectors.toList());
        return PageUtils.createPageFromList(collect,pageable);
    }
}
