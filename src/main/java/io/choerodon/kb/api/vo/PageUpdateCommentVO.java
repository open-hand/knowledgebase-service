package io.choerodon.kb.api.vo;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Created by Zenger on 2019/4/29.
 */
public class PageUpdateCommentVO {

    @NotNull
    @ApiModelProperty(value = "页面id")
    @Encrypt
    private Long pageId;

    @NotNull
    @ApiModelProperty(value = "评论内容")
    private String comment;

    @NotNull
    @ApiModelProperty(value = "乐观锁版本号")
    private Long objectVersionNumber;

    public Long getPageId() {
        return pageId;
    }

    public void setPageId(Long pageId) {
        this.pageId = pageId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }
}
