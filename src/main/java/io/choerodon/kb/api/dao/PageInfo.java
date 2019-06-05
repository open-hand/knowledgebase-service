package io.choerodon.kb.api.dao;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author shinan.chen
 * @since 2019/6/5
 */
public class PageInfo {
    @ApiModelProperty(value = "页面ID")
    private Long id;
    @ApiModelProperty(value = "页面标题")
    private String title;
    @ApiModelProperty(value = "页面内容源码")
    private String content;
    @ApiModelProperty(value = "页面内容")
    private String drawContent;
    @ApiModelProperty(value = "组织id")
    private Long organizationId;
    @ApiModelProperty(value = "项目id")
    private Long projectId;

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDrawContent() {
        return drawContent;
    }

    public void setDrawContent(String drawContent) {
        this.drawContent = drawContent;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
