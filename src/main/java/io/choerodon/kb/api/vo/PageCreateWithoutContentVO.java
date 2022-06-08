package io.choerodon.kb.api.vo;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;

import javax.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.RequestParam;

import io.choerodon.kb.infra.enums.WorkSpaceType;

/**
 * @author shinan.chen
 * @since 2019/7/17
 */
public class PageCreateWithoutContentVO {
    //    @NotNull
    @ApiModelProperty(value = "父级工作空间ID，顶级目录则传0L")
    @Encrypt(/*value = EncryptConstants.TN_KB_WORKSPACE,*/ ignoreValue = "0")
    private Long parentWorkspaceId;
    @NotNull
    @ApiModelProperty(value = "页面名称")
    private String title;

    @ApiModelProperty(value = "页面描述")
    private String description;

    @ApiModelProperty(value = "该篇文档的类型")
//    @NotNull
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

    @ApiModelProperty("文件分片上传的路径")
    private String filePath;

    @ApiModelProperty("文件的类型：mp4,docx等等")
    private String fileType;

    @ApiModelProperty("文件来源：上传还是复制")
    private String fileSourceType;


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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileSourceType() {
        return fileSourceType;
    }

    public void setFileSourceType(String fileSourceType) {
        this.fileSourceType = fileSourceType;
    }
}
