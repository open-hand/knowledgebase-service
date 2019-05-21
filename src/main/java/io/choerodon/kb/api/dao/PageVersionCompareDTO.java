package io.choerodon.kb.api.dao;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author shinan.chen
 * @since 2019/5/21
 */
public class PageVersionCompareDTO {
    @ApiModelProperty(value = "第一个版本内容")
    private String firstVersionContent;
    @ApiModelProperty(value = "第二个版本内容")
    private String secondVersionContent;
    @ApiModelProperty(value = "diff")
    private TextDiffDTO diff;

    public String getFirstVersionContent() {
        return firstVersionContent;
    }

    public void setFirstVersionContent(String firstVersionContent) {
        this.firstVersionContent = firstVersionContent;
    }

    public String getSecondVersionContent() {
        return secondVersionContent;
    }

    public void setSecondVersionContent(String secondVersionContent) {
        this.secondVersionContent = secondVersionContent;
    }

    public TextDiffDTO getDiff() {
        return diff;
    }

    public void setDiff(TextDiffDTO diff) {
        this.diff = diff;
    }
}
