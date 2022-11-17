package io.choerodon.kb.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;
import org.hzero.starter.keyencrypt.core.Encrypt;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Created by Zenger on 2019/4/29.
 */
@ModifyAudit
@VersionAudit
@Table(name = "kb_page")
public class PageDTO extends AuditDomain {

    public static final String FIELD_TITLE = "title";
    public static final String FIELD_IS_SYNC_ES = "isSyncEs";
    public static final String FIELD_LATEST_VERSION_ID = "latestVersionId";
    @Id
    @GeneratedValue
    @Encrypt
    private Long id;
    private String title;
    private Long latestVersionId;
    private Long organizationId;
    private Long projectId;
    private Boolean isSyncEs;

    public Boolean getIsSyncEs() {
        return isSyncEs;
    }

    public void setIsSyncEs(Boolean syncEs) {
        isSyncEs = syncEs;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getLatestVersionId() {
        return latestVersionId;
    }

    public void setLatestVersionId(Long latestVersionId) {
        this.latestVersionId = latestVersionId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
