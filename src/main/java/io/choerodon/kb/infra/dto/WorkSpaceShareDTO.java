package io.choerodon.kb.infra.dto;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;
import org.hzero.starter.keyencrypt.core.Encrypt;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @Author: CaiShuangLian
 * @Date: 2022/01/17
 * @Description:覆盖WorkSpaceShareDTO，增加当前知识库是否分享字段
 */
@ModifyAudit
@VersionAudit
@Table(name = "kb_workspace_share")
public class WorkSpaceShareDTO extends AuditDomain {
    @Id
    @GeneratedValue
    @Encrypt
    private Long id;
    @Encrypt
    private Long workspaceId;
    private String token;
    private String type;
    @Column(name = "is_enabled")
    private Boolean enabled;

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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
