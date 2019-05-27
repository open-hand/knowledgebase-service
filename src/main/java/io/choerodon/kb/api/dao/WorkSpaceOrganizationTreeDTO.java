package io.choerodon.kb.api.dao;

/**
 * Created by Zenger on 2019/5/14.
 */
public class WorkSpaceOrganizationTreeDTO {

    private Long orgId;
    private String orgName;
    private WorkSpaceFirstTreeDTO workSpace;

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public WorkSpaceFirstTreeDTO getWorkSpace() {
        return workSpace;
    }

    public void setWorkSpace(WorkSpaceFirstTreeDTO workSpace) {
        this.workSpace = workSpace;
    }
}
