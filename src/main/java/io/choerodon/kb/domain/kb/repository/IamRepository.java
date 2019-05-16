package io.choerodon.kb.domain.kb.repository;

import java.util.List;

import io.choerodon.kb.infra.dataobject.iam.OrganizationDO;
import io.choerodon.kb.infra.dataobject.iam.ProjectDO;
import io.choerodon.kb.infra.dataobject.iam.RoleDO;
import io.choerodon.kb.infra.dataobject.iam.UserDO;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface IamRepository {

    OrganizationDO queryOrganizationById(Long organizationId);

    ProjectDO queryIamProject(Long projectId);

    List<RoleDO> roleList(String code);

    List<UserDO> pagingQueryUsersByRoleIdOnOrganizationLevel(Long roleId, Long organizationId, Long userId);

    List<ProjectDO> pageByProject(Long organizationId);

    List<ProjectDO> queryProjects(Long id);

    List<UserDO> userDOList(Long[] ids);
}
