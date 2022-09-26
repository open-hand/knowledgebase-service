package io.choerodon.kb.domain.entity;

import java.util.Optional;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import io.choerodon.kb.infra.enums.PermissionConstants;
import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

import org.hzero.core.base.BaseConstants;
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

    /**
     * 获取唯一性查询条件
     * @return 唯一性查询条件, 如果唯一键不全则返回null
     */
    public PermissionRoleConfig generateUniqueQueryParam() {
        if(
                this.organizationId == null
                || this.projectId == null
                || StringUtils.isBlank(this.permissionCode)
                || StringUtils.isBlank(this.permissionRoleCode)
        ) {
            return null;
        } else {
            return new PermissionRoleConfig()
                    .setOrganizationId(this.organizationId)
                    .setProjectId(this.projectId)
                    .setPermissionCode(this.permissionCode)
                    .setPermissionRoleCode(this.permissionRoleCode);
        }
    }

    /**
     * 创建前的通用校验和处理
     */
    public PermissionRoleConfig validateAndProcessBeforeCreate() {
        this.validateAndProcessBeforeCreateOrUpdate();
        Assert.notNull(this.organizationId, BaseConstants.ErrorCode.NOT_NULL);
        Assert.notNull(this.projectId, BaseConstants.ErrorCode.NOT_NULL);
        Assert.isTrue(PermissionConstants.ActionPermission.isValid(this.permissionCode), BaseConstants.ErrorCode.DATA_INVALID);
        Assert.isTrue(PermissionConstants.PermissionRole.isValidForPermissionRoleConfig(this.permissionRoleCode), BaseConstants.ErrorCode.DATA_INVALID);

        this.id = null;
        return this;
    }

    /**
     * 更新前的通用校验和处理
     * @return this
     */
    public PermissionRoleConfig validateAndProcessBeforeUpdate() {
        this.validateAndProcessBeforeCreateOrUpdate();
        Assert.notNull(this.id, BaseConstants.ErrorCode.NOT_NULL);
        return this;
    }

    /**
     * 将权限Code翻译为权限描述
     * @return this
     */
    public PermissionRoleConfig translatePermissionCode() {
        this.permissionDesc = Optional.ofNullable(PermissionConstants.ActionPermission.ofCode(this.permissionCode))
                .map(PermissionConstants.ActionPermission::getDescription)
                .orElse(null);
        return this;
    }

    /**
     * 创建或更新前的通用校验和操作
     */
    protected void validateAndProcessBeforeCreateOrUpdate() {
        if(this.authorizeFlag == null) {
            this.authorizeFlag = Boolean.FALSE;
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
    private Long organizationId;
    @ApiModelProperty(value = "项目ID", required = true)
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

    @Transient
    @ApiModelProperty(value = "操作权限描述")
    private String permissionDesc;

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

    /**
     * @return 操作权限描述
     */
    public String getPermissionDesc() {
        return permissionDesc;
    }

    public PermissionRoleConfig setPermissionDesc(String permissionDesc) {
        this.permissionDesc = permissionDesc;
        return this;
    }
}
