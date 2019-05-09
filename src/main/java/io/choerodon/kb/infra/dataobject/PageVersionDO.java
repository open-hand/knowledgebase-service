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
@Table(name = "kb_page_version")
public class PageVersionDO extends AuditDomain {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private Long pageId;

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

    public Long getPageId() {
        return pageId;
    }

    public void setPageId(Long pageId) {
        this.pageId = pageId;
    }
}
