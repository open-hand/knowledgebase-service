package io.choerodon.kb.api.vo;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.kb.api.vo.permission.PermissionCheckVO;
import io.choerodon.kb.infra.feign.vo.UserDO;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 知识库对象信息
 *
 * @author shinan.chen 2019/7/17
 */
@ApiModel(value = "知识库对象信息")
@JsonInclude(JsonInclude.Include.NON_NULL)
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
    private WorkSpaceTreeNodeVO workSpace;
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

    @ApiModelProperty(value = "空间名称")
    private String name;

    /**
     * {@link io.choerodon.kb.infra.enums.WorkSpaceType}
     */
    @ApiModelProperty("工作空间的类型")
    private String type;
    @ApiModelProperty("如果是文件类型他的大小")
    private Long fileSize;

    @ApiModelProperty("子文件的数量")
    private Long subFiles;

    @ApiModelProperty("子文档的数量")
    private Long subDocuments;

    @ApiModelProperty("子文件夹的数量")
    private Long subFolders;


    @ApiModelProperty("fileKey")
    private String fileKey;


    // 前端onlyoffice展示时需要用到的字段
    /**
     * “fileType”：“docx”，
     * “key”：“Khirz6zTPdfd7”，
     * title”：“示例文档 Title.docx”，
     * “url”：“https://example.com/url -to-example-document.docx"
     */
    @ApiModelProperty("文件的类型（根据后缀来判断）")
    private String fileType;
    @ApiModelProperty("这个就是uuid的那个fileId")
    private String key;
    @ApiModelProperty("文件名")
    private String title;
    @ApiModelProperty("文件的下载地址")
    private String url;
    @ApiModelProperty("文件的大小")
    private Long size;
    @ApiModelProperty("权限信息")
    private List<PermissionCheckVO> permissionCheckInfos;

    /**
     * 模板用到的字段
     */

    private Boolean templateFlag;
    private String templateCategory;


    public Boolean getDelete() {
        return delete;
    }

    public WorkSpaceInfoVO setDelete(Boolean delete) {
        this.delete = delete;
        return this;
    }

    public List<PageCommentVO> getPageComments() {
        return pageComments;
    }

    public WorkSpaceInfoVO setPageComments(List<PageCommentVO> pageComments) {
        this.pageComments = pageComments;
        return this;
    }

    public List<PageAttachmentVO> getPageAttachments() {
        return pageAttachments;
    }

    public WorkSpaceInfoVO setPageAttachments(List<PageAttachmentVO> pageAttachments) {
        this.pageAttachments = pageAttachments;
        return this;
    }

    public Boolean getIsOperate() {
        return isOperate;
    }

    public WorkSpaceInfoVO setIsOperate(Boolean operate) {
        this.isOperate = operate;
        return this;
    }

    public Long getId() {
        return id;
    }

    public WorkSpaceInfoVO setId(Long id) {
        this.id = id;
        return this;
    }

    public String getRoute() {
        return route;
    }

    public WorkSpaceInfoVO setRoute(String route) {
        this.route = route;
        return this;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public WorkSpaceInfoVO setReferenceType(String referenceType) {
        this.referenceType = referenceType;
        return this;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public WorkSpaceInfoVO setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
        return this;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public WorkSpaceInfoVO setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public UserDO getCreateUser() {
        return createUser;
    }

    public WorkSpaceInfoVO setCreateUser(UserDO createUser) {
        this.createUser = createUser;
        return this;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public WorkSpaceInfoVO setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public Long getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public WorkSpaceInfoVO setLastUpdatedBy(Long lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
        return this;
    }

    public UserDO getLastUpdatedUser() {
        return lastUpdatedUser;
    }

    public WorkSpaceInfoVO setLastUpdatedUser(UserDO lastUpdatedUser) {
        this.lastUpdatedUser = lastUpdatedUser;
        return this;
    }

    public Boolean getOperate() {
        return isOperate;
    }

    public WorkSpaceInfoVO setOperate(Boolean operate) {
        this.isOperate = operate;
        return this;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public WorkSpaceInfoVO setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
        return this;
    }

    public Boolean getHasDraft() {
        return hasDraft;
    }

    public WorkSpaceInfoVO setHasDraft(Boolean hasDraft) {
        this.hasDraft = hasDraft;
        return this;
    }

    public Date getCreateDraftDate() {
        return createDraftDate;
    }

    public WorkSpaceInfoVO setCreateDraftDate(Date createDraftDate) {
        this.createDraftDate = createDraftDate;
        return this;
    }

    public WorkSpaceTreeNodeVO getWorkSpace() {
        return workSpace;
    }

    public WorkSpaceInfoVO setWorkSpace(WorkSpaceTreeNodeVO workSpace) {
        this.workSpace = workSpace;
        return this;
    }

    public PageInfoVO getPageInfo() {
        return pageInfo;
    }

    public WorkSpaceInfoVO setPageInfo(PageInfoVO pageInfo) {
        this.pageInfo = pageInfo;
        return this;
    }

    public UserSettingVO getUserSettingVO() {
        return userSettingVO;
    }

    public WorkSpaceInfoVO setUserSettingVO(UserSettingVO userSettingVO) {
        this.userSettingVO = userSettingVO;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public WorkSpaceInfoVO setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getFileType() {
        return fileType;
    }

    public WorkSpaceInfoVO setFileType(String fileType) {
        this.fileType = fileType;
        return this;
    }

    public String getKey() {
        return key;
    }

    public WorkSpaceInfoVO setKey(String key) {
        this.key = key;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public WorkSpaceInfoVO setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public WorkSpaceInfoVO setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getType() {
        return type;
    }

    public WorkSpaceInfoVO setType(String type) {
        this.type = type;
        return this;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public WorkSpaceInfoVO setFileSize(Long fileSize) {
        this.fileSize = fileSize;
        return this;
    }

    public Long getSubFiles() {
        return subFiles;
    }

    public WorkSpaceInfoVO setSubFiles(Long subFiles) {
        this.subFiles = subFiles;
        return this;
    }

    public String getFileKey() {
        return fileKey;
    }

    public WorkSpaceInfoVO setFileKey(String fileKey) {
        this.fileKey = fileKey;
        return this;
    }

    public String getName() {
        return name;
    }

    public WorkSpaceInfoVO setName(String name) {
        this.name = name;
        return this;
    }

    public Long getSubDocuments() {
        return subDocuments;
    }

    public WorkSpaceInfoVO setSubDocuments(Long subDocuments) {
        this.subDocuments = subDocuments;
        return this;
    }

    public Long getSubFolders() {
        return subFolders;
    }

    public WorkSpaceInfoVO setSubFolders(Long subFolders) {
        this.subFolders = subFolders;
        return this;
    }

    /**
     * @return 权限信息
     */
    public List<PermissionCheckVO> getPermissionCheckInfos() {
        return permissionCheckInfos;
    }

    public WorkSpaceInfoVO setPermissionCheckInfos(List<PermissionCheckVO> permissionCheckInfos) {
        this.permissionCheckInfos = permissionCheckInfos;
        return this;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Boolean getTemplateFlag() {
        return templateFlag;
    }

    public void setTemplateFlag(Boolean templateFlag) {
        this.templateFlag = templateFlag;
    }

    public String getTemplateCategory() {
        return templateCategory;
    }

    public void setTemplateCategory(String templateCategory) {
        this.templateCategory = templateCategory;
    }
}
