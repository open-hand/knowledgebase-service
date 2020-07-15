package io.choerodon.kb.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Created by Zenger on 2019/4/29.
 */
@ModifyAudit
@VersionAudit
@Table(name = "kb_tag")
public class TagDTO extends AuditDomain {

    @Id
    @GeneratedValue
    @Encrypt
    private Long id;
    private String name;

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
}
