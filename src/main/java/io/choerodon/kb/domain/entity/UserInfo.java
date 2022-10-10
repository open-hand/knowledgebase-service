package io.choerodon.kb.domain.entity;

import java.util.Set;

/**
 * @author superlee
 * @since 2022-10-09
 */
public class UserInfo {

    private Long userId;

    private Set<Long> roleIds;

    private Set<Long> workGroupIds;

    private Boolean adminFlag;

    public Boolean getAdminFlag() {
        return adminFlag;
    }

    public void setAdminFlag(Boolean adminFlag) {
        this.adminFlag = adminFlag;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Set<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(Set<Long> roleIds) {
        this.roleIds = roleIds;
    }

    public Set<Long> getWorkGroupIds() {
        return workGroupIds;
    }

    public void setWorkGroupIds(Set<Long> workGroupIds) {
        this.workGroupIds = workGroupIds;
    }
}
