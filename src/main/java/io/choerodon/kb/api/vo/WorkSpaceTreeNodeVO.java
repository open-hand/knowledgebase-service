package io.choerodon.kb.api.vo;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.collections4.CollectionUtils;

import io.choerodon.kb.api.vo.permission.PermissionCheckVO;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.feign.vo.UserDO;
import io.choerodon.kb.infra.utils.CommonUtil;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 知识库对象树节点VO
 * @author Zenger on 2019/5/6.
 */
@ApiModel(value = "知识库对象树节点VO")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkSpaceTreeNodeVO {

    /**
     * 快速创建
     * @param workSpace 知识库对象
     * @param childIds  子级ID
     * @return          知识库对象树节点VO
     */
    public static WorkSpaceTreeNodeVO of(WorkSpaceDTO workSpace, Collection<Long> childIds) {
        WorkSpaceTreeNodeVO treeNode = new WorkSpaceTreeNodeVO();
        treeNode.setCreatedBy(workSpace.getCreatedBy());
        if (CollectionUtils.isEmpty(childIds)) {
            treeNode.setHasChildren(false);
            treeNode.setChildren(Collections.emptyList());
        } else {
            treeNode.setHasChildren(true);
            treeNode.setChildren(new ArrayList<>(childIds));
        }
        WorkSpaceTreeNodeInfo data = new WorkSpaceTreeNodeInfo();
        data.setTitle(workSpace.getName());
        treeNode.setData(data);
        treeNode.setIsExpanded(false);
        treeNode.setIsClick(false);
        treeNode.setBaseId(workSpace.getBaseId());
        treeNode.setParentId(workSpace.getParentId());
        treeNode.setId(workSpace.getId());
        treeNode.setRoute(workSpace.getRoute());
        treeNode.setType(workSpace.getType());
        treeNode.setFileKey(workSpace.getFileKey());
        treeNode.setFileType(CommonUtil.getFileType(workSpace.getFileKey()));
        treeNode.setCreationDate(workSpace.getCreationDate());
        treeNode.setLastUpdateDate(workSpace.getLastUpdateDate());
        treeNode.setType(workSpace.getType());
        return treeNode;
    }

    /**
     * 知识库对象树节点VO信息
     * @author Zenger on 2019/5/6.
     */
    @ApiModel(value = "知识库对象树节点VO信息")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WorkSpaceTreeNodeInfo {

        @ApiModelProperty(value = "名称")
        private String title;

        /**
         * @return 名称
         */
        public String getTitle() {
            return title;
        }

        public WorkSpaceTreeNodeInfo setTitle(String title) {
            this.title = title;
            return this;
        }
    }

    public WorkSpaceTreeNodeVO() {
        this.isExpanded = false;
    }

    @ApiModelProperty(value = "工作空间ID")
    @Encrypt(ignoreValue = "0")
    private Long id;
    @ApiModelProperty(value = "工作空间父级ID")
    @Encrypt(ignoreValue = "0")
    private Long parentId;
    @Encrypt
    @ApiModelProperty(value = "知识库ID")
    private Long baseId;
    @ApiModelProperty(value = "是否展开")
    private Boolean isExpanded;
    @ApiModelProperty(value = "是否有子空间目录")
    private Boolean hasChildren;
    @ApiModelProperty(value = "工作空间信息")
    private WorkSpaceTreeNodeInfo data;
    @ApiModelProperty(value = "工作空间子目录ID")
    @Encrypt
    private List<Long> children;
    @ApiModelProperty(value = "创建用户id")
    @Encrypt
    private Long createdBy;
    @ApiModelProperty(value = "路由")
    private String route;
    @ApiModelProperty(value = "是否点击")
    private Boolean isClick;
    @ApiModelProperty(value = "类型")
    private String type;
    @ApiModelProperty(value = "fileKey")
    private String fileKey;

    // 前端onlyoffice展示时需要用到的字段
    /**
     * “fileType”：“docx”，
     * “key”：“Khirz6zTPdfd7”，
     * title”：“示例文档 Title.docx”，
     * “url”：“https://example.com/url -to-example-document.docx"
     */
    @ApiModelProperty(value = "文件类型")
    private String fileType;
    @ApiModelProperty("对应的是fileId")
    private String key;
    @ApiModelProperty(value = "标题")
    private String title;
    @ApiModelProperty(value = "链接")
    private String url;

    @ApiModelProperty(value = "创建时间")
    private Date creationDate;
    @ApiModelProperty(value = "更新时间")
    private Date lastUpdateDate;
    @ApiModelProperty(value = "更新人")
    private UserDO lastUpdatedUser;
    @ApiModelProperty(value = "创建人")
    private UserDO createdUser;
    @ApiModelProperty("前端需要默认这个初始化的值为false")
    private Boolean isEdit = false;
    @ApiModelProperty("操作权限集合")
    List<PermissionCheckVO> permissionCheckInfos;

    public Boolean getClick() {
        return isClick;
    }

    public WorkSpaceTreeNodeVO setClick(Boolean click) {
        this.isClick = click;
        return this;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public WorkSpaceTreeNodeVO setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public WorkSpaceTreeNodeVO setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
        return this;
    }

    public UserDO getLastUpdatedUser() {
        return lastUpdatedUser;
    }

    public WorkSpaceTreeNodeVO setLastUpdatedUser(UserDO lastUpdatedUser) {
        this.lastUpdatedUser = lastUpdatedUser;
        return this;
    }

    public UserDO getCreatedUser() {
        return createdUser;
    }

    public WorkSpaceTreeNodeVO setCreatedUser(UserDO createdUser) {
        this.createdUser = createdUser;
        return this;
    }

    public Boolean getIsClick() {
        return isClick;
    }

    public WorkSpaceTreeNodeVO setIsClick(Boolean click) {
        this.isClick = click;
        return this;
    }

    public String getRoute() {
        return route;
    }

    public WorkSpaceTreeNodeVO setRoute(String route) {
        this.route = route;
        return this;
    }

    public Boolean getExpanded() {
        return isExpanded;
    }

    public WorkSpaceTreeNodeVO setExpanded(Boolean expanded) {
        this.isExpanded = expanded;
        return this;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public WorkSpaceTreeNodeVO setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public Long getId() {
        return id;
    }

    public WorkSpaceTreeNodeVO setId(Long id) {
        this.id = id;
        return this;
    }

    public Boolean getHasChildren() {
        return hasChildren;
    }

    public WorkSpaceTreeNodeVO setHasChildren(Boolean hasChildren) {
        this.hasChildren = hasChildren;
        return this;
    }

    public WorkSpaceTreeNodeInfo getData() {
        return data;
    }

    public WorkSpaceTreeNodeVO setData(WorkSpaceTreeNodeInfo data) {
        this.data = data;
        return this;
    }

    public List<Long> getChildren() {
        return children;
    }

    public WorkSpaceTreeNodeVO setChildren(List<Long> children) {
        this.children = children;
        return this;
    }

    public Long getParentId() {
        return parentId;
    }

    public WorkSpaceTreeNodeVO setParentId(Long parentId) {
        this.parentId = parentId;
        return this;
    }

    public Boolean getIsExpanded() {
        return isExpanded;
    }

    public WorkSpaceTreeNodeVO setIsExpanded(Boolean expanded) {
        this.isExpanded = expanded;
        return this;
    }

    public WorkSpaceTreeNodeVO setBaseId(Long baseId) {
        this.baseId = baseId;
        return this;
    }

    public Long getBaseId() {
        return baseId;
    }

    public String getType() {
        return type;
    }

    public WorkSpaceTreeNodeVO setType(String type) {
        this.type = type;
        return this;
    }

    public String getFileKey() {
        return fileKey;
    }

    public WorkSpaceTreeNodeVO setFileKey(String fileKey) {
        this.fileKey = fileKey;
        return this;
    }

    public String getFileType() {
        return fileType;
    }

    public WorkSpaceTreeNodeVO setFileType(String fileType) {
        this.fileType = fileType;
        return this;
    }

    public String getKey() {
        return key;
    }

    public WorkSpaceTreeNodeVO setKey(String key) {
        this.key = key;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public WorkSpaceTreeNodeVO setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public WorkSpaceTreeNodeVO setUrl(String url) {
        this.url = url;
        return this;
    }

    public Boolean getEdit() {
        return isEdit;
    }

    public WorkSpaceTreeNodeVO setEdit(Boolean edit) {
        this.isEdit = edit;
        return this;
    }

    /**
     * @return 操作权限集合
     */
    public List<PermissionCheckVO> getPermissionCheckInfos() {
        return permissionCheckInfos;
    }

    public WorkSpaceTreeNodeVO setPermissionCheckInfos(List<PermissionCheckVO> permissionCheckInfos) {
        this.permissionCheckInfos = permissionCheckInfos;
        return this;
    }
}
