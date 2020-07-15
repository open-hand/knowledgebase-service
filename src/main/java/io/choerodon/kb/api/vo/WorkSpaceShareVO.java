package io.choerodon.kb.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Created by Zenger on 2019/6/10.
 */
public class WorkSpaceShareVO {

    @ApiModelProperty(value = "主键id")
    @Encrypt
    private Long id;

    @ApiModelProperty(value = "工作空间ID")
    @Encrypt
    private Long workspaceId;

    @ApiModelProperty(value = "token")
    private String token;

    @ApiModelProperty(value = "分享类型")
    private String type;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }
}
