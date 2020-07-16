package io.choerodon.kb.infra.dto;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;
import org.hzero.starter.keyencrypt.core.Encrypt;

import javax.persistence.*;

/**
 * Created by Zenger on 2019/4/29.
 */
@ModifyAudit
@VersionAudit
@Table(name = "kb_workspace")
public class WorkSpaceDTO extends AuditDomain {

    @Id
    @GeneratedValue
    @Encrypt
    private Long id;
    private String name;
    private Long organizationId;
    private Long projectId;
    private String route;
    @Encrypt
    private Long parentId;
    private String rank;
    @Encrypt
    private Long bookId;
    @Column(name = "is_delete")
    private Boolean delete;
    @Encrypt
    private Long baseId;

    private String description;

    @Transient
    @Encrypt
    private Long pageId;

    @Transient
    private Long workPageId;

    public Long getPageId() {
        return pageId;
    }

    public Long getWorkPageId() {
        return workPageId;
    }

    public void setWorkPageId(Long workPageId) {
        this.workPageId = workPageId;
    }

    public void setPageId(Long pageId) {
        this.pageId = pageId;
    }

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

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Boolean getDelete() {
        return delete;
    }

    public void setDelete(Boolean delete) {
        this.delete = delete;
    }

    public Long getBaseId() {
        return baseId;
    }

    public void setBaseId(Long baseId) {
        this.baseId = baseId;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
