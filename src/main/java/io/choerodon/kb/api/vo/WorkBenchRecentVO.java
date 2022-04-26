package io.choerodon.kb.api.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.choerodon.kb.infra.feign.vo.UserDO;
import io.choerodon.mybatis.domain.AuditDomain;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author jiaxu.cui@hand-china.com 2020/7/13 下午3:45
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkBenchRecentVO extends AuditDomain {

    @ApiModelProperty(value = "空间id")
    @Encrypt
    private Long id;
    @ApiModelProperty(value = "页面标题")
    private String title;
    @ApiModelProperty(value = "知识库id")
    @Encrypt
    private Long baseId;
    @ApiModelProperty("页面ID")
    @Encrypt
    private Long pageId;
    @ApiModelProperty(value = "页面修改用户List")
    private List<UserDO> updatedUserList;
    @ApiModelProperty(value = "其他用户数量")
    private Integer otherUserCount = 0;
    @ApiModelProperty(value = "知识库名称")
    private String knowledgeBaseName;
    @ApiModelProperty(value = "组织id")
    private Long organizationId;
    @ApiModelProperty(value = "组织Name")
    private String organizationName;
    @ApiModelProperty(value = "项目id")
    private Long projectId;
    @ApiModelProperty(value = "项目名称")
    private String projectName;
    @ApiModelProperty(value = "是否是组织级")
    private Boolean orgFlag = false;
    @ApiModelProperty(value = "项目logo")
    private String imageUrl;

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public Long getBaseId() {
        return baseId;
    }

    public void setBaseId(Long baseId) {
        this.baseId = baseId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getOrgFlag() {
        return orgFlag;
    }

    public void setOrgFlag(Boolean orgFlag) {
        this.orgFlag = orgFlag;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getKnowledgeBaseName() {
        return knowledgeBaseName;
    }

    public void setKnowledgeBaseName(String knowledgeBaseName) {
        this.knowledgeBaseName = knowledgeBaseName;
    }

    public Integer getOtherUserCount() {
        return otherUserCount;
    }

    public void setOtherUserCount(Integer otherUserCount) {
        this.otherUserCount = otherUserCount;
    }

    public List<UserDO> getUpdatedUserList() {
        return updatedUserList;
    }

    public void setUpdatedUserList(List<UserDO> updatedUserList) {
        this.updatedUserList = updatedUserList;
    }

    public Long getPageId() {
        return pageId;
    }

    public void setPageId(Long pageId) {
        this.pageId = pageId;
    }
}
