package io.choerodon.kb.api.dao;


import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Zenger on 2019/4/29.
 */
public class PageCreateAttachmentDTO {

    @NotNull
    @ApiModelProperty(value = "附件评论")
    private String comment;

    @NotNull
    @ApiModelProperty(value = "上传附件的页面版本id")
    private Long version;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}