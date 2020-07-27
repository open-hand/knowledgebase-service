package io.choerodon.kb.api.vo;

import io.choerodon.kb.infra.feign.vo.UserDO;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import java.util.Date;
import java.util.List;

/**
 * @author shinan.chen
 * @since 2019/7/17
 */
public class WorkSpaceInfoVO {
    @ApiModelProperty(value = "空间id")
    @Encrypt
    private Long id;
    @ApiModelProperty(value = "描述")
    private String description;
    @ApiModelProperty(value = "工作空间路径")
    private String route;
    @ApiModelProperty(value = "引用类型")
    private String referenceType;
    @ApiModelProperty(value = "乐观锁版本号")
    private Long objectVersionNumber;
    @ApiModelProperty(value = "空间创建人id")
    @Encrypt
    private Long createdBy;
    @ApiModelProperty(value = "空间创建用户对象")
    private UserDO createUser;
    @ApiModelProperty(value = "空间创建日期")
    private Date creationDate;
    @ApiModelProperty(value = "空间最后修改人id")
    @Encrypt
    private Long lastUpdatedBy;
    @ApiModelProperty(value = "空间最后修改用户对象")
    private UserDO lastUpdatedUser;
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
    @ApiModelProperty(value = "附件列表")
    private List<PageAttachmentVO> pageAttachments;
    @ApiModelProperty(value = "评论列表")
    private List<PageCommentVO> pageComments;
    @ApiModelProperty(value = "是否已经被删除")
    private Boolean delete;

    public Boolean getDelete() {
        return delete;
    }

    public void setDelete(Boolean delete) {
        this.delete = delete;
    }

    public List<PageCommentVO> getPageComments() {
        return pageComments;
    }

    public void setPageComments(List<PageCommentVO> pageComments) {
        this.pageComments = pageComments;
    }

    public List<PageAttachmentVO> getPageAttachments() {
        return pageAttachments;
    }

    public void setPageAttachments(List<PageAttachmentVO> pageAttachments) {
        this.pageAttachments = pageAttachments;
    }

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

    public UserDO getCreateUser() {
        return createUser;
    }

    public void setCreateUser(UserDO createUser) {
        this.createUser = createUser;
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

    public UserDO getLastUpdatedUser() {
        return lastUpdatedUser;
    }

    public void setLastUpdatedUser(UserDO lastUpdatedUser) {
        this.lastUpdatedUser = lastUpdatedUser;
    }

    public Boolean getOperate() {
        return isOperate;
    }

    public void setOperate(Boolean operate) {
        isOperate = operate;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
