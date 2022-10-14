package io.choerodon.kb.domain.entity;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import io.choerodon.kb.api.vo.permission.CollaboratorVO;
import io.choerodon.kb.infra.enums.PermissionConstants;
import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 知识库权限应用范围
 * <br/>
 * <table>
 *     <tr>
 *         <td>target_type</td>
 *         <td>控制对象类型</td>
 *         <td>组织层创建知识库</td>
 *         <td>项目层创建知识库</td>
 *         <td>组织层知识库默认</td>
 *         <td>项目层知识库默认</td>
 *         <td>组织层知识库</td>
 *         <td>组织层文件夹</td>
 *         <td>组织层文件</td>
 *         <td>项目层知识库</td>
 *         <td>项目层文件夹</td>
 *         <td>项目层文件</td>
 *     </tr>
 *     <tr>
 *         <td>target_value</td>
 *         <td>控制对象</td>
 *         <td>organization_id</td>
 *         <td>project_id</td>
 *         <td>organization_id</td>
 *         <td>project_id</td>
 *         <td>workspace_id</td>
 *         <td>folder_id</td>
 *         <td>file_id</td>
 *         <td>workspace_id</td>
 *         <td>folder_id</td>
 *         <td>file_id</td>
 *     </tr>
 *     <tr>
 *         <td>range_type</td>
 *         <td>授权对象类型</td>
 *         <td>管理员/成员</td>
 *         <td>管理员/成员</td>
 *         <td>用户/角色/工作组/公开</td>
 *         <td>用户/角色/工作组/公开</td>
 *         <td>用户/角色/工作组/公开</td>
 *         <td>用户/角色/工作组/公开</td>
 *         <td>用户/角色/工作组/公开</td>
 *         <td>用户/角色/工作组/公开</td>
 *         <td>用户/角色/工作组/公开</td>
 *         <td>用户/角色/工作组/公开</td>
 *     </tr>
 *     <tr>
 *         <td>range_value</td>
 *         <td>授权对象</td>
 *         <td>0</td>
 *         <td>0</td>
 *         <td>user_id/role_id/group_id/0</td>
 *         <td>user_id/role_id/group_id/0</td>
 *         <td>user_id/role_id/group_id/0</td>
 *         <td>user_id/role_id/group_id/0</td>
 *         <td>user_id/role_id/group_id/0</td>
 *         <td>user_id/role_id/group_id/0</td>
 *         <td>user_id/role_id/group_id/0</td>
 *         <td>user_id/role_id/group_id/0</td>
 *     </tr>
 *     <tr>
 *         <td>permission_role</td>
 *         <td>授权角色</td>
 *         <td>'NULL'</td>
 *         <td>'NULL'</td>
 *         <td>MANAGER/EDITOR/READER</td>
 *         <td>MANAGER/EDITOR/READER</td>
 *         <td>MANAGER/EDITOR/READER</td>
 *         <td>MANAGER/EDITOR/READER</td>
 *         <td>MANAGER/EDITOR/READER</td>
 *         <td>MANAGER/EDITOR/READER</td>
 *         <td>MANAGER/EDITOR/READER</td>
 *         <td>MANAGER/EDITOR/READER</td>
 *     </tr>
 * </table>
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 * @author zongqi.hao@zknow.com
 */
@ApiModel("知识库权限应用范围")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "kb_permission_range")
public class PermissionRange extends AuditDomain {

    public static final String FIELD_ID = "id";
    public static final String FIELD_ORGANIZATION_ID = "organizationId";
    public static final String FIELD_PROJECT_ID = "projectId";
    public static final String FIELD_TARGET_TYPE = "targetType";
    public static final String FIELD_TARGET_VALUE = "targetValue";
    public static final String FIELD_RANGE_TYPE = "rangeType";
    public static final String FIELD_RANGE_VALUE = "rangeValue";
    public static final String FIELD_PERMISSION_ROLE_CODE = "permissionRoleCode";
    public static final String FIELD_OWNER_FLAG = "ownerFlag";

