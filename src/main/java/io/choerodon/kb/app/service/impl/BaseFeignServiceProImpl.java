package io.choerodon.kb.app.service.impl;

import io.choerodon.kb.api.vo.ProjectDTO;
import io.choerodon.kb.app.service.BaseFeignService;
import io.choerodon.kb.infra.feign.IamFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zhaotianxin
 * @date 2022/1/6 23:52
 */
@Service
@Primary
public class BaseFeignServiceProImpl extends BaseFeignServiceImpl implements BaseFeignService {

    @Autowired
    private IamFeignClient iamFeignClient;

    public ResponseEntity<List<ProjectDTO>> queryOrgProjects(Long organizationId, Long userId) {
        return iamFeignClient.listProjectsByUserIdForSimple(organizationId, userId, null, true);
    }

}
