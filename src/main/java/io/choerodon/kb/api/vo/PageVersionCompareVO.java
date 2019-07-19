package io.choerodon.kb.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author shinan.chen
 * @since 2019/5/21
 */
public class PageVersionCompareVO {
    @ApiModelProperty(value = "第一个版本内容")
    private String firstVersionContent;
    @ApiModelProperty(value = "第二个版本内容")
    private String secondVersionContent;
    @ApiModelProperty(value = "diffContent")
    private String diffContent;

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

    public String getDiffContent() {
        return diffContent;
    }

    public void setDiffContent(String diffContent) {
        this.diffContent = diffContent;
    }
}
