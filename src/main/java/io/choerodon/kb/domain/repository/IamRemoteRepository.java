package io.choerodon.kb.domain.repository;

import java.util.Collection;
import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.kb.api.vo.ProjectDTO;
import io.choerodon.kb.api.vo.ProjectSearchVO;
import io.choerodon.kb.api.vo.RoleAssignmentSearchVO;
import io.choerodon.kb.api.vo.WatermarkVO;
import io.choerodon.kb.api.vo.permission.RoleVO;
import io.choerodon.kb.api.vo.permission.UserInfoVO;
import io.choerodon.kb.api.vo.permission.WorkGroupVO;
import io.choerodon.kb.infra.feign.vo.*;

/**
 * Created by wangxiang on 2022/4/26
 */
public interface IamRemoteRepository {

    List<UserDO> listUsersByIds(Collection<Long> ids, boolean onlyEnabled);

    List<RoleVO> listRolesWithUserCountOnProjectLevel(Long projectId, RoleAssignmentSearchVO roleAssignmentSearchVO);

    List<RoleVO> listRolesOnOrganizationLevel(Long organizationId, String labelName, Boolean onlyEnabled);

    List<RoleVO> listRolesWithUserCountOnOrganizationLevel(Long organizationId, RoleAssignmentSearchVO roleAssignmentSearchVO);

    List<RoleVO> listRolesByIds(Long tenantId, Collection<Long> roleIds);

    List<WorkGroupVO> listWorkGroups(Long organizationId);

    List<OrganizationDTO> listOrganizationByUserId(Long userId);

    List<ProjectDO> listProjectsByOrgId(Long organizationId);

    /**
     * 分页查询组织下的项目(不包含本项目)
     * @param organizationId 组织ID
     * @param page 当前页面
     * @param size 页面大小
     * @param project 查询条件
     * @return 查询结果
     */
    Page<ProjectDO> pageProjectInfo(Long organizationId, Integer page, Integer size, ProjectSearchVO project);

    OrganizationDTO queryOrganizationById(Long organizationId);

    List<ProjectDTO> queryProjectByIds(Collection<Long> ids);

    ProjectDTO queryProjectById(Long projectId);

    Page<OrganizationSimplifyDTO> pageOrganizations(int page, int size);

    List<ProjectDTO> getAllProjects();

    String queryOrgLevel(Long tenantId);

    Boolean checkIsOrgRoot(Long organizationId, Long userId);

    Boolean checkAdminPermission(Long projectId);

    List<ProjectDTO> queryOrgProjectsOptional(Long organizationId, Long userId);

    WatermarkVO getWaterMarkConfig(Long organizationId);

    List<ProjectDTO> listProjectsByUserIdForSimple(Long organizationId, Long userId, String category, Boolean enabled);

    TenantWpsConfigVO queryTenantWpsConfig(Long tenantId);

    UserInfoVO queryUserInfo(Long userId, Long organizationId, Long projectId);
}
