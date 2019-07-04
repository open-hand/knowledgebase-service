package io.choerodon.kb.api.dao;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author shinan.chen
 * @since 2019/7/3
 */
public class FullTextSearchResultDTO {
    @ApiModelProperty(value = "页面id")
    private Long pageId;
    @ApiModelProperty(value = "标题")
    private String title;
    @ApiModelProperty(value = "搜索后正文截取高亮部分")
    private String highlightContent;
    @ApiModelProperty(value = "正文")
    private String content;
    @ApiModelProperty(value = "搜索评分")
    private Float score;
    @ApiModelProperty(value = "项目id")
    private Long projectId;
    @ApiModelProperty(value = "组织id")
    private Long organizationId;

    public FullTextSearchResultDTO(Long pageId, String title, String content, Long projectId, Long organizationId) {
        this.pageId = pageId;
        this.title = title;
        this.content = content;
        this.projectId = projectId;
        this.organizationId = organizationId;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getHighlightContent() {
        return highlightContent;
    }

    public void setHighlightContent(String highlightContent) {
        this.highlightContent = highlightContent;
    }

    public Long getPageId() {
        return pageId;
    }

    public void setPageId(Long pageId) {
        this.pageId = pageId;
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
