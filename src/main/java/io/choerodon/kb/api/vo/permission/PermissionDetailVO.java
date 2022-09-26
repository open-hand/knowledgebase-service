package io.choerodon.kb.api.vo.permission;

import java.util.List;
import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.entity.SecurityConfig;
import io.choerodon.kb.infra.enums.PermissionConstants;
import io.choerodon.mybatis.domain.AuditDomain;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author superlee
 * @since 2022-09-26
 */
public class PermissionDetailVO extends AuditDomain {
    /**
     * {@link PermissionConstants.PermissionTargetType}
     */
    @ApiModelProperty(value = "控制对象类型", required = true)
    @NotBlank
    private String targetType;
    @ApiModelProperty(value = "控制对象")
    @Encrypt
    private Long targetValue;
    @ApiModelProperty(value = "协作者权限范围")
    private List<PermissionRange> permissionRanges;
    @ApiModelProperty(value = "安全设置")
    private SecurityConfig securityConfig;

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Long getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(Long targetValue) {
        this.targetValue = targetValue;
    }

    public List<PermissionRange> getPermissionRanges() {
        return permissionRanges;
    }

    public void setPermissionRanges(List<PermissionRange> permissionRanges) {
        this.permissionRanges = permissionRanges;
    }

    public SecurityConfig getSecurityConfig() {
        return securityConfig;
    }

    public void setSecurityConfig(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    public void validate(){

    }
}
