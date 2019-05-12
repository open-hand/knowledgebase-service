package io.choerodon.kb.api.dao;

import java.util.Date;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Zenger on 2019/4/29.
 */
public class PageDTO {

    @ApiModelProperty(value = "页面ID")
    private Long pageId;

    @ApiModelProperty(value = "页面标题")
    private String title;

    @ApiModelProperty(value = "页面内容")
    private String content;

    @ApiModelProperty(value = "页面版本ID")
    private Long versionId;

    @ApiModelProperty(value = "工作空间ID")
    private Long workSpaceId;

    @ApiModelProperty(value = "工作空间父级ID")
    private Long workSpaceParentId;

    @ApiModelProperty(value = "工作空间路径")
    private String route;

    @ApiModelProperty(value = "引用类型")
    private String referenceType;

    @ApiModelProperty(value = "引用地址")
    private String referenceUrl;

    @ApiModelProperty(value = "页面更新审计字段")
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

    public Long getPageId() {
        return pageId;
    }

    public void setPageId(Long pageId) {
        this.pageId = pageId;
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

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public Long getWorkSpaceId() {
        return workSpaceId;
    }

    public void setWorkSpaceId(Long workSpaceId) {
        this.workSpaceId = workSpaceId;
    }

    public Long getWorkSpaceParentId() {
        return workSpaceParentId;
    }

    public void setWorkSpaceParentId(Long workSpaceParentId) {
        this.workSpaceParentId = workSpaceParentId;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getReferenceUrl() {
        return referenceUrl;
    }

    public void setReferenceUrl(String referenceUrl) {
        this.referenceUrl = referenceUrl;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
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

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(Long lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }
}
