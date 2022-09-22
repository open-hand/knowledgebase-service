package io.choerodon.kb.infra.repository.impl;

import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.domain.Page;
import io.choerodon.kb.api.vo.ProjectDTO;
import io.choerodon.kb.api.vo.ProjectSearchVO;
import io.choerodon.kb.api.vo.WatermarkVO;
import io.choerodon.kb.domain.repository.IamRemoteRepository;
import io.choerodon.kb.infra.feign.IamFeignClient;
import io.choerodon.kb.infra.feign.vo.*;

import org.hzero.core.util.ResponseUtils;

/**
 * Created by wangxiang on 2022/4/26
 */
@Service
public class IamRemoteRepositoryImpl implements IamRemoteRepository {

    @Autowired
    private IamFeignClient iamFeignClient;

    @Override
    public List<UserDO> listUsersByIds(Collection<Long> ids, boolean onlyEnabled) {
        if(CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        final Long[] idArrays = ids.stream().filter(Objects::nonNull).distinct().toArray(Long[]::new);
        return ResponseUtils.getResponse(
                this.iamFeignClient.listUsersByIds(idArrays, onlyEnabled),
                new TypeReference<List<UserDO>>(){}
        );
    }

    @Override
    public List<OrganizationDTO> listOrganizationByUserId(Long userId) {
        if(userId == null) {
            return Collections.emptyList();
        }
        return ResponseUtils.getResponse(
                this.iamFeignClient.listOrganizationByUserId(userId),
                new TypeReference<List<OrganizationDTO>>(){}
        );
    }

    @Override
    public List<ProjectDO> listProjectsByOrgId(Long organizationId) {
        if(organizationId == null) {
            return Collections.emptyList();
        }
        return ResponseUtils.getResponse(
                this.iamFeignClient.listProjectsByOrgId(organizationId),
                new TypeReference<List<ProjectDO>>(){}
        );
    }

    @Override
    public Page<ProjectDO> pageProjectInfo(Long organizationId, Integer page, Integer size, ProjectSearchVO project) {
        return ResponseUtils.getResponse(
                this.iamFeignClient.pageProjectInfo(organizationId, page, size, project.getParam(), project.getTopProjectIds(), project.getIgnoredProjectIds()),
                new TypeReference<Page<ProjectDO>>(){}
        );
    }

    @Override
    public OrganizationDTO queryOrganizationById(Long organizationId) {
        if(organizationId == null) {
            return null;
        }
        return ResponseUtils.getResponse(
                this.iamFeignClient.queryOrganizationById(organizationId),
                OrganizationDTO.class
        );
    }

    @Override
    public List<ProjectDTO> queryProjectByIds(Collection<Long> ids) {
        if(CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return ResponseUtils.getResponse(
                this.iamFeignClient.queryProjectByIds(new HashSet<>(ids)),
                new TypeReference<List<ProjectDTO>>(){}
        );
    }

    @Override
    public ProjectDTO queryProjectById(Long projectId) {
        if(projectId == null) {
            return null;
        }
        return ResponseUtils.getResponse(
                this.iamFeignClient.queryProjectById(projectId),
                ProjectDTO.class
        );
    }

    @Override
    public Page<OrganizationSimplifyDTO> pageOrganizations(int page, int size) {
        return ResponseUtils.getResponse(
                this.iamFeignClient.pageOrganizations(page, size),
                new TypeReference<Page<OrganizationSimplifyDTO>>(){}
        );
    }

    @Override
    public List<ProjectDTO> getAllProjects() {
        return ResponseUtils.getResponse(
                this.iamFeignClient.getAllProjects(),
                new TypeReference<List<ProjectDTO>>(){}
        );
    }

    @Override
    public String queryOrgLevel(Long tenantId) {
        if(tenantId == null) {
            return null;
        }
        return ResponseUtils.getResponse(
                this.iamFeignClient.orgLevel(tenantId),
                String.class
        );
    }

    @Override
    public Boolean checkIsOrgRoot(Long organizationId, Long userId) {
        if(organizationId == null || userId == null) {
            return Boolean.FALSE;
        }
        return ResponseUtils.getResponse(
                this.iamFeignClient.checkIsOrgRoot(organizationId, userId),
                Boolean.class
        );
    }

    @Override
    public Boolean checkAdminPermission(Long projectId) {
        if(projectId == null) {
            return Boolean.FALSE;
        }
        return ResponseUtils.getResponse(
                this.iamFeignClient.checkAdminPermission(projectId),
                Boolean.class
        );
    }

    @Override
    public List<ProjectDTO> queryOrgProjectsOptional(Long organizationId, Long userId) {
        if(organizationId == null || userId == null) {
            return null;
        }
        return ResponseUtils.getResponse(
                this.iamFeignClient.queryOrgProjectsOptional(organizationId, userId),
                new TypeReference<List<ProjectDTO>>(){}
        );
    }

    @Override
    public WatermarkVO getWaterMarkConfig(Long organizationId) {
        if(organizationId == null) {
            return null;
        }
        return ResponseUtils.getResponse(
                this.iamFeignClient.getWaterMarkConfig(organizationId),
                WatermarkVO.class
        );
    }

    @Override
    public List<ProjectDTO> listProjectsByUserIdForSimple(Long organizationId,
                                                          Long userId,
                                                          String category,
                                                          Boolean enabled
    ) {
        return ResponseUtils.getResponse(
                this.iamFeignClient.listProjectsByUserIdForSimple(organizationId, userId, category, enabled),
                new TypeReference<List<ProjectDTO>>() {});
    }

    @Override
    public TenantWpsConfigVO queryTenantWpsConfig(Long tenantId) {
        if(tenantId == null) {
            return null;
        }
            return ResponseUtils.getResponse(
                this.iamFeignClient.queryTenantWpsConfig(tenantId),
                TenantWpsConfigVO.class
        );
    }
}
