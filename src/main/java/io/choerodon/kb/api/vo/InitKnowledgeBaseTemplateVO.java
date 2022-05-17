package io.choerodon.kb.api.vo;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author zhaotianxin
 * @since 2020/1/6
 */
public class InitKnowledgeBaseTemplateVO {
    @ApiModelProperty(value = "模板名称")
    private String name;

    @ApiModelProperty(value = "公开范围")
    private String openRange;

    @ApiModelProperty(value = "模板创建集合")
    private List<PageCreateVO> templatePage;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOpenRange() {
        return openRange;
    }

    public void setOpenRange(String openRange) {
        this.openRange = openRange;
    }

    public List<PageCreateVO> getTemplatePage() {
        return templatePage;
    }

    public void setTemplatePage(List<PageCreateVO> templatePage) {
        this.templatePage = templatePage;
    }
}
