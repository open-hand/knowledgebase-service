package io.choerodon.kb.api.vo;

import io.swagger.annotations.ApiModelProperty;
import java.util.List;

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


    @Encrypt
    private Long id;

    private String name;

    private String route;

    @ApiModelProperty("workSpace的类型")
    private String type;

    @ApiModelProperty("file的后缀")
    private String fileType;

    private List<WorkSpaceVO> children;

    @Encrypt
    private Long baseId;

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
}
