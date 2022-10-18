package io.choerodon.kb.infra.dto;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Created by Zenger on 2019/4/29.
 */
@ModifyAudit
@VersionAudit
@Table(name = "kb_workspace")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkSpaceDTO extends AuditDomain {

    public static final String FIELD_ID = "id";
    public static final String FIELD_DELETE = "delete";
    public static final String FIELD_ROUTE = "route";

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

    private String type;
    private String fileKey;

    @Transient
    @Encrypt
    private Long pageId;

    @Transient
    @ApiModelProperty(value = "知识库名称")
    private String baseName;
    @Transient
    private Long workPageId;

    public Long getPageId() {
        return pageId;
    }

    public Long getWorkPageId() {
        return workPageId;
    }

    public WorkSpaceDTO setWorkPageId(Long workPageId) {
        this.workPageId = workPageId;
        return this;
    }

    public WorkSpaceDTO setPageId(Long pageId) {
        this.pageId = pageId;
        return this;
    }

    public Long getId() {
        return id;
    }

    public WorkSpaceDTO setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public WorkSpaceDTO setName(String name) {
        this.name = name;
        return this;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public WorkSpaceDTO setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
        return this;
    }

    public Long getProjectId() {
        return projectId;
    }

    public WorkSpaceDTO setProjectId(Long projectId) {
        this.projectId = projectId;
        return this;
    }

    public String getRoute() {
        return route;
    }

    public WorkSpaceDTO setRoute(String route) {
        this.route = route;
        return this;
    }

    public Long getParentId() {
        return parentId;
    }

    public WorkSpaceDTO setParentId(Long parentId) {
        this.parentId = parentId;
        return this;
    }

    public String getRank() {
        return rank;
    }

    public WorkSpaceDTO setRank(String rank) {
        this.rank = rank;
        return this;
    }

    public Long getBookId() {
        return bookId;
    }

    public WorkSpaceDTO setBookId(Long bookId) {
        this.bookId = bookId;
        return this;
    }

    public Boolean getDelete() {
        return delete;
    }

    public WorkSpaceDTO setDelete(Boolean delete) {
        this.delete = delete;
        return this;
    }

    public Long getBaseId() {
        return baseId;
    }

    public WorkSpaceDTO setBaseId(Long baseId) {
        this.baseId = baseId;
        return this;
    }

    public String getDescription() {

        return description;
    }

    public WorkSpaceDTO setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getType() {
        return type;
    }

    public WorkSpaceDTO setType(String type) {
        this.type = type;
        return this;
    }


    public String getFileKey() {
        return fileKey;
    }

    public WorkSpaceDTO setFileKey(String fileKey) {
        this.fileKey = fileKey;
        return this;
    }

    public String getBaseName() {
        return baseName;
    }

    public WorkSpaceDTO setBaseName(String baseName) {
        this.baseName = baseName;
        return this;
    }
}
