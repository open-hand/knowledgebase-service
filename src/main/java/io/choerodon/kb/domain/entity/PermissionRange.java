package io.choerodon.kb.domain.entity;

import java.util.Objects;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.kb.api.vo.permission.Collaborator;
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

    //
    // 业务方法(按public protected private顺序排列)
    // ------------------------------------------------------------------------------

    public PermissionRange() {
    }

    public static PermissionRange of(Long organizationId, Long projectId, String targetType, Long targetValue, String rangeType, Long rangeValue, String permissionRoleCode) {
        PermissionRange permissionRange = new PermissionRange();
        permissionRange.setOrganizationId(organizationId);
        permissionRange.setProjectId(projectId);
        permissionRange.setTargetType(targetType);
        permissionRange.setTargetValue(targetValue);
        permissionRange.setRangeType(rangeType);
        permissionRange.setRangeValue(rangeValue);
        permissionRange.setPermissionRoleCode(permissionRoleCode);
        return permissionRange;
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
    @ApiModelProperty(value = "控制对象")
    @Encrypt
    private Long targetValue;
    /**
     * {@link PermissionConstants.PermissionRangeType}
     */
    @ApiModelProperty(value = "授权对象类型", required = true)
    @NotBlank
    private String rangeType;
    @ApiModelProperty(value = "授权对象")
    private Long rangeValue;
    @ApiModelProperty(value = "授权角色", required = true)
    @NotBlank
    private String permissionRoleCode;

    //
    // 非数据库字段
    // ------------------------------------------------------------------------------

    @Transient
    @ApiModelProperty(value = "控制对象(不加密)")
    private Long noEncryptTargetValue;
    @Transient
    @ApiModelProperty(value = "协作者信息")
    private Collaborator collaborator;
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

    public Collaborator getCollaborator() {
        return collaborator;
    }

    public void setCollaborator(Collaborator collaborator) {
        this.collaborator = collaborator;
    }

    public Long getNoEncryptTargetValue() {
        return noEncryptTargetValue;
    }

    public void setNoEncryptTargetValue(Long noEncryptTargetValue) {
        this.noEncryptTargetValue = noEncryptTargetValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PermissionRange)) return false;
        PermissionRange that = (PermissionRange) o;
        return Objects.equals(getOrganizationId(), that.getOrganizationId()) &&
                Objects.equals(getProjectId(), that.getProjectId()) &&
                Objects.equals(getTargetType(), that.getTargetType()) &&
                Objects.equals(getTargetValue(), that.getTargetValue()) &&
                Objects.equals(getRangeType(), that.getRangeType()) &&
                Objects.equals(getRangeValue(), that.getRangeValue()) &&
                Objects.equals(getPermissionRoleCode(), that.getPermissionRoleCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOrganizationId(), getProjectId(), getTargetType(), getTargetValue(), getRangeType(), getRangeValue(), getPermissionRoleCode());
    }
}
