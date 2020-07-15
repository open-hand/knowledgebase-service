package io.choerodon.kb.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author shinan.chen
 * @since 2019/5/16
 */
public class PageVersionInfoVO {
    @ApiModelProperty(value = "版本id")
    @Encrypt
    private Long id;
    @ApiModelProperty(value = "版本名称")
    private String name;
    @ApiModelProperty(value = "页面id")
    @Encrypt
    private Long pageId;
    @ApiModelProperty(value = "标题")
    private String title;
    @ApiModelProperty(value = "内容")
    private String content;
    @ApiModelProperty(value = "乐观锁")
    private Long objectVersionNumber;

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

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }
}
