package io.choerodon.kb.api.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.kb.api.vo.permission.PermissionCheckVO;

import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 知识库列表VO
 * @author 25499 2020/1/2 10:29
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KnowledgeBaseListVO {
    @ApiModelProperty(value = "知识库id")
    @Encrypt
    private Long id;

    @ApiModelProperty(value = "知识库名称")
    private String name;

    @ApiModelProperty(value = "知识库描述")
    private String  description;

    @ApiModelProperty("公开范围类型:私有、公开到组织、")
    private String openRange;
    @ApiModelProperty("公开到项目记录")
    private String rangeProject;

    @ApiModelProperty("来源")
    private String source;

    @ApiModelProperty(value = "项目id")
    private Long projectId;

    @ApiModelProperty(value = "组织id")
    private Long organizationId;

    @ApiModelProperty(value = "乐观锁")
    private Long objectVersionNumber;
    @ApiModelProperty(value = "空间共享对象列表")
    private List<WorkSpaceSimpleVO> workSpaceRecents;
    
    @ApiModelProperty(value = "操作权限集合")
    private List<PermissionCheckVO> permissionCheckInfos;

    @ApiModelProperty("是否为模板的标志")
    private Boolean templateFlag;

    @ApiModelProperty("发布标志")
    private Boolean publishFlag;


    public String getRangeProject() {
        return rangeProject;
    }

    public KnowledgeBaseListVO setRangeProject(String rangeProject) {
        this.rangeProject = rangeProject;
      return this;
    }

    public Long getId() {
        return id;
    }

    public KnowledgeBaseListVO setId(Long id) {
        this.id = id;
      return this;
    }

    public String getName() {
        return name;
    }

    public KnowledgeBaseListVO setName(String name) {
        this.name = name;
      return this;
    }

    public String getDescription() {
        return description;
    }

    public KnowledgeBaseListVO setDescription(String description) {
        this.description = description;
      return this;
    }

    public String getOpenRange() {
        return openRange;
    }

    public KnowledgeBaseListVO setOpenRange(String openRange) {
        this.openRange = openRange;
      return this;
    }

    public Long getProjectId() {
        return projectId;
    }

    public KnowledgeBaseListVO setProjectId(Long projectId) {
        this.projectId = projectId;
      return this;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public KnowledgeBaseListVO setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
      return this;
    }

    public List<WorkSpaceSimpleVO> getWorkSpaceRecents() {
        return workSpaceRecents;
    }

    public KnowledgeBaseListVO setWorkSpaceRecents(List<WorkSpaceSimpleVO> workSpaceRecents) {
        this.workSpaceRecents = workSpaceRecents;
      return this;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public KnowledgeBaseListVO setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
      return this;
    }

    public String getSource() {
        return source;
    }

    public KnowledgeBaseListVO setSource(String source) {
        this.source = source;
      return this;
    }

    /**
     * @return 操作权限集合
     */
    public List<PermissionCheckVO> getPermissionCheckInfos() {
        return permissionCheckInfos;
    }

    public KnowledgeBaseListVO setPermissionCheckInfos(List<PermissionCheckVO> permissionCheckInfos) {
        this.permissionCheckInfos = permissionCheckInfos;
        return this;
    }

    public Boolean getTemplateFlag() {
        return templateFlag;
    }

    public void setTemplateFlag(Boolean templateFlag) {
        this.templateFlag = templateFlag;
    }

    public Boolean getPublishFlag() {
        return publishFlag;
    }

    public void setPublishFlag(Boolean publishFlag) {
        this.publishFlag = publishFlag;
    }

}
