package io.choerodon.kb.api.vo;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

public class WorkSpaceVO {

    public WorkSpaceVO() {
    }

    public WorkSpaceVO(Long id, String name, String route, String type, String fileType) {
        this.id = id;
        this.name = name;
        this.route = route;
        this.type = type;
        this.fileType = fileType;
    }

    @ApiModelProperty(value = "空间id")
    @Encrypt
    private Long id;

    @ApiModelProperty(value = "空间名称")
    private String name;

    @ApiModelProperty(value = "空间路径")
    private String route;

    @ApiModelProperty("workSpace的类型")
    private String type;

    @ApiModelProperty("file的后缀")
    private String fileType;

    @ApiModelProperty("子目录")
    private List<WorkSpaceVO> children;

    @ApiModelProperty("知识库id")
    @Encrypt
    private Long baseId;

    @ApiModelProperty("知识库名称")
    private String baseName;
    @ApiModelProperty("是否有权限")
    private Boolean approve;

    public Boolean getApprove() {
        return approve;
    }

    public void setApprove(Boolean approve) {
        this.approve = approve;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getRoute() {
        return route;
    }

    public void setChildren(List<WorkSpaceVO> children) {
        this.children = children;
    }

    public List<WorkSpaceVO> getChildren() {
        return children;
    }

    public Long getBaseId() {
        return baseId;
    }

    public void setBaseId(Long baseId) {
        this.baseId = baseId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getBaseName() {
        return baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }
}
