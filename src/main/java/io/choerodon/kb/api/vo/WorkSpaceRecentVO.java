package io.choerodon.kb.api.vo;

import io.choerodon.kb.infra.feign.vo.UserDO;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import java.util.Date;
import java.util.List;

/**
 * @author shinan.chen
 * @since 2019/10/10
 */
public class WorkSpaceRecentVO {
    @ApiModelProperty(value = "空间id")
    @Encrypt
    private Long id;
    @ApiModelProperty(value = "知识库id")
    @Encrypt
    private Long baseId;
    @ApiModelProperty(value = "页面标题")
    private String title;
    @ApiModelProperty(value = "空间路由id")
    private String route;
    @ApiModelProperty(value = "更新空间名")
    private String updateworkSpace;
    @ApiModelProperty(value = "页面最后修改人id")
    private Long lastUpdatedBy;
    @ApiModelProperty(value = "页面最后修改用户对象")
    private UserDO lastUpdatedUser;
    @ApiModelProperty(value = "页面最后修改日期")
    private Date lastUpdateDate;
    @ApiModelProperty(value = "页面最后修改日期字符串")
    private String lastUpdateDateStr;
    @ApiModelProperty(value = "组织id")
    private Long organizationId;
    @ApiModelProperty(value = "项目id")
    private Long projectId;
    @ApiModelProperty(value = "知识库名称")
    private String knowledgeBaseName;

    public Long getBaseId() {
        return baseId;
    }

    public void setBaseId(Long baseId) {
        this.baseId = baseId;
    }

    public String getLastUpdateDateStr() {
        return lastUpdateDateStr;
    }

    public void setLastUpdateDateStr(String lastUpdateDateStr) {
        this.lastUpdateDateStr = lastUpdateDateStr;
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

    public Long getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(Long lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public UserDO getLastUpdatedUser() {
        return lastUpdatedUser;
    }

    public void setLastUpdatedUser(UserDO lastUpdatedUser) {
        this.lastUpdatedUser = lastUpdatedUser;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getUpdateworkSpace() {
        return updateworkSpace;
    }

    public void setUpdateworkSpace(String updateworkSpace) {
        this.updateworkSpace = updateworkSpace;
    }

    public String getKnowledgeBaseName() {
        return knowledgeBaseName;
    }

    public void setKnowledgeBaseName(String knowledgeBaseName) {
        this.knowledgeBaseName = knowledgeBaseName;
    }
}
