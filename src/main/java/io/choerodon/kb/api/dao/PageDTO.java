package io.choerodon.kb.api.dao;

import java.util.Date;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Zenger on 2019/4/29.
 */
public class PageDTO {

    @ApiModelProperty(value = "工作空间目录结构")
    private WorkSpaceTreeDTO workSpace;

    @ApiModelProperty(value = "页面信息")
    private PageInfo pageInfo;

    @ApiModelProperty(value = "工作空间路径")
    private String route;

    @ApiModelProperty(value = "分享链接token")
    private String token;

    @ApiModelProperty(value = "引用类型")
    private String referenceType;

    @ApiModelProperty(value = "引用地址")
    private String referenceUrl;

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

    public WorkSpaceTreeDTO getWorkSpace() {
        return workSpace;
    }

    public void setWorkSpace(WorkSpaceTreeDTO workSpace) {
        this.workSpace = workSpace;
    }

    public PageInfo getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public static class PageInfo {

        @ApiModelProperty(value = "页面ID")
        private Long id;

        @ApiModelProperty(value = "页面标题")
        private String title;

        @ApiModelProperty(value = "页面内容源码")
        private String souceContent;

        @ApiModelProperty(value = "页面内容")
        private String content;

        @ApiModelProperty(value = "页面版本ID")
        private Long versionId;

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

        public String getSouceContent() {
            return souceContent;
        }

        public void setSouceContent(String souceContent) {
            this.souceContent = souceContent;
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
    }


}