    //
    // 业务方法(按public protected private顺序排列)
    // ------------------------------------------------------------------------------

    public PermissionRange() {
    }

    /**
     * 快速创建, 所有者标识默认复制FALSE
     *
     * @param organizationId     组织ID
     * @param projectId          项目ID
     * @param targetType         控制对象类型
     * @param targetValue        控制对象
     * @param rangeType          授权对象类型
     * @param rangeValue         授权对象
     * @param permissionRoleCode 授权角色
     * @return Entity
     */
    public static PermissionRange of(
            Long organizationId,
            Long projectId,
            String targetType,
            Long targetValue,
            String rangeType,
            Long rangeValue,
            String permissionRoleCode
    ) {
        return of(
                organizationId,
                projectId,
                targetType,
                targetValue,
                rangeType,
                rangeValue,
                permissionRoleCode,
                null
        );
    }

    /**
     * 快速创建
     *
     * @param organizationId     组织ID
     * @param projectId          项目ID
     * @param targetType         控制对象类型
     * @param targetValue        控制对象
     * @param rangeType          授权对象类型
     * @param rangeValue         授权对象
     * @param permissionRoleCode 授权角色
     * @param ownerFlag          所有者标识
     * @return Entity
     */
    public static PermissionRange of(
            Long organizationId,
            Long projectId,
            String targetType,
            Long targetValue,
            String rangeType,
            Long rangeValue,
            String permissionRoleCode,
            Boolean ownerFlag
    ) {
        PermissionRange permissionRange = new PermissionRange();
        permissionRange.organizationId = organizationId;
        permissionRange.projectId = projectId;
        permissionRange.setTargetType(targetType);
        permissionRange.targetValue = targetValue;
        permissionRange.rangeType = rangeType;
        permissionRange.rangeValue = rangeValue;
        permissionRange.permissionRoleCode = permissionRoleCode;
        permissionRange.ownerFlag = ownerFlag;
        return permissionRange;
    }

    /**
     * 设置空值的为非owner
     */
    public void nonOwner() {
        if (this.ownerFlag == null) {
            this.ownerFlag = Boolean.FALSE;
        }
    }
    //
    // 数据库字段
    // ------------------------------------------------------------------------------


    @ApiModelProperty("主键")
    @Id
    @GeneratedValue
    @Encrypt
    private Long id;
    @ApiModelProperty(value = "组织ID", required = true)
    @NotNull
    private Long organizationId;
    @ApiModelProperty(value = "项目ID", required = true)
    @NotNull
    private Long projectId;
    /**
     * {@link PermissionConstants.PermissionRangeType}
     */
    @ApiModelProperty(value = "控制对象类型", required = true)
    @NotBlank
    private String targetType;
    @ApiModelProperty(value = "控制对象ID")
    @Encrypt(ignoreValue = {"0"})
    private Long targetValue;
    /**
     * {@link PermissionConstants.PermissionRangeType}
     */
    @ApiModelProperty(value = "授权对象类型", required = true)
    @NotBlank
    private String rangeType;
    @ApiModelProperty(value = "授权对象")
    @Encrypt(ignoreValue = {"0"})
    private Long rangeValue;
    @ApiModelProperty(value = "授权角色", required = true)
    @NotBlank
    private String permissionRoleCode;
    @ApiModelProperty(value = "所有者标识")
    private Boolean ownerFlag;

    //
    // 非数据库字段
    // ------------------------------------------------------------------------------

    @Transient
    @ApiModelProperty(value = "控制对象基础类型")
    private String targetBaseType;

    @Transient
    @ApiModelProperty(value = "控制对象名称")
    private String targetName;

    @Transient
    @ApiModelProperty(value = "协作者信息")
    private CollaboratorVO collaborator;

