package io.choerodon.kb.api.vo;

import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * @author shinan.chen
 * @since 2019/7/17
 */
public class WorkSpaceInfoVO {
    @ApiModelProperty(value = "空间id")
    private Long id;
    @ApiModelProperty(value = "工作空间路径")
    private String route;
    @ApiModelProperty(value = "引用类型")
    private String referenceType;
    @ApiModelProperty(value = "乐观锁版本号")
    private Long objectVersionNumber;
    @ApiModelProperty(value = "空间创建人id")
    private Long createdBy;
    @ApiModelProperty(value = "空间创建人")
    private String createName;
    @ApiModelProperty(value = "空间创建日期")
    private Date creationDate;
    @ApiModelProperty(value = "空间最后修改人id")
    private Long lastUpdatedBy;
    @ApiModelProperty(value = "空间最后修改人")
    private String lastUpdatedName;
    @ApiModelProperty(value = "空间最后修改日期")
    private Date lastUpdateDate;
    @ApiModelProperty(value = "当前页面当前用户是否有草稿")
    private Boolean hasDraft;
    @ApiModelProperty(value = "创建草稿时间")
    private Date createDraftDate;
    @ApiModelProperty(value = "工作空间目录结构")
    private WorkSpaceTreeVO workSpace;
    @ApiModelProperty(value = "页面信息")
    private PageInfoVO pageInfo;
    @ApiModelProperty(value = "用户个人设置信息")
    private UserSettingVO userSettingVO;
    @ApiModelProperty(value = "是否有操作的权限（用于项目层只能查看组织层文档，不能操作）")
    private Boolean isOperate;

    public Boolean getIsOperate() {
        return isOperate;
    }

    public void setIsOperate(Boolean operate) {
        isOperate = operate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
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

    public String getCreateName() {
        return createName;
    }

    public void setCreateName(String createName) {
        this.createName = createName;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Long getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(Long lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public String getLastUpdatedName() {
        return lastUpdatedName;
    }

    public void setLastUpdatedName(String lastUpdatedName) {
        this.lastUpdatedName = lastUpdatedName;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Boolean getHasDraft() {
        return hasDraft;
    }

    public void setHasDraft(Boolean hasDraft) {
        this.hasDraft = hasDraft;
    }

    public Date getCreateDraftDate() {
        return createDraftDate;
    }

    public void setCreateDraftDate(Date createDraftDate) {
        this.createDraftDate = createDraftDate;
    }

    public WorkSpaceTreeVO getWorkSpace() {
        return workSpace;
    }

    public void setWorkSpace(WorkSpaceTreeVO workSpace) {
        this.workSpace = workSpace;
    }

    public PageInfoVO getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(PageInfoVO pageInfo) {
        this.pageInfo = pageInfo;
    }

    public UserSettingVO getUserSettingVO() {
        return userSettingVO;
    }

    public void setUserSettingVO(UserSettingVO userSettingVO) {
        this.userSettingVO = userSettingVO;
    }
}
