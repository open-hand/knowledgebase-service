package io.choerodon.kb.api.vo.permission;

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
public class UserInfoVO {

    /**
     * @return 当前线程的UserInfo
     */
    public static UserInfoVO currentUserInfo() {
        return CURRENT_USER_INFO.get();
    }

    /**
     * 设置当前线程的UserInfo
     * @param userInfo UserInfo
     */
    public static void putCurrentUserInfo(UserInfoVO userInfo) {
        CURRENT_USER_INFO.set(userInfo);
    }

    /**
     * 清除当前线程的UserInfo
     */
    public static void clearCurrentUserInfo() {
        CURRENT_USER_INFO.set(null);
    }

    /**
     * 无用户数据时的占位符, 在ThreadLocal中使用
     */
    public static UserInfoVO NONE = new UserInfoVO();
    /**
     * 当前线程的UserInfo
     */
    private static final ThreadLocal<UserInfoVO> CURRENT_USER_INFO = new ThreadLocal<>();

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

    public UserInfoVO setManagerFlag(Boolean managerFlag) {
        this.managerFlag = managerFlag;
        return this;
    }

    /**
     * @return 是否为组织/项目下的人
     */
    public Boolean getMemberFlag() {
        return memberFlag;
    }

    public UserInfoVO setMemberFlag(Boolean memberFlag) {
        this.memberFlag = memberFlag;
        return this;
    }
}
