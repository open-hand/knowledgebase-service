package io.choerodon.kb.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author zhaotianxin
 * @since 2019/12/30
 */
@ModifyAudit
@VersionAudit
@Table(name = "kb_knowledge_base")
public class KnowledgeBaseDTO extends AuditDomain {
    @Id
    @GeneratedValue
    @Encrypt
    private Long id;

    private String name;

    private String  description;
    @ApiModelProperty("公开范围类型:私有、公开到组织、某些项目")
    private String openRange;

    @ApiModelProperty("公开到项目记录")
    private String rangeProject;

    private Long projectId;

    private Long organizationId;

    private Boolean isDelete;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getOpenRange() {
        return openRange;
    }

    public void setOpenRange(String openRange) {
        this.openRange = openRange;
    }

    public String getRangeProject() {
        return rangeProject;
    }

    public void setRangeProject(String rangeProject) {
        this.rangeProject = rangeProject;
    }

    public Boolean getDelete() {
        return isDelete;
    }

    public void setDelete(Boolean delete) {
        isDelete = delete;
    }

    public KnowledgeBaseDTO() {
    }

    public KnowledgeBaseDTO(String name, String description, String openRange, Long projectId, Long organizationId) {
        this.name = name;
        this.description = description;
        this.openRange = openRange;
        this.projectId = projectId;
        this.organizationId = organizationId;
    }
}
