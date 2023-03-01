package io.choerodon.kb.api.vo;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.kb.infra.enums.WorkSpaceType;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author shinan.chen
 * @since 2019/7/17
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageCreateWithoutContentVO {


    private Long refId;

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
    private String sourceType;
    private Long sourceId;

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

    private Long organizationId;

    @ApiModelProperty("是否为模版")
    private Boolean templateFlag;
    @ApiModelProperty("文件类型")
    private String templateCategory;

    public Long getOrganizationId() {
        return organizationId;
    }

    public PageCreateWithoutContentVO setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
        return this;
    }

    public Long getParentWorkspaceId() {
        return parentWorkspaceId;
    }

    public PageCreateWithoutContentVO setParentWorkspaceId(Long parentWorkspaceId) {
        this.parentWorkspaceId = parentWorkspaceId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public PageCreateWithoutContentVO setTitle(String title) {
        this.title = title;
        return this;
    }

    public Long getBaseId() {
        return baseId;
    }

    public PageCreateWithoutContentVO setBaseId(Long baseId) {
        this.baseId = baseId;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PageCreateWithoutContentVO setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getType() {
        return type;
    }

    public PageCreateWithoutContentVO setType(String type) {
        this.type = type;
        return this;
    }

    public String getFileKey() {
        return fileKey;
    }

    public PageCreateWithoutContentVO setFileKey(String fileKey) {
        this.fileKey = fileKey;
        return this;
    }

    public String getFilePath() {
        return filePath;
    }

    public PageCreateWithoutContentVO setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public String getFileType() {
        return fileType;
    }

    public PageCreateWithoutContentVO setFileType(String fileType) {
        this.fileType = fileType;
        return this;
    }

    public String getFileSourceType() {
        return fileSourceType;
    }

    public PageCreateWithoutContentVO setFileSourceType(String fileSourceType) {
        this.fileSourceType = fileSourceType;
        return this;
    }

    public String getSourceType() {
        return sourceType;
    }

    public PageCreateWithoutContentVO setSourceType(String sourceType) {
        this.sourceType = sourceType;
        return this;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public PageCreateWithoutContentVO setSourceId(Long sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    public Long getRefId() {
        return refId;
    }

    public PageCreateWithoutContentVO setRefId(Long refId) {
        this.refId = refId;
        return this;
    }

    public Boolean getTemplateFlag() {
        return templateFlag;
    }

    public PageCreateWithoutContentVO setTemplateFlag(Boolean templateFlag) {
        this.templateFlag = templateFlag;
        return this;
    }

    public String getTemplateCategory() {
        return templateCategory;
    }

    public PageCreateWithoutContentVO setTemplateCategory(String templateCategory) {
        this.templateCategory = templateCategory;
        return this;
    }
}
