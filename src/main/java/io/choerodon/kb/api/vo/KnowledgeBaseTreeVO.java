package io.choerodon.kb.api.vo;

/**
 * @author zhaotianxin
 * @since 2020/1/2
 */
public class KnowledgeBaseTreeVO {
   private Long id;

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
