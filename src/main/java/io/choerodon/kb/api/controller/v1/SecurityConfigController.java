package io.choerodon.kb.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.choerodon.kb.api.vo.permission.PermissionSearchVO;
import io.choerodon.kb.app.service.SecurityConfigService;
import io.choerodon.kb.domain.entity.SecurityConfig;
import io.choerodon.kb.domain.repository.SecurityConfigRepository;
import io.choerodon.kb.infra.enums.PermissionConstants;
import io.choerodon.swagger.annotation.Permission;

import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 知识库安全设置 管理 API
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
@RestController("securityConfigController.v1")
@RequestMapping("/v1/organizations/{organizationId}/permission/security-config")
public class SecurityConfigController extends BaseController {

    @Autowired
    private SecurityConfigRepository securityConfigRepository;
    @Autowired
    private SecurityConfigService securityConfigService;

    @ApiOperation(value = "查询组织层知识库/文件夹/文档安全设置")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping
    public ResponseEntity<List<SecurityConfig>> queryOrgSecurityConfig(
            @PathVariable("organizationId") Long organizationId,
            @Encrypt PermissionSearchVO permissionSearchVO) {
        validObject(permissionSearchVO);
        List<SecurityConfig> collaborator = securityConfigRepository.queryByTarget(organizationId, PermissionConstants.EMPTY_ID_PLACEHOLDER, permissionSearchVO);
        return Results.success(collaborator);
    }

    @ApiOperation(value = "查询项目层知识库/文件夹/文档安全设置")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/projects/{projectId}")
    public ResponseEntity<List<SecurityConfig>> queryProjectSecurityConfig(
            @PathVariable Long organizationId,
            @PathVariable Long projectId,
            @Encrypt PermissionSearchVO permissionSearchVO) {
        validObject(permissionSearchVO);
        List<SecurityConfig> collaborator = securityConfigRepository.queryByTarget(organizationId, projectId, permissionSearchVO);
        return Results.success(collaborator);
    }

    @ApiOperation(value = "项目层修改知识库权限应用范围和安全设置")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/save-security")
    public ResponseEntity<PermissionDetailVO> orgSaveSecurity(@PathVariable Long organizationId,
                                                              @RequestBody @Validated PermissionDetailVO permissionDetailVO) {
        return Results.success(securityConfigService.saveSecurity(organizationId, PermissionConstants.EMPTY_ID_PLACEHOLDER, permissionDetailVO));
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
