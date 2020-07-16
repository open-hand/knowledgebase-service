package io.choerodon.kb.api.vo;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author: 25499
 * @date: 2020/1/2 10:29
 * @description:
 */
public class KnowledgeBaseListVO {
    @Encrypt
    private Long id;

    private String name;

    private String  description;

    @ApiModelProperty("公开范围类型:私有、公开到组织、")
    private String openRange;
    @ApiModelProperty("公开到项目记录")
    private String rangeProject;

    @ApiModelProperty("来源")
    private String source;

    private Long projectId;

    private Long organizationId;

    private Long objectVersionNumber;
    @ApiModelProperty(value = "空间共享对象列表")
    private List<WorkSpaceRecentVO> workSpaceRecents;

    public String getRangeProject() {
        return rangeProject;
    }

    public void setRangeProject(String rangeProject) {
        this.rangeProject = rangeProject;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOpenRange() {
        return openRange;
    }

    public void setOpenRange(String openRange) {
        this.openRange = openRange;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public List<WorkSpaceRecentVO> getWorkSpaceRecents() {
        return workSpaceRecents;
    }

    public void setWorkSpaceRecents(List<WorkSpaceRecentVO> workSpaceRecents) {
        this.workSpaceRecents = workSpaceRecents;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
