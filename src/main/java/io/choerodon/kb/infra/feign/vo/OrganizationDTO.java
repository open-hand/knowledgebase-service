package io.choerodon.kb.infra.feign.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author superlee
 * @since 2019-04-22
 */
public class OrganizationDTO {
    @ApiModelProperty(value = "主键/非必填")
    private Long id;
    @ApiModelProperty(value = "组织名/必填")
    private String name;
    @ApiModelProperty(value = "组织编码/必填")
    private String code;
    @ApiModelProperty("创建者Id/非必填/默认为登陆用户id")
    private Long userId;
    @ApiModelProperty("组织地址/非必填")
    private String address;
    @ApiModelProperty(value = "组织类别")
    private String category;
    @ApiModelProperty(value = "组织图标url")
    private String imageUrl;
    @ApiModelProperty(value = "是否启用/非必填/默认：true")
    private Boolean enabled;
    @ApiModelProperty(value = "组织官网地址")
    private String homePage;
    @ApiModelProperty(value = "组织规模")
    private Integer scale;
    @ApiModelProperty(value = "组织所在行业")
    private String businessType;
    @ApiModelProperty(value = "邮箱后缀，唯一。注册时必输，数据库非必输")
    private String emailSuffix;
    @ApiModelProperty(value = "是否是注册组织")
    private Boolean isRegister;
    @ApiModelProperty(value = "项目数量")
    private Integer projectCount;
    @ApiModelProperty(value = "是否有权限进入")
    private Boolean isInto = true;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getHomePage() {
        return homePage;
    }

    public void setHomePage(String homePage) {
        this.homePage = homePage;
    }

    public Integer getScale() {
        return scale;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getEmailSuffix() {
        return emailSuffix;
    }

    public void setEmailSuffix(String emailSuffix) {
        this.emailSuffix = emailSuffix;
    }

    public Boolean getRegister() {
        return isRegister;
    }

    public void setRegister(Boolean register) {
        isRegister = register;
    }

    public Integer getProjectCount() {
        return projectCount;
    }

    public void setProjectCount(Integer projectCount) {
        this.projectCount = projectCount;
    }

    public Boolean getInto() {
        return isInto;
    }

    public void setInto(Boolean into) {
        isInto = into;
    }
}
