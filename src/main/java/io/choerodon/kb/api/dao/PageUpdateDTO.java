package io.choerodon.kb.api.dao;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Zenger on 2019/4/29.
 */
public class PageUpdateDTO {

    @ApiModelProperty(value = "页面名称")
    private String title;

    @ApiModelProperty(value = "页面内容")
    private String content;

    @ApiModelProperty(value = "引用地址")
    private String referenceUrl;

    @ApiModelProperty(value = "修改类型")
    private Boolean minorEdit;

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

    public String getReferenceUrl() {
        return referenceUrl;
    }

    public void setReferenceUrl(String referenceUrl) {
        this.referenceUrl = referenceUrl;
    }

    public Boolean getMinorEdit() {
        return minorEdit;
    }

    public void setMinorEdit(Boolean minorEdit) {
        this.minorEdit = minorEdit;
    }
}
