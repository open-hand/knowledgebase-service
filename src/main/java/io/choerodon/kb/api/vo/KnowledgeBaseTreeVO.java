package io.choerodon.kb.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author zhaotianxin
 * @since 2020/1/2
 */
public class KnowledgeBaseTreeVO {

    @ApiModelProperty(value = "知识库id")
    @Encrypt
    private Long id;

    @ApiModelProperty(value = "父级id")
    @Encrypt
    private Long parentId;

    @ApiModelProperty(value = "知识库名称")
    private String name;

    @ApiModelProperty(value = "是否为第一层级")
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
