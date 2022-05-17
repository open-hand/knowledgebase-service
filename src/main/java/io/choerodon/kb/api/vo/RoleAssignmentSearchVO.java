package io.choerodon.kb.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Zenger on 2019/5/14.
 */
public class RoleAssignmentSearchVO {

    @ApiModelProperty(value = "登录名")
    private String loginName;

    @ApiModelProperty(value = "角色名")
    private String roleName;

    @ApiModelProperty(value = "真实名称")
    private String realName;

    @ApiModelProperty(value = "查询参数")
    private String[] param;

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String[] getParam() {
        return param;
    }

    public void setParam(String[] param) {
        this.param = param;
    }
}
