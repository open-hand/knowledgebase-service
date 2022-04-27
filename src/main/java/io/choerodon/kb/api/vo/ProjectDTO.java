package io.choerodon.kb.api.vo;

import java.util.Date;
import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;

import io.choerodon.mybatis.domain.AuditDomain;

/**
 * @author: 25499
 * @date: 2020/1/6 21:09
 * @description:
 */
public class ProjectDTO extends AuditDomain {

    private static final String CODE_REGULAR_EXPRESSION = "^[a-z](([a-z0-9]|-(?!-))*[a-z0-9])*$";

    private static final String PROJECT_NAME_REG = "^[-—\\.\\w\\s\\u4e00-\\u9fa5]{1,32}$";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "主键ID/非必填")
    private Long id;

    @ApiModelProperty(value = "项目名/必填")
    @NotEmpty(message = "error.project.name.empty")
    @Size(min = 1, max = 32, message = "error.project.code.size")
    @Pattern(regexp = PROJECT_NAME_REG, message = "error.project.name.regex")
    private String name;

    @ApiModelProperty(value = "项目编码/必填")
    @NotEmpty(message = "error.project.code.empty")
    @Size(min = 1, max = 14, message = "error.project.code.size")
    @Pattern(regexp = CODE_REGULAR_EXPRESSION, message = "error.project.code.illegal")
    private String code;

    @ApiParam(name = "organization_id", value = "组织id")
    @ApiModelProperty(value = "组织ID/非必填")
    private Long organizationId;

    @ApiModelProperty(value = "项目图标url/非必填")
    private String imageUrl;

    @ApiModelProperty(value = "是否启用/非必填")
    @Column(name = "is_enabled")
    private Boolean enabled;

    @ApiModelProperty(value = "项目类型code/非必填")
    private String type;

    @ApiModelProperty(value = "项目类型（遗留旧字段，一对一）:AGILE(敏捷项目),GENERAL(普通应用项目),PROGRAM(普通项目群)")
    private String category;

    @ApiModelProperty(value = "项目类型")
    private List<Long> categoryIds;

    @Transient
    private List<ProjectDTO> projects;

    @Transient
    @ApiModelProperty(value = "项目类型名称/非必填")
    private String typeName;
    @Transient
    @ApiModelProperty(value = "项目所属组织名称")
    private String organizationName;
    @Transient
    @ApiModelProperty(value = "项目所属组织编码")
    private String organizationCode;
    @Transient
    @ApiModelProperty(value = "项目创建人的用户名")
    private String createUserName;
    @Transient
    @ApiModelProperty(value = "项目创建人的头像")
    private String createUserImageUrl;
    @Transient
    @ApiModelProperty(value = "项目所在项目群名称")
    private String programName;

    private Long createdBy;

    private Date creationDate;

    @Transient
    @ApiModelProperty("敏捷项目问题前缀")
    private String agileProjectCode;

    @Transient
    @ApiModelProperty("敏捷项目乐观琐版本")
    private Long agileProjectObjectVersionNumber;

    @Transient
    @ApiModelProperty("敏捷项目乐观琐版本")
    private Long agileProjectId;

    @Transient
    @ApiModelProperty("是否有权限进入项目，默认为true")
    private Boolean isInto = true;

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

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getOrganizationCode() {
        return organizationCode;
    }

    public void setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
    }

    public List<ProjectDTO> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectDTO> projects) {
        this.projects = projects;
    }

    public List<Long> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public String getCreateUserName() {
        return createUserName;
    }

    public void setCreateUserName(String createUserName) {
        this.createUserName = createUserName;
    }

    public String getCreateUserImageUrl() {
        return createUserImageUrl;
    }

    public void setCreateUserImageUrl(String createUserImageUrl) {
        this.createUserImageUrl = createUserImageUrl;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getAgileProjectCode() {
        return agileProjectCode;
    }

    public void setAgileProjectCode(String agileProjectCode) {
        this.agileProjectCode = agileProjectCode;
    }

    public Long getAgileProjectObjectVersionNumber() {
        return agileProjectObjectVersionNumber;
    }

    public void setAgileProjectObjectVersionNumber(Long agileProjectObjectVersionNumber) {
        this.agileProjectObjectVersionNumber = agileProjectObjectVersionNumber;
    }

    public Long getAgileProjectId() {
        return agileProjectId;
    }

    public void setAgileProjectId(Long agileProjectId) {
        this.agileProjectId = agileProjectId;
    }

    @Override
    public Long getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    @Override
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Boolean getInto() {
        return isInto;
    }

    public void setInto(Boolean into) {
        isInto = into;
    }
}
