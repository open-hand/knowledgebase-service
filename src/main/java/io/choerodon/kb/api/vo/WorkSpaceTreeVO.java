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
    private String type;
    private String  fileKey;

    // 前端onlyoffice展示时需要用到的字段
    /**
     * “fileType”：“docx”，
     * “key”：“Khirz6zTPdfd7”，
     * title”：“示例文档 Title.docx”，
     * “url”：“https://example.com/url -to-example-document.docx"
     */
    private String fileType;
    private String key;
    private String title;
    private String url;





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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
