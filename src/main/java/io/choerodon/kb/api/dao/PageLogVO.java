package io.choerodon.kb.api.dao;

import java.util.Date;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Zenger on 2019/5/17.
 */
public class PageLogVO {

    @ApiModelProperty(value = "日志主键id")
    private Long id;

    @ApiModelProperty(value = "操作")
    private String operation;

    @ApiModelProperty(value = "领域")
    private String field;

    @ApiModelProperty(value = "日志旧值")
    private String oldValue;

    @ApiModelProperty(value = "日志旧值")
    private String oldString;

    @ApiModelProperty(value = "日志新值")
    private String newValue;

    @ApiModelProperty(value = "日志新值")
    private String newString;

    @ApiModelProperty(value = "页面id")
    private Long pageId;

    @ApiModelProperty(value = "用户名id")
    private Long userId;

    @ApiModelProperty(value = "用户名")
    private String userName;

    @ApiModelProperty(value = "登录名")
    private String loginName;

    @ApiModelProperty(value = "真实名")
    private String realName;

    @ApiModelProperty(value = "用户头像url")
    private String imageUrl;

    @ApiModelProperty(value = "用户email地址")
    private String email;

    @ApiModelProperty(value = "最后更新日期")
    private Date lastUpdateDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getOldString() {
        return oldString;
    }

    public void setOldString(String oldString) {
        this.oldString = oldString;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getNewString() {
        return newString;
    }

    public void setNewString(String newString) {
        this.newString = newString;
    }

    public Long getPageId() {
        return pageId;
    }

    public void setPageId(Long pageId) {
        this.pageId = pageId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }
}
