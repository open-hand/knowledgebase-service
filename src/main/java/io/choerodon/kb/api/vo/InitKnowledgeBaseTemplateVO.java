package io.choerodon.kb.api.vo;

import java.util.List;

/**
 * @author zhaotianxin
 * @since 2020/1/6
 */
public class InitKnowledgeBaseTemplateVO {
    private String name;

    private String openRange;

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
