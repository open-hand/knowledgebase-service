package io.choerodon.kb.api.vo;

import java.util.List;

import io.choerodon.kb.infra.feign.vo.UserDO;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author zhaotianxin
 * @since 2019/12/30
 */
public class KnowledgeBaseInfoVO {

    @Encrypt
    private Long id;

    private String name;

    private String  description;

    @ApiModelProperty("公开范围类型:私有、公开到组织、")
    private String openRange;

    @ApiModelProperty("公开到某些项目")
    private List<Long> rangeProjectIds;

    private Long projectId;

    private Long organizationId;

    private Long objectVersionNumber;

    @Encrypt
    private Long templateBaseId;

    private List<UserDO> lastUpdateUsers;

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

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public List<UserDO> getLastUpdateUsers() {
        return lastUpdateUsers;
    }

    public void setLastUpdateUsers(List<UserDO> lastUpdateUsers) {
        this.lastUpdateUsers = lastUpdateUsers;
    }

    public String getOpenRange() {

        return openRange;
    }

    public void setOpenRange(String openRange) {
        this.openRange = openRange;
    }

    public List<Long> getRangeProjectIds() {
        return rangeProjectIds;
    }

    public void setRangeProjectIds(List<Long> rangeProjectIds) {
        this.rangeProjectIds = rangeProjectIds;
    }

    public Long getTemplateBaseId() {
        return templateBaseId;
    }

    public void setTemplateBaseId(Long templateBaseId) {
        this.templateBaseId = templateBaseId;
    }
}
