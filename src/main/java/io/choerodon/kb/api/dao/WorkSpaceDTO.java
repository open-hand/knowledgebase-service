package io.choerodon.kb.api.dao;

import java.util.List;

public class WorkSpaceDTO {

    public WorkSpaceDTO() {}

    public WorkSpaceDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    private Long id;

    private String name;

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

    public void setChildren(List<WorkSpaceDTO> children) {
        this.children = children;
    }

    public List<WorkSpaceDTO> getChildren() {
        return children;
    }
}
