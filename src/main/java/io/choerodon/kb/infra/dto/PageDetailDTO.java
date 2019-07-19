package io.choerodon.kb.infra.dto;

import java.util.Date;

/**
 * Created by Zenger on 2019/4/30.
 */
public class PageDetailDTO {

    private Long pageId;
    private String title;
    private String content;
    private Long latestVersionId;

    private Long workSpaceId;
    private Long workSpaceParentId;
    private String route;

    private String referenceType;
    private String referenceUrl;

    private Long workSpaceCreatedBy;
    private Long pageObjectVersionNumber;
    private Long pageCreatedBy;
    private Date pageCreationDate;
    private Long pageLastUpdatedBy;
    private Date pageLastUpdateDate;

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

    public Long getLatestVersionId() {
        return latestVersionId;
    }

    public void setLatestVersionId(Long latestVersionId) {
        this.latestVersionId = latestVersionId;
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

    public Long getWorkSpaceCreatedBy() {
        return workSpaceCreatedBy;
    }

    public void setWorkSpaceCreatedBy(Long workSpaceCreatedBy) {
        this.workSpaceCreatedBy = workSpaceCreatedBy;
    }

    public Long getPageObjectVersionNumber() {
        return pageObjectVersionNumber;
    }

    public void setPageObjectVersionNumber(Long pageObjectVersionNumber) {
        this.pageObjectVersionNumber = pageObjectVersionNumber;
    }

    public Long getPageCreatedBy() {
        return pageCreatedBy;
    }

    public void setPageCreatedBy(Long pageCreatedBy) {
        this.pageCreatedBy = pageCreatedBy;
    }

    public Date getPageCreationDate() {
        return pageCreationDate;
    }

    public void setPageCreationDate(Date pageCreationDate) {
        this.pageCreationDate = pageCreationDate;
    }

    public Long getPageLastUpdatedBy() {
        return pageLastUpdatedBy;
    }

    public void setPageLastUpdatedBy(Long pageLastUpdatedBy) {
        this.pageLastUpdatedBy = pageLastUpdatedBy;
    }

    public Date getPageLastUpdateDate() {
        return pageLastUpdateDate;
    }

    public void setPageLastUpdateDate(Date pageLastUpdateDate) {
        this.pageLastUpdateDate = pageLastUpdateDate;
    }
}
