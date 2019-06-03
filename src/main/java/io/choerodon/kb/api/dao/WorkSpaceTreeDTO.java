package io.choerodon.kb.api.dao;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * Created by Zenger on 2019/5/6.
 */
public class WorkSpaceTreeDTO {

    public WorkSpaceTreeDTO() {
        this.isExpanded = false;
    }

    @ApiModelProperty(value = "工作空间ID")
    private Long id;

    @ApiModelProperty(value = "工作空间父级ID")
    private Long parentId;

    @ApiModelProperty(value = "是否展开")
    private Boolean isExpanded;

    @ApiModelProperty(value = "是否有子空间目录")
    private Boolean hasChildren;

    @ApiModelProperty(value = "工作空间信息")
    private Data data;

    @ApiModelProperty(value = "工作空间子目录ID")
    private List<Long> children;

    @ApiModelProperty(value = "创建用户id")
    private Long createdBy;

    public Boolean getExpanded() {
        return isExpanded;
    }

    public void setExpanded(Boolean expanded) {
        isExpanded = expanded;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

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

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Boolean getIsExpanded() {
        return isExpanded;
    }

    public void setIsExpanded(Boolean expanded) {
        isExpanded = expanded;
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
