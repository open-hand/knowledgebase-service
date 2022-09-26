package io.choerodon.kb.api.vo.permission;

import java.util.List;

import io.choerodon.kb.domain.entity.PermissionRange;

/**
 * Copyright (c) 2022. Zknow Enterprise Solution. All right reserved.
 *
 * @author zongqi.hao@zknow.com
 * @since 2022/9/23
 */
public class OrganizationPermissionSettingVO {

    /**
     * 组织创建权限范围类型
     */
    private String organizationCreateRangeType;
    /**
     * 组织创建权限设置
     */
    private List<PermissionRange> organizationCreateSetting;
    /**
     * 项目创建权限范围类型
     */
    private String projectCreateRangeType;
    /**
     * 项目创建知识库权限设置
     */
    private List<PermissionRange> projectCreateSetting;
    /**
     * 组织知识库默认权限配置
     */
    private List<PermissionRange> organizationDefaultPermissionRange;
    /**
     * 项目知识库默认权限配置
     */
    private List<PermissionRange> projectDefaultPermissionRange;

    public List<PermissionRange> getOrganizationCreateSetting() {
        return organizationCreateSetting;
    }

    public void setOrganizationCreateSetting(List<PermissionRange> organizationCreateSetting) {
        this.organizationCreateSetting = organizationCreateSetting;
    }

    public List<PermissionRange> getProjectCreateSetting() {
        return projectCreateSetting;
    }

    public void setProjectCreateSetting(List<PermissionRange> projectCreateSetting) {
        this.projectCreateSetting = projectCreateSetting;
    }

    public List<PermissionRange> getProjectDefaultPermissionRange() {
        return projectDefaultPermissionRange;
    }

    public void setProjectDefaultPermissionRange(List<PermissionRange> projectDefaultPermissionRange) {
        this.projectDefaultPermissionRange = projectDefaultPermissionRange;
    }

    public List<PermissionRange> getOrganizationDefaultPermissionRange() {
        return organizationDefaultPermissionRange;
    }

    public void setOrganizationDefaultPermissionRange(List<PermissionRange> organizationDefaultPermissionRange) {
        this.organizationDefaultPermissionRange = organizationDefaultPermissionRange;
    }

    public String getOrganizationCreateRangeType() {
        return organizationCreateRangeType;
    }

    public void setOrganizationCreateRangeType(String organizationCreateRangeType) {
        this.organizationCreateRangeType = organizationCreateRangeType;
    }

    public String getProjectCreateRangeType() {
        return projectCreateRangeType;
    }

    public void setProjectCreateRangeType(String projectCreateRangeType) {
        this.projectCreateRangeType = projectCreateRangeType;
    }
}
