package io.choerodon.kb.api.vo;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

/**
 * Created by Zenger on 2019/4/29.
 */
public class PageCreateVO {
    private Long Id;
    @NotNull
    @ApiModelProperty(value = "父级工作空间ID")
    private Long parentWorkspaceId;
    @NotNull
    @ApiModelProperty(value = "页面名称")
    private String title;
    @NotNull
    @ApiModelProperty(value = "页面内容")
    private String content;

    private Long BaseId;

    public Long getParentWorkspaceId() {
        return parentWorkspaceId;
    }

    public void setParentWorkspaceId(Long parentWorkspaceId) {
        this.parentWorkspaceId = parentWorkspaceId;
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

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public Long getBaseId() {
        return BaseId;
    }

    public void setBaseId(Long baseId) {
        BaseId = baseId;
    }
}
