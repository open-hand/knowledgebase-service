package io.choerodon.kb.api.vo;

import java.util.Objects;

import io.choerodon.kb.infra.constants.EncryptConstants;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.hzero.starter.keyencrypt.core.IEncryptionService;

import javax.validation.constraints.NotNull;

/**
 * Created by Zenger on 2019/4/29.
 */
public class PageCreateVO {
    private Long id;
    @NotNull
    @ApiModelProperty(value = "父级工作空间ID")
    @Encrypt(value = EncryptConstants.TN_KB_WORKSPACE, ignoreValue = "0")
    private Long parentWorkspaceId;
    @NotNull
    @ApiModelProperty(value = "页面名称")
    private String title;
    @NotNull
    @ApiModelProperty(value = "页面内容")
    private String content;

    private String description;
    @Encrypt/*(EncryptConstants.TN_KB_KNOWLEDGE_BASE)*/
    private Long baseId;

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

    public PageCreateVO(@NotNull Long parentWorkspaceId, @NotNull String title, @NotNull String content) {
        this.parentWorkspaceId = parentWorkspaceId;
        this.title = title;
        this.content = content;
    }

    public PageCreateVO(@NotNull Long parentWorkspaceId, @NotNull String title, String description,@NotNull String content) {
        this.parentWorkspaceId = parentWorkspaceId;
        this.title = title;
        this.description = description;
        this.content = content;
    }

    public PageCreateVO(@NotNull Long parentWorkspaceId, @NotNull String title, @NotNull String content, Long baseId) {
        this.parentWorkspaceId = parentWorkspaceId;
        this.title = title;
        this.content = content;
        this.baseId = baseId;
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
}