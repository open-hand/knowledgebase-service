package io.choerodon.kb.infra.feign.fallback;

import java.util.List;
import java.util.Set;

import io.choerodon.core.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.FeignException;
import io.choerodon.kb.api.vo.ProjectDTO;
import io.choerodon.kb.infra.feign.BaseFeignClient;
import io.choerodon.kb.infra.feign.vo.OrganizationDTO;
import io.choerodon.kb.infra.feign.vo.OrganizationSimplifyDTO;
import io.choerodon.kb.infra.feign.vo.ProjectDO;
import io.choerodon.kb.infra.feign.vo.UserDO;

/**
 * Created by Zenger on 2019/4/30.
 */
@Component
public class BaseFeignClientFallback implements BaseFeignClient {

    private static final String BATCH_QUERY_ERROR = "error.baseFeign.queryList";
    private static final String LIST_ORGANIZATION_ERROR = "error.baseFeign.listOrganizationByUserId";
    private static final String LIST_PROJECT_ERROR = "error.baseFeign.listProjectByOrganizationId";
    private static final String QUERY_PROJECT_ERROR = "error.baseFeign.queryPorjectById";

    @Override
    public ResponseEntity<List<UserDO>> listUsersByIds(Long[] ids, Boolean onlyEnabled) {
        throw new FeignException(BATCH_QUERY_ERROR);
    }

    @Override
    public ResponseEntity<List<OrganizationDTO>> listOrganizationByUserId(Long userId) {
        throw new FeignException(LIST_ORGANIZATION_ERROR);
    }

    @Override
    public ResponseEntity<List<ProjectDTO>> queryProjectByIds(Set<Long> ids) {
        throw new FeignException(LIST_PROJECT_ERROR);
    }

    @Override
    public ResponseEntity<OrganizationDTO> query(Long id) {
        throw new FeignException(LIST_ORGANIZATION_ERROR);
    }

    @Override
    public ResponseEntity<List<ProjectDO>> listProjectsByOrgId(Long organizationId) {
        throw new FeignException(LIST_PROJECT_ERROR);
    }

    @Override
    public ResponseEntity<List<ProjectDTO>> getAllProList() {
        throw new FeignException(LIST_PROJECT_ERROR);
    }

    @Override
    public ResponseEntity<List<ProjectDTO>> queryOrgProjects(Long userId, Long organizationId) {
        throw new FeignException(LIST_PROJECT_ERROR);
    }

    @Override
    public ResponseEntity<Page<OrganizationSimplifyDTO>> getAllOrgsList(int page, int size) {
        throw new FeignException(LIST_ORGANIZATION_ERROR);
    }

    @Override
    public ResponseEntity<ProjectDTO> queryProject(Long id) {
        throw new FeignException(QUERY_PROJECT_ERROR);
    }

    @Override
    public ResponseEntity<String> orgLevel(Long tenantId) {
        throw new FeignException("error.query.project");
    }
}
