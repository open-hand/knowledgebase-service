package io.choerodon.kb.api.vo.permission;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.entity.PermissionRoleConfig;


/**
 * 知识库鉴权 VO
 * @author gaokuo.dai@zknow.com 2022-10-12
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel("知识库鉴权 VO")
public class PermissionCheckVO {

    /**
     * @see PermissionRoleConfig#getPermissionCode()
     */
    @ApiModelProperty("操作权限Code")
    @NotBlank
    private String permissionCode;
    @ApiModelProperty("是否有权限")
    private Boolean approve;
    /**
     * @see PermissionRange#getPermissionRoleCode()
     */
    @ApiModelProperty("授权角色")
    private String controllerType;

    /**
     * @see PermissionRoleConfig#getPermissionCode()
     * @return 操作权限Code
     */
    public String getPermissionCode() {
        return permissionCode;
    }

    public PermissionCheckVO setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
        return this;
    }

    /**
     * @return 是否有权限
     */
    public Boolean getApprove() {
        return approve;
    }

    public PermissionCheckVO setApprove(Boolean approve) {
        this.approve = approve;
        return this;
    }

    /**
     * @see PermissionRange#getPermissionRoleCode()
     * @return 授权角色
     */
    public String getControllerType() {
        return controllerType;
    }

    public PermissionCheckVO setControllerType(String controllerType) {
        this.controllerType = controllerType;
        return this;
    }
}
