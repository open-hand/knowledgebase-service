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
@Table(name = "kb_page_comment")
public class PageCommentDO extends AuditDomain {

    @Id
    @GeneratedValue
    private Long id;
    private Long pageId;
    private String comment;
    private Long parentId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPageId() {
        return pageId;
    }

    public void setPageId(Long pageId) {
        this.pageId = pageId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}
