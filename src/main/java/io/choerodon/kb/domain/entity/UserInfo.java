package io.choerodon.kb.domain.entity;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 用户信息--权限专用
 * @author superlee
 * @since 2022-10-09
 */
@ApiModel(value = "用户信息--权限专用")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfo {

    @ApiModelProperty(value = "用户ID")
    private Long userId;
    @ApiModelProperty(value = "用户角色ID")
    private Set<Long> roleIds;
    @ApiModelProperty(value = "用户工作组ID")
    private Set<Long> workGroupIds;
    @ApiModelProperty(value = "是否为Root用户")
    private Boolean adminFlag;
    @ApiModelProperty(value = "是否为组织/项目下管理员")
    private Boolean managerFlag;
    @ApiModelProperty(value = "是否为组织/项目下的人")
    private Boolean memberFlag;

    /**
     * @return 用户ID
     */
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * @return 用户角色ID
     */
    public Set<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(Set<Long> roleIds) {
        this.roleIds = roleIds;
    }

    /**
     * @return 用户工作组ID
     */
    public Set<Long> getWorkGroupIds() {
        return workGroupIds;
    }

    public void setWorkGroupIds(Set<Long> workGroupIds) {
        this.workGroupIds = workGroupIds;
    }

    /**
     * @return 是否为Root用户
     */
    public Boolean getAdminFlag() {
        return adminFlag;
    }

    public void setAdminFlag(Boolean adminFlag) {
        this.adminFlag = adminFlag;
    }

    /**
     * @return 是否为组织/项目下管理员
     */
    public Boolean getManagerFlag() {
        return managerFlag;
    }

    public UserInfo setManagerFlag(Boolean managerFlag) {
        this.managerFlag = managerFlag;
        return this;
    }

    /**
     * @return 是否为组织/项目下的人
     */
    public Boolean getMemberFlag() {
        return memberFlag;
    }

    public UserInfo setMemberFlag(Boolean memberFlag) {
        this.memberFlag = memberFlag;
        return this;
    }
}
