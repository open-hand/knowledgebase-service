package io.choerodon.kb.api.dao;

import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * @author shinan.chen
 * @since 2019/5/16
 */
public class PageVersionDTO {
    @ApiModelProperty(value = "版本id")
    private Long id;
    @ApiModelProperty(value = "版本名称")
    private String name;
    @ApiModelProperty(value = "页面id")
    private Long pageId;
    @ApiModelProperty(value = "创建日期")
    private Date creationDate;
    @ApiModelProperty(value = "创建用户id")
    private Long createdBy;
    @ApiModelProperty(value = "创建用户真实名称")
    private String createUserRealName;
    @ApiModelProperty(value = "创建用户工号+姓名")
    private String createUserName;
    @ApiModelProperty(value = "创建用户头像图片地址")
    private String createUserImageUrl;

    public String getCreateUserRealName() {
        return createUserRealName;
    }

    public void setCreateUserRealName(String createUserRealName) {
        this.createUserRealName = createUserRealName;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreateUserName() {
        return createUserName;
    }

    public void setCreateUserName(String createUserName) {
        this.createUserName = createUserName;
    }

    public String getCreateUserImageUrl() {
        return createUserImageUrl;
    }

    public void setCreateUserImageUrl(String createUserImageUrl) {
        this.createUserImageUrl = createUserImageUrl;
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

    public Long getPageId() {
        return pageId;
    }

    public void setPageId(Long pageId) {
        this.pageId = pageId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
