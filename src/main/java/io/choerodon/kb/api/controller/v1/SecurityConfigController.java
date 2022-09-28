package io.choerodon.kb.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.choerodon.kb.app.service.SecurityConfigService;
import io.choerodon.swagger.annotation.Permission;

import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;

/**
 * 知识库安全设置 管理 API
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
@RestController("securityConfigController.v1")
@RequestMapping("/v1/organizations/{organizationId}/permission/security-config")
public class SecurityConfigController extends BaseController {

    @Autowired
    private SecurityConfigService securityConfigService;

    @ApiOperation(value = "项目层修改知识库权限应用范围和安全设置")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/save-security")
    public ResponseEntity<PermissionDetailVO> orgSaveSecurity(@PathVariable Long organizationId,
                                                              @RequestBody @Validated PermissionDetailVO permissionDetailVO) {
        return Results.success(securityConfigService.saveSecurity(organizationId, 0L, permissionDetailVO));
    }

    @ApiOperation(value = "项目层修改知识库权限应用范围")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/projects/{projectId}/save-security")
    public ResponseEntity<PermissionDetailVO> projectSaveSecurity(@PathVariable Long organizationId,
                                                                  @PathVariable Long projectId,
                                                                  @RequestBody @Validated PermissionDetailVO permissionDetailVO) {
        return Results.success(securityConfigService.saveSecurity(organizationId, projectId, permissionDetailVO));
    }

}
