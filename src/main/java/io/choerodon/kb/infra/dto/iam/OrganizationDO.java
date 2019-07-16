package io.choerodon.kb.infra.dto.iam;


/**
 * Created by Zenger on 2019/4/29.
 */
public class OrganizationDO {

    private Long id;
    private String name;
    private String code;
    private Boolean enabled;
    private Long projectCount;
    private Long userId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Long getProjectCount() {
        return projectCount;
    }

    public void setProjectCount(Long projectCount) {
        this.projectCount = projectCount;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "OrganizationDO{" +
                "id=" + id +
                ", name=" + name +
                ", code=" + code +
                ", userId=" + userId +
                ", enabled=" + enabled +
                '}';
    }
}
