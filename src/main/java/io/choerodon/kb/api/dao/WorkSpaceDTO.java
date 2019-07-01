package io.choerodon.kb.api.dao;

import java.util.List;

public class WorkSpaceDTO {

    public WorkSpaceDTO() {}

    public WorkSpaceDTO(Long id, String name, String route) {
        this.id = id;
        this.name = name;
        this.route = route;
    }

    private Long id;

    private String name;

    private String route;

    private List<WorkSpaceDTO> children;

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

    public void setChildren(List<WorkSpaceDTO> children) {
        this.children = children;
    }

    public List<WorkSpaceDTO> getChildren() {
        return children;
    }
}
