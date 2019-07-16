package io.choerodon.kb.api.dao;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

/**
 * Created by Zenger on 2019/4/29.
 */
public class PageCreateVO {

    @NotNull
    @ApiModelProperty(value = "父级工作空间ID")
    private Long parentWorkspaceId;

    @NotNull
    @ApiModelProperty(value = "页面名称")
    private String title;

    @NotNull
    @ApiModelProperty(value = "页面内容")
    private String content;

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
}
