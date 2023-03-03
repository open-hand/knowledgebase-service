package io.choerodon.kb.api.vo;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.choerodon.kb.infra.feign.vo.UserDO;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author zhaotianxin
 * @since 2019/12/30
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KnowledgeBaseInfoVO {

    @ApiModelProperty(value = "知识库id")
    @Encrypt
    private Long id;

    @ApiModelProperty(value = "知识库名称")
    private String name;

    @ApiModelProperty(value = "知识库描述")
    private String  description;

    @ApiModelProperty("公开范围类型:私有、公开到组织、")
    private String openRange;

    @ApiModelProperty("公开到某些项目")
    private List<Long> rangeProjectIds;

    @ApiModelProperty(value = "项目id")
    private Long projectId;

    @ApiModelProperty(value = "组织id")
    private Long organizationId;

    @ApiModelProperty(value = "乐观锁")
    private Long objectVersionNumber;

    @ApiModelProperty(value = "模板id")
    @Encrypt
    private Set<Long> templateBaseIds;

    @ApiModelProperty(value = "更新人列表")
    private List<UserDO> lastUpdateUsers;

    @ApiModelProperty("权限信息")
    private PermissionDetailVO permissionDetailVO;
    private Boolean templateFlag;
    private Boolean publishFlag;



    public Long getId() {
        return id;
    }

    public KnowledgeBaseInfoVO setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public KnowledgeBaseInfoVO setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public KnowledgeBaseInfoVO setDescription(String description) {
        this.description = description;
        return this;
    }

    public Long getProjectId() {
        return projectId;
    }

    public KnowledgeBaseInfoVO setProjectId(Long projectId) {
        this.projectId = projectId;
        return this;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public KnowledgeBaseInfoVO setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
        return this;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public KnowledgeBaseInfoVO setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
        return this;
    }

    public List<UserDO> getLastUpdateUsers() {
        return lastUpdateUsers;
    }

    public KnowledgeBaseInfoVO setLastUpdateUsers(List<UserDO> lastUpdateUsers) {
        this.lastUpdateUsers = lastUpdateUsers;
        return this;
    }

    public String getOpenRange() {

        return openRange;
    }

    public KnowledgeBaseInfoVO setOpenRange(String openRange) {
        this.openRange = openRange;
        return this;
    }

    public List<Long> getRangeProjectIds() {
        return rangeProjectIds;
    }

    public KnowledgeBaseInfoVO setRangeProjectIds(List<Long> rangeProjectIds) {
        this.rangeProjectIds = rangeProjectIds;
        return this;
    }

    public Set<Long> getTemplateBaseIds() {
        return templateBaseIds;
    }

    public KnowledgeBaseInfoVO setTemplateBaseIds(Set<Long> templateBaseIds) {
        this.templateBaseIds = templateBaseIds;
        return this;
    }

    public PermissionDetailVO getPermissionDetailVO() {
        return permissionDetailVO;
    }

    public KnowledgeBaseInfoVO setPermissionDetailVO(PermissionDetailVO permissionDetailVO) {
        this.permissionDetailVO = permissionDetailVO;
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
