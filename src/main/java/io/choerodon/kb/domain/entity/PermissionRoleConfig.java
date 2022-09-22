package io.choerodon.kb.domain.entity;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 知识库权限矩阵
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
@ApiModel("知识库权限矩阵")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "kb_pms_role_config")
public class PermissionRoleConfig extends AuditDomain {

    public static final String FIELD_ID = "id";
    public static final String FIELD_ORGANIZATION_ID = "organizationId";
    public static final String FIELD_PROJECT_ID = "projectId";
    public static final String FIELD_PERMISSION_CODE = "permissionCode";
    public static final String FIELD_PERMISSION_ROLE_CODE = "permissionRoleCode";
    public static final String FIELD_AUTHORIZE_FLAG = "authorizeFlag";

//
// 业务方法(按public protected private顺序排列)
// ------------------------------------------------------------------------------

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
    @ApiModelProperty(value = "操作权限Code", required = true)
    @NotBlank
    private String permissionCode;
    @ApiModelProperty(value = "授权角色", required = true)
    @NotBlank
    private String permissionRoleCode;
    @ApiModelProperty(value = "授权标识")
    private Boolean authorizeFlag;

//
// 非数据库字段
// ------------------------------------------------------------------------------

//
// getter/setter
// ------------------------------------------------------------------------------

    /**
     * @return 主键
     */
    public Long getId() {
        return id;
    }

    public PermissionRoleConfig setId(Long id) {
        this.id = id;
        return this;
    }

    /**
     * @return 组织ID
     */
    public Long getOrganizationId() {
        return organizationId;
    }

    public PermissionRoleConfig setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
        return this;
    }

    /**
     * @return 项目ID
     */
    public Long getProjectId() {
        return projectId;
    }

    public PermissionRoleConfig setProjectId(Long projectId) {
        this.projectId = projectId;
        return this;
    }

    /**
     * @return 操作权限Code
     */
    public String getPermissionCode() {
        return permissionCode;
    }

    public PermissionRoleConfig setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
        return this;
    }

    /**
     * @return 授权角色
     */
    public String getPermissionRoleCode() {
        return permissionRoleCode;
    }

    public PermissionRoleConfig setPermissionRoleCode(String permissionRoleCode) {
        this.permissionRoleCode = permissionRoleCode;
        return this;
    }

    /**
     * @return 授权标识
     */
    public Boolean getAuthorizeFlag() {
        return authorizeFlag;
    }

    public PermissionRoleConfig setAuthorizeFlag(Boolean authorizeFlag) {
        this.authorizeFlag = authorizeFlag;
        return this;
    }
}
