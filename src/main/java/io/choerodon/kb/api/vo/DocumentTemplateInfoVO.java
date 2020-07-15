package io.choerodon.kb.api.vo;

import java.util.Date;

import io.choerodon.kb.infra.feign.vo.UserDO;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author zhaotianxin
 * @since 2020/1/2
 */
public class DocumentTemplateInfoVO {
    @Encrypt
    private Long id;

    private String title;

    private String description;

    private Long createdBy;

    private Long lastUpdatedBy;

    private UserDO createdUser;

    private UserDO lastUpdatedUser;

    @ApiModelProperty(value = "模板类型")
    private String templateType;

    @ApiModelProperty(value = "空间创建日期")
    private Date creationDate;

    @ApiModelProperty(value = "最后修改日期")
    private Date lastUpdateDate;

    private Long objectVersionNumber;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UserDO getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(UserDO createdUser) {
        this.createdUser = createdUser;
    }

    public UserDO getLastUpdatedUser() {
        return lastUpdatedUser;
    }

    public void setLastUpdatedUser(UserDO lastUpdatedUser) {
        this.lastUpdatedUser = lastUpdatedUser;
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(Long lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public DocumentTemplateInfoVO() {
    }

    public DocumentTemplateInfoVO(Long id, String title, String description, Long createdBy, Long lastUpdatedBy, UserDO createdUser, UserDO lastUpdatedUser, String templateType, Date creationDate, Date lastUpdateDate, Long objectVersionNumber) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.createdBy = createdBy;
        this.lastUpdatedBy = lastUpdatedBy;
        this.createdUser = createdUser;
        this.lastUpdatedUser = lastUpdatedUser;
        this.templateType = templateType;
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
        this.objectVersionNumber = objectVersionNumber;
    }

    public DocumentTemplateInfoVO(Long id, String title, String description, Long createdBy, Long lastUpdatedBy, String templateType, Date creationDate, Date lastUpdateDate, Long objectVersionNumber) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.createdBy = createdBy;
        this.lastUpdatedBy = lastUpdatedBy;
        this.templateType = templateType;
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
        this.objectVersionNumber = objectVersionNumber;
    }
}
