package io.choerodon.kb.domain.kb.entity;

import java.util.Date;

/**
 * Created by Zenger on 2019/4/30.
 */
public class PageDetailE {

    private Long pageId;
    private String title;
    private String content;
    private Long versionId;
    private Long workSpaceId;
    private Long workSpaceParentId;
    private String route;
    private String referenceType;
    private String referenceUrl;
    private Long objectVersionNumber;
    private Long createdBy;
    private String createName;
    private Date creationDate;
    private Long lastUpdatedBy;
    private String lastUpdatedName;
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

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
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

    public String getCreateName() {
        return createName;
    }

    public void setCreateName(String createName) {
        this.createName = createName;
    }

    public String getLastUpdatedName() {
        return lastUpdatedName;
    }

    public void setLastUpdatedName(String lastUpdatedName) {
        this.lastUpdatedName = lastUpdatedName;
    }
}
