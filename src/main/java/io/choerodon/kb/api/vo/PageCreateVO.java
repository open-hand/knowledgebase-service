package io.choerodon.kb.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import javax.validation.constraints.NotNull;

/**
 * Created by Zenger on 2019/4/29.
 */
public class PageCreateVO {
    private Long id;
    @NotNull
    @ApiModelProperty(value = "父级工作空间ID")
    @Encrypt(/*value = EncryptConstants.TN_KB_WORKSPACE,*/ ignoreValue = "0")
    private Long parentWorkspaceId;
    @NotNull
    @ApiModelProperty(value = "页面名称")
    private String title;
    @NotNull
    @ApiModelProperty(value = "页面内容")
    private String content;

    @ApiModelProperty(value = "页面描述")
    private String description;

    @ApiModelProperty(value = "知识库id")
    @Encrypt
    private Long baseId;

    @ApiModelProperty(value = "来源页面id")
    private Long sourcePageId;

    @ApiModelProperty("文件类型")
    private String type;
    @ApiModelProperty("是否为模版")
    private Boolean templateFlag;
    @ApiModelProperty("文件类型")
    private String templateCategory;

    public Long getSourcePageId() {
        return sourcePageId;
    }

    public void setSourcePageId(Long sourcePageId) {
        this.sourcePageId = sourcePageId;
    }

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
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBaseId() {
        return baseId;
    }

    public Boolean getTemplateFlag() {
        return templateFlag;
    }

    public PageCreateVO setTemplateFlag(Boolean templateFlag) {
        this.templateFlag = templateFlag;
        return this;
    }

    public String getTemplateCategory() {
        return templateCategory;
    }

    public PageCreateVO setTemplateCategory(String templateCategory) {
        this.templateCategory = templateCategory;
        return this;
    }

    public PageCreateVO(@NotNull Long parentWorkspaceId, @NotNull String title, @NotNull String content) {
        this.parentWorkspaceId = parentWorkspaceId;
        this.title = title;
        this.content = content;
    }

    public PageCreateVO(@NotNull Long parentWorkspaceId, @NotNull String title, String description, @NotNull String content, String type) {
        this.parentWorkspaceId = parentWorkspaceId;
        this.title = title;
        this.description = description;
        this.content = content;
        this.type = type;
    }

    public PageCreateVO(@NotNull Long parentWorkspaceId, @NotNull String title, @NotNull String content, Long baseId, String type) {
        this.parentWorkspaceId = parentWorkspaceId;
        this.title = title;
        this.content = content;
        this.baseId = baseId;
        this.type = type;
    }

    public void setBaseId(Long baseId) {
        this.baseId = baseId;
    }

    public PageCreateVO() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}