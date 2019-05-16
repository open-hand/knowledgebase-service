package io.choerodon.kb.api.dao;

/**
 * Created by Zenger on 2019/5/14.
 */
public class WorkSpaceProjectTreeDTO {

    private Long projectId;
    private String projectName;
    private WorkSpaceFirstTreeDTO workSpace;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public WorkSpaceFirstTreeDTO getWorkSpace() {
        return workSpace;
    }

    public void setWorkSpace(WorkSpaceFirstTreeDTO workSpace) {
        this.workSpace = workSpace;
    }
}
