package io.choerodon.kb.api.vo.permission;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.entity.SecurityConfig;

/**
 * 知识库对象权限详情VO
 * @author superlee
 * @since 2022-09-26
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PermissionDetailVO extends PermissionSearchVO<PermissionDetailVO> {

    /**
     * 快速创建
     *
     * @param targetType       targetType
     * @param targetValue      targetValue
     * @param permissionRanges permissionRanges
     * @return 创建结果
     */
    public static PermissionDetailVO of(String targetType, Long targetValue, List<PermissionRange> permissionRanges) {
        return of(targetType, targetValue, permissionRanges, null);
    }

    /**
     * 快速创建
     *
     * @param targetType       targetType
     * @param targetValue      targetValue
     * @param permissionRanges permissionRanges
     * @param securityConfigs  securityConfigs
     * @return 创建结果
     */
    public static PermissionDetailVO of(String targetType, Long targetValue, List<PermissionRange> permissionRanges, List<SecurityConfig> securityConfigs) {
        PermissionDetailVO result = new PermissionDetailVO();
        result.setTargetType(targetType);
        result.setTargetValue(targetValue);
        result.permissionRanges = permissionRanges;
        result.securityConfigs = securityConfigs;
        return result;
    }

    @ApiModelProperty(value = "协作者权限范围")
    private List<PermissionRange> permissionRanges;
    @ApiModelProperty(value = "安全设置")
    private List<SecurityConfig> securityConfigs;

    /**
     * @return 协作者权限范围
     */
    public List<PermissionRange> getPermissionRanges() {
        return permissionRanges;
    }

    public void setPermissionRanges(List<PermissionRange> permissionRanges) {
        this.permissionRanges = permissionRanges;
    }

    /**
     * @return 安全设置
     */
    public List<SecurityConfig> getSecurityConfigs() {
        return securityConfigs;
    }

    public void setSecurityConfigs(List<SecurityConfig> securityConfigs) {
        this.securityConfigs = securityConfigs;
    }
}
