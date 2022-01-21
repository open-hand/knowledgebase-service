package io.choerodon.kb.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import javax.persistence.Column;

/**
 * @Author: CaiShuangLian
 * @Date: 2022/01/17
 * @Description:覆盖WorkSpaceShareVo，增加数据库是否取消分享字段
 */
public class WorkSpaceShareVO {
    @ApiModelProperty("主键id")
    @Encrypt
    private Long id;
    @ApiModelProperty("工作空间ID")
    @Encrypt
    private Long workspaceId;
    @ApiModelProperty("token")
    private String token;
    @ApiModelProperty("分享类型")
    private String type;
    @ApiModelProperty("乐观锁版本号")
    private Long objectVersionNumber;
    @ApiModelProperty("是否能分享")
    private Boolean enabled;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWorkspaceId() {
        return this.workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getObjectVersionNumber() {
        return this.objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}

