package io.choerodon.kb.api.dao;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Zenger on 2019/5/6.
 */
public class WorkSpaceTreeDTO {

    @ApiModelProperty(value = "工作空间ID")
    private Long id;

    @ApiModelProperty(value = "是否有子空间目录")
    private Boolean hasChildren;

    @ApiModelProperty(value = "工作空间信息")
    private Data data;

    @ApiModelProperty(value = "工作空间子目录ID")
    private List<Long> children;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(Boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public List<Long> getChildren() {
        return children;
    }

    public void setChildren(List<Long> children) {
        this.children = children;
    }

    public static class Data {
        private String title;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
