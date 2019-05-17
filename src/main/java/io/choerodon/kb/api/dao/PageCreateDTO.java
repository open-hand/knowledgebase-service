package io.choerodon.kb.api.dao;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Zenger on 2019/4/29.
 */
public class PageCreateDTO {

    @NotNull
    @ApiModelProperty(value = "工作空间ID")
    private Long workspaceId;

    @NotNull
    @ApiModelProperty(value = "页面名称")
    private String title;

    @NotNull
    @ApiModelProperty(value = "页面内容")
    private String content;

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
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
