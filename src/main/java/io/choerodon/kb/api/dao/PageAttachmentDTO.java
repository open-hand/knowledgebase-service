package io.choerodon.kb.api.dao;


import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Zenger on 2019/4/29.
 */
public class PageAttachmentDTO {

    @ApiModelProperty(value = "附件主键id")
    private Long id;

    @ApiModelProperty(value = "附件名称")
    private String title;

    @ApiModelProperty(value = "页面id")
    private Long pageId;

    @ApiModelProperty(value = "附件大小")
    private Long size;

    @ApiModelProperty(value = "上传附件的页面版本id")
    private Long version;

    @ApiModelProperty(value = "附件url")
    private String url;

    @ApiModelProperty(value = "版本号")
    private Long objectVersionNumber;

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

    public Long getPageId() {
        return pageId;
    }

    public void setPageId(Long pageId) {
        this.pageId = pageId;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }
}