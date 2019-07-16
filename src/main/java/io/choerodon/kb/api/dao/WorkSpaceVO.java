package io.choerodon.kb.api.dao;

import java.util.List;

public class WorkSpaceVO {

    public WorkSpaceVO() {}

    public WorkSpaceVO(Long id, String name, String route) {
        this.id = id;
        this.name = name;
        this.route = route;
    }

    private Long id;

    private String name;

    private String route;

    private List<WorkSpaceVO> children;

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
}
