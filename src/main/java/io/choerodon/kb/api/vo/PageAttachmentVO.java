package io.choerodon.kb.api.vo;


import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Created by Zenger on 2019/4/29.
 */
public class PageAttachmentVO {

    @ApiModelProperty(value = "附件主键id")
    @Encrypt
    private Long id;

    @ApiModelProperty(value = "附件名称")
    private String name;

    @ApiModelProperty(value = "页面id")
    @Encrypt
    private Long pageId;

    @ApiModelProperty(value = "附件大小")
    private Long size;

    @ApiModelProperty(value = "附件url")
    private String url;

    @ApiModelProperty(value = "乐观锁版本号")
    private Long objectVersionNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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