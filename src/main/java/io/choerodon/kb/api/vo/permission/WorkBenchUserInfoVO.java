package io.choerodon.kb.api.vo.permission;

import java.util.List;
import java.util.Set;

import io.swagger.annotations.ApiModelProperty;

/**
 * 工作台根据组织id查组织角色和项目角色以及工作组信息
 *
 * @author superlee
 * @since 2022-10-24
 */
public class WorkBenchUserInfoVO {

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "用户工作组ID")
    private Set<Long> workGroupIds;

    @ApiModelProperty(value = "是否为Root用户")
    private Boolean adminFlag;

    @ApiModelProperty(value = "组织层和项目层角色，组织层角色projectId为null")
    private List<RoleVO> roles;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Set<Long> getWorkGroupIds() {
        return workGroupIds;
    }

    public void setWorkGroupIds(Set<Long> workGroupIds) {
        this.workGroupIds = workGroupIds;
    }

    public Boolean getAdminFlag() {
        return adminFlag;
    }

    public void setAdminFlag(Boolean adminFlag) {
        this.adminFlag = adminFlag;
    }

    public List<RoleVO> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleVO> roles) {
        this.roles = roles;
    }
}
