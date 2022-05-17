package io.choerodon.kb.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import javax.validation.constraints.NotNull;

import io.choerodon.kb.infra.enums.WorkSpaceType;

/**
 * @author shinan.chen
 * @since 2019/7/17
 */
public class PageCreateWithoutContentVO {
    @NotNull
    @ApiModelProperty(value = "父级工作空间ID，顶级目录则传0L")
    @Encrypt(/*value = EncryptConstants.TN_KB_WORKSPACE,*/ ignoreValue = "0")
    private Long parentWorkspaceId;
    @NotNull
    @ApiModelProperty(value = "页面名称")
    private String title;

    @ApiModelProperty(value = "页面描述")
    private String description;

    @ApiModelProperty(value = "该篇文档的类型")
    @NotNull
    /**
     * {@link WorkSpaceType}
     */
    private String type;

    /**
     * 上传问件时必传
     */
    @ApiModelProperty(value = "文件key")
    private String fileKey;

    @ApiModelProperty(value = "知识库id")
    @Encrypt
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

    public Long getBaseId() {
        return baseId;
    }

    public void setBaseId(Long baseId) {
        this.baseId = baseId;
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

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }
}
