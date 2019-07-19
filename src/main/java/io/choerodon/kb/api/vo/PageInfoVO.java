package io.choerodon.kb.api.vo;

import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * @author shinan.chen
 * @since 2019/6/5
 */
public class PageInfoVO {
    @ApiModelProperty(value = "页面ID")
    private Long id;
    @ApiModelProperty(value = "页面标题")
    private String title;
    @ApiModelProperty(value = "页面内容源码")
    private String content;
    @ApiModelProperty(value = "页面内容渲染后")
    private String drawContent;
    @ApiModelProperty(value = "组织id")
    private Long organizationId;
    @ApiModelProperty(value = "项目id")
    private Long projectId;
    @ApiModelProperty(value = "应用于全文检索，根据检索内容高亮内容")
    private String highlightContent;

    @ApiModelProperty(value = "乐观锁版本号")
    private Long objectVersionNumber;
    @ApiModelProperty(value = "页面创建人id")
    private Long createdBy;
    @ApiModelProperty(value = "页面创建人")
    private String createName;
    @ApiModelProperty(value = "页面创建日期")
    private Date creationDate;
    @ApiModelProperty(value = "页面最后修改人id")
    private Long lastUpdatedBy;
    @ApiModelProperty(value = "页面最后修改人")
    private String lastUpdatedName;
    @ApiModelProperty(value = "页面最后修改日期")
    private Date lastUpdateDate;

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDrawContent() {
        return drawContent;
    }

    public void setDrawContent(String drawContent) {
        this.drawContent = drawContent;
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

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreateName() {
        return createName;
    }

    public void setCreateName(String createName) {
        this.createName = createName;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Long getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(Long lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public String getLastUpdatedName() {
        return lastUpdatedName;
    }

    public void setLastUpdatedName(String lastUpdatedName) {
        this.lastUpdatedName = lastUpdatedName;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getHighlightContent() {
        return highlightContent;
    }

    public void setHighlightContent(String highlightContent) {
        this.highlightContent = highlightContent;
    }
}
