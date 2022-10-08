package io.choerodon.kb.domain.entity;

import static io.choerodon.kb.infra.enums.PermissionConstants.PermissionTargetType;

import java.util.*;
import java.util.stream.Collectors;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.collections4.list.UnmodifiableList;
import org.springframework.util.ObjectUtils;

import io.choerodon.kb.infra.enums.PermissionConstants;
import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

import org.hzero.core.base.BaseConstants;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 知识库安全设置
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
@ApiModel("知识库安全设置")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "kb_security_config")
public class SecurityConfig extends AuditDomain {

    public static final String FIELD_ID = "id";
    public static final String FIELD_ORGANIZATION_ID = "organizationId";
    public static final String FIELD_PROJECT_ID = "projectId";
    public static final String FIELD_TARGET_TYPE = "targetType";
    public static final String FIELD_TARGET_VALUE = "targetValue";
    public static final String FIELD_PERMISSION_CODE = "permissionCode";
    public static final String FIELD_AUTHORIZE_FLAG = "authorizeFlag";

//
// 业务方法(按public protected private顺序排列)
// ------------------------------------------------------------------------------

    public static SecurityConfig of(Long organizationId,
                                    Long projectId,
                                    String targetType,
                                    Long targetValue,
                                    String permissionCode,
                                    Integer authorizeFlag
    ) {
        SecurityConfig securityConfig = new SecurityConfig();
        securityConfig.setOrganizationId(organizationId);
        securityConfig.setProjectId(projectId);
        securityConfig.setTargetType(targetType);
        securityConfig.setTargetValue(targetValue);
        securityConfig.setPermissionCode(permissionCode);
        securityConfig.setAuthorizeFlag(authorizeFlag);
        return securityConfig;
    }

    /**
     * 生成对象默认的安全设置
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @param targetType        控制对象类型
     * @param targetValue       控制对象
     * @return                  默认的安全设置(注意是UnmodifiableList)
     */
    public static UnmodifiableList<SecurityConfig> generateDefaultSecurityConfig(
            Long organizationId,
            Long projectId,
            PermissionConstants.PermissionTargetType targetType,
            Long targetValue
    ) {
        // 获取targetType的字符串形式
        final String targetTypeString = Optional.ofNullable(targetType).map(PermissionConstants.PermissionTargetType::getCode).orElse(null);
        // 不可变处理
        return new UnmodifiableList<>(
                Optional.ofNullable(targetType)
                        // 获取基本类型
                        .map(PermissionConstants.PermissionTargetType::getBaseType)
                        // 转化为安全设置的permission code
                        .map(PermissionConstants.SecurityConfigAction::buildPermissionCodeByType)
                        // 类型体操
                        .map(ArrayList::new)
                        // 如果没有传入targetType则生成三个null占位符
                        // 为什么是三个呢, 因为SecurityConfigAction::buildPermissionCodeByType默认就生成3个
                        // 类型体操 again
                        .orElse(new ArrayList<>(Arrays.asList(null, null, null)))
                        .stream()
                        // 将permission code扩展为SecurityConfig对象
                        .map(permissionCode -> SecurityConfig.of(
                                organizationId,
                                projectId,
                                targetTypeString,
                                targetValue,
                                permissionCode,
                                BaseConstants.Flag.YES
                        )).collect(Collectors.toList()));
    }

    public void copy(String targetType, Long targetValue) {
        this.id = null;
        this.setCreatedBy(null);
        this.setLastUpdatedBy(null);
        this.setCreationDate(null);
        this.setLastUpdateDate(null);
        this.targetType = targetType;
        this.targetValue = targetValue;
        String[] array = this.permissionCode.split("\\.");
        String action = array[array.length - 1];
        PermissionTargetType permissionTargetType = PermissionTargetType.valueOf(targetType);
        String prefix = permissionTargetType.getBaseType().getKebabCaseName();
        StringBuilder builder = new StringBuilder(prefix).append(BaseConstants.Symbol.POINT).append(action);
        this.permissionCode = builder.toString();
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
    @ApiModelProperty(value = "控制对象类型", required = true)
    @NotBlank
    private String targetType;
    @ApiModelProperty(value = "控制对象", required = true)
    @NotNull
    private Long targetValue;
    @ApiModelProperty(value = "操作权限Code", required = true)
    @NotBlank
    private String permissionCode;
    @ApiModelProperty(value = "授权标识")
    @NotNull
    private Integer authorizeFlag;

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

    public SecurityConfig setId(Long id) {
        this.id = id;
        return this;
    }

    /**
     * @return 组织ID
     */
    public Long getOrganizationId() {
        return organizationId;
    }

    public SecurityConfig setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
        return this;
    }

    /**
     * @return 项目ID
     */
    public Long getProjectId() {
        return projectId;
    }

    public SecurityConfig setProjectId(Long projectId) {
        this.projectId = projectId;
        return this;
    }

    /**
     * @return 控制对象类型
     */
    public String getTargetType() {
        return targetType;
    }

    public SecurityConfig setTargetType(String targetType) {
        this.targetType = targetType;
        return this;
    }

    /**
     * @return 控制对象
     */
    public Long getTargetValue() {
        return targetValue;
    }

    public SecurityConfig setTargetValue(Long targetValue) {
        this.targetValue = targetValue;
        return this;
    }

    /**
     * @return 操作权限Code
     */
    public String getPermissionCode() {
        return permissionCode;
    }

    public SecurityConfig setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
        return this;
    }

    /**
     * @return 授权标识
     */
    public Integer getAuthorizeFlag() {
        return authorizeFlag;
    }

    public SecurityConfig setAuthorizeFlag(Integer authorizeFlag) {
        this.authorizeFlag = authorizeFlag;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SecurityConfig)) return false;
        SecurityConfig that = (SecurityConfig) o;
        return Objects.equals(getOrganizationId(), that.getOrganizationId()) &&
                Objects.equals(getProjectId(), that.getProjectId()) &&
                Objects.equals(getTargetType(), that.getTargetType()) &&
                Objects.equals(getTargetValue(), that.getTargetValue()) &&
                Objects.equals(getPermissionCode(), that.getPermissionCode()) &&
                Objects.equals(getAuthorizeFlag(), that.getAuthorizeFlag());
    }

    public boolean equalsWithoutAuthorizeFlag(Object o) {
        if (this == o) return true;
        if (!(o instanceof SecurityConfig)) return false;
        SecurityConfig that = (SecurityConfig) o;
        return Objects.equals(getOrganizationId(), that.getOrganizationId()) &&
                Objects.equals(getProjectId(), that.getProjectId()) &&
                Objects.equals(getTargetType(), that.getTargetType()) &&
                Objects.equals(getTargetValue(), that.getTargetValue()) &&
                Objects.equals(getPermissionCode(), that.getPermissionCode());
    }

    public boolean in(List<SecurityConfig> list,
                      boolean withoutAuthorizeFlag) {

        if (ObjectUtils.isEmpty(list)) {
            return false;
        }
        for (SecurityConfig securityConfig : list) {
            boolean in;
            if (withoutAuthorizeFlag) {
                in = securityConfig.equalsWithoutAuthorizeFlag(this);
            } else {
                in = securityConfig.equals(this);
            }
            if (in) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOrganizationId(), getProjectId(), getTargetType(), getTargetValue(), getPermissionCode(), getAuthorizeFlag());
    }
}