    @Transient
    @ApiModelProperty(value = "是否为继承权限")
    private Boolean inheritFlag;

    //
    // getter/setter
    // ------------------------------------------------------------------------------

    /**
     * @return 主键
     */
    public Long getId() {
        return id;
    }

    public PermissionRange setId(Long id) {
        this.id = id;
        return this;
    }

    /**
     * @return 组织ID
     */
    public Long getOrganizationId() {
        return organizationId;
    }

    public PermissionRange setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
        return this;
    }

    /**
     * @return 项目ID
     */
    public Long getProjectId() {
        return projectId;
    }

    public PermissionRange setProjectId(Long projectId) {
        this.projectId = projectId;
        return this;
    }

    /**
     * @return 控制对象类型
     */
    public String getTargetType() {
        return targetType;
    }

    public PermissionRange setTargetType(String targetType) {
        this.targetType = targetType;
        if (StringUtils.isNotBlank(targetType)) {
            this.targetBaseType = PermissionConstants.PermissionTargetType.of(targetType).getBaseType().toString();
        }
        return this;
    }

    /**
     * @return 控制对象
     */
    public Long getTargetValue() {
        return targetValue;
    }

    public PermissionRange setTargetValue(Long targetValue) {
        this.targetValue = targetValue;
        return this;
    }

    /**
     * @return 授权对象类型
     */
    public String getRangeType() {
        return rangeType;
    }

    public PermissionRange setRangeType(String rangeType) {
        this.rangeType = rangeType;
        return this;
    }

    /**
     * @return 授权对象
     */
    public Long getRangeValue() {
        return rangeValue;
    }

    public PermissionRange setRangeValue(Long rangeValue) {
        this.rangeValue = rangeValue;
        return this;
    }

    /**
     * @return 授权角色
     */
    public String getPermissionRoleCode() {
        return permissionRoleCode;
    }

    public PermissionRange setPermissionRoleCode(String permissionRoleCode) {
        this.permissionRoleCode = permissionRoleCode;
        return this;
    }

    /**
     * @return 所有者标识
     */
    public Boolean getOwnerFlag() {
        return ownerFlag;
    }

    public PermissionRange setOwnerFlag(Boolean ownerFlag) {
        this.ownerFlag = ownerFlag;
        return this;
    }

    /**
     * @return 控制对象基础类型
     */
    public String getTargetBaseType() {
        return targetBaseType;
    }

    public PermissionRange setTargetBaseType(String targetBaseType) {
        this.targetBaseType = targetBaseType;
        return this;
    }

    /**
     * @return 控制对象名称
     */
    public String getTargetName() {
        return targetName;
    }

    public PermissionRange setTargetName(String targetName) {
        this.targetName = targetName;
        return this;
    }

    /**
     * @return 协作者信息
     */
    public CollaboratorVO getCollaborator() {
        return collaborator;
    }

    public PermissionRange setCollaborator(CollaboratorVO collaborator) {
        this.collaborator = collaborator;
        return this;
    }

    /**
     * @return 是否为继承权限
     */
    public Boolean getInheritFlag() {
        return inheritFlag;
    }

    public PermissionRange setInheritFlag(Boolean inheritFlag) {
        this.inheritFlag = inheritFlag;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionRange that = (PermissionRange) o;
        return new EqualsBuilder()
                .append(organizationId, that.organizationId)
                .append(projectId, that.projectId)
                .append(targetType, that.targetType)
                .append(targetValue, that.targetValue)
                .append(rangeType, that.rangeType)
                .append(rangeValue, that.rangeValue)
                .append(permissionRoleCode, that.permissionRoleCode)
                .append(ownerFlag, that.ownerFlag)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(projectId)
                .append(targetType)
                .append(targetValue)
                .append(rangeType)
                .append(rangeValue)
                .append(permissionRoleCode)
                .append(ownerFlag)
                .toHashCode();
    }
}
