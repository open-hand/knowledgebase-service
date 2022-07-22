package io.choerodon.kb.app.service;

import java.util.List;
import java.util.Set;

import io.choerodon.core.domain.Page;
import io.choerodon.kb.api.vo.ProjectDTO;
import io.choerodon.kb.infra.feign.vo.OrganizationDTO;
import io.choerodon.kb.infra.feign.vo.OrganizationSimplifyDTO;
import io.choerodon.kb.infra.feign.vo.ProjectDO;
import io.choerodon.kb.infra.feign.vo.UserDO;
import org.springframework.http.ResponseEntity;

/**
 * Created by wangxiang on 2022/4/26
 */
public interface BaseFeignService {
    ResponseEntity<List<UserDO>> listUsersByIds(Long[] ids, Boolean onlyEnabled);

    ResponseEntity<List<OrganizationDTO>> listOrganizationByUserId(Long userId);

    ResponseEntity<List<ProjectDO>> listProjectsByOrgId(Long organizationId);

    ResponseEntity<OrganizationDTO> query(Long organizationId);

    ResponseEntity<List<ProjectDTO>> queryProjectByIds(Set<Long> ids);

    ResponseEntity<ProjectDTO> queryProject(Long projectId);

    ResponseEntity<Page<OrganizationSimplifyDTO>> getAllOrgsList(int page, int size);

    ResponseEntity<List<ProjectDTO>> getAllProList();

    ResponseEntity<String> orgLevel(Long tenantId);
}
