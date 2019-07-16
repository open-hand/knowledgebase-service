package io.choerodon.kb.api.dao;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

/**
 * @author shinan.chen
 * @since 2019/6/26
 */
public class PageAutoSaveVO {
    @NotNull
    @ApiModelProperty(value = "页面内容")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
