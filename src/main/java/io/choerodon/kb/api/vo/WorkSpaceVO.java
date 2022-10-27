package io.choerodon.kb.api.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 工作空间VO
 * @author wangxiang 2022-04-26
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkSpaceVO {

    public WorkSpaceVO() {
    }

    public WorkSpaceVO(Long id, String name, String route, String type, String fileType) {
//        this.id = id;
//        this.name = name;
//        this.route = route;
//        this.type = type;
//        this.fileType = fileType;
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

    public WorkSpaceVO setApprove(Boolean approve) {
        this.approve = approve;
        return this;
    }

    public Long getId() {
        return id;
    }

    public WorkSpaceVO setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public WorkSpaceVO setName(String name) {
        this.name = name;
        return this;
    }

    public WorkSpaceVO setRoute(String route) {
        this.route = route;
        return this;
    }

    public String getRoute() {
        return route;
    }

    public WorkSpaceVO setChildren(List<WorkSpaceVO> children) {
        this.children = children;
        return this;
    }

    public List<WorkSpaceVO> getChildren() {
        return children;
    }

    public Long getBaseId() {
        return baseId;
    }

    public WorkSpaceVO setBaseId(Long baseId) {
        this.baseId = baseId;
        return this;
    }

    public String getType() {
        return type;
    }

    public WorkSpaceVO setType(String type) {
        this.type = type;
        return this;
    }

    public String getFileType() {
        return fileType;
    }

    public WorkSpaceVO setFileType(String fileType) {
        this.fileType = fileType;
        return this;
    }

    public String getBaseName() {
        return baseName;
    }

    public WorkSpaceVO setBaseName(String baseName) {
        this.baseName = baseName;
        return this;
    }
}
