package io.choerodon.kb.api.vo;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author zhaotianxin
 * @since 2020/1/2
 */
public class KnowledgeBaseTreeVO {

   @Encrypt
   private Long id;

   @Encrypt
   private Long parentId;

   private String name;

   private Boolean topLeavl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getTopLeavl() {
        return topLeavl;
    }

    public void setTopLeavl(Boolean topLeavl) {
        this.topLeavl = topLeavl;
    }
}
