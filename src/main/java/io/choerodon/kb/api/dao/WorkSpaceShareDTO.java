package io.choerodon.kb.api.dao;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Zenger on 2019/6/10.
 */
public class WorkSpaceShareDTO {

    @ApiModelProperty(value = "主键id")
    private Long id;

    @ApiModelProperty(value = "工作空间ID")
    private Long workspaceId;

    @ApiModelProperty(value = "token")
    private String token;

    @ApiModelProperty(value = "是否分享子页面")
    private Boolean isContain;

    @ApiModelProperty(value = "乐观锁版本号")
    private Long objectVersionNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getContain() {
        return isContain;
    }

    public void setContain(Boolean contain) {
        isContain = contain;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }
}
