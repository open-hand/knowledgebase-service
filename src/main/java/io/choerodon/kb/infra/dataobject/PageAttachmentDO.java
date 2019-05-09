package io.choerodon.kb.infra.dataobject;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Created by Zenger on 2019/4/29.
 */
@VersionAudit
@ModifyAudit
@Table(name = "kb_page_attachment")
public class PageAttachmentDO extends AuditDomain {

    @Id
    @GeneratedValue
    private Long id;
    private String title;
    private Long pageId;
    private Long size;
    private String attachmentComment;
    private Long version;
    private String url;

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

    public Long getPageId() {
        return pageId;
    }

    public void setPageId(Long pageId) {
        this.pageId = pageId;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getAttachmentComment() {
        return attachmentComment;
    }

    public void setAttachmentComment(String attachmentComment) {
        this.attachmentComment = attachmentComment;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
