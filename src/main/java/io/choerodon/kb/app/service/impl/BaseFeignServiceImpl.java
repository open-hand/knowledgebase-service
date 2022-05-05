package io.choerodon.kb.app.service.impl;

import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import io.choerodon.core.domain.Page;
import io.choerodon.kb.api.vo.ProjectDTO;
import io.choerodon.kb.app.service.BaseFeignService;
import io.choerodon.kb.infra.feign.BaseFeignClient;
import io.choerodon.kb.infra.feign.IamFeignClient;
import io.choerodon.kb.infra.feign.vo.OrganizationDTO;
import io.choerodon.kb.infra.feign.vo.OrganizationSimplifyDTO;
import io.choerodon.kb.infra.feign.vo.ProjectDO;
import io.choerodon.kb.infra.feign.vo.UserDO;

/**
 * Created by wangxiang on 2022/4/26
 */
@Service
public class BaseFeignServiceImpl implements BaseFeignService {
    @Qualifier("baseFeignClientFallback")
    @Autowired
    private BaseFeignClient baseFeignClient;

    @Autowired
    private IamFeignClient iamFeignClient;



    public ResponseEntity<List<UserDO>> listUsersByIds(Long[] ids, Boolean onlyEnabled) {
        return this.baseFeignClient.listUsersByIds(ids, onlyEnabled);
    }

    public ResponseEntity<List<OrganizationDTO>> listOrganizationByUserId(Long userId) {
        return this.baseFeignClient.listOrganizationByUserId(userId);
    }

    public ResponseEntity<List<ProjectDO>> listProjectsByOrgId(Long organizationId) {
        return this.baseFeignClient.listProjectsByOrgId(organizationId);
    }

    public ResponseEntity<OrganizationDTO> query(Long organizationId) {
        return this.baseFeignClient.query(organizationId);
    }

    public ResponseEntity<List<ProjectDTO>> queryProjectByIds(Set<Long> ids) {
        return this.baseFeignClient.queryProjectByIds(ids);
    }

    public ResponseEntity<ProjectDTO> queryProject(Long projectId) {
        return this.baseFeignClient.queryProject(projectId);
    }

    public ResponseEntity<Page<OrganizationSimplifyDTO>> getAllOrgsList(int page, int size) {
        return this.baseFeignClient.getAllOrgsList(page, size);
    }

    public ResponseEntity<List<ProjectDTO>> getAllProList() {
        return this.baseFeignClient.getAllProList();
    }

    public ResponseEntity<List<ProjectDTO>> queryOrgProjects(Long organizationId, Long userId) {
        return iamFeignClient.listProjectsByUserIdForSimple(organizationId, userId, null, true);
    }


    public ResponseEntity<String> orgLevel(Long tenantId) {
        return this.baseFeignClient.orgLevel(tenantId);
    }
}