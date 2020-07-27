package io.choerodon.kb.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import java.util.List;

/**
 * Created by Zenger on 2019/5/6.
 */
public class WorkSpaceTreeVO {

    public WorkSpaceTreeVO() {
        this.isExpanded = false;
    }

    @ApiModelProperty(value = "工作空间ID")
    @Encrypt
    private Long id;
    @Encrypt
    @ApiModelProperty(value = "工作空间父级ID")
    private Long parentId;
    @ApiModelProperty(value = "是否展开")
    private Boolean isExpanded;
    @ApiModelProperty(value = "是否有子空间目录")
    private Boolean hasChildren;
    @ApiModelProperty(value = "工作空间信息")
    private Data data;
    @ApiModelProperty(value = "工作空间子目录ID")
    @Encrypt
    private List<Long> children;
    @ApiModelProperty(value = "创建用户id")
    @Encrypt
    private Long createdBy;
    @ApiModelProperty(value = "路由")
    private String route;
    @ApiModelProperty(value = "是否点击")
    private Boolean isClick;

    public Boolean getIsClick() {
        return isClick;
    }

    public void setIsClick(Boolean click) {
        isClick = click;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

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
