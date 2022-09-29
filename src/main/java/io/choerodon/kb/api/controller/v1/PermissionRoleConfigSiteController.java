package io.choerodon.kb.api.controller.v1;

import java.util.List;
import java.util.stream.Collectors;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.app.service.PermissionRoleConfigService;
import io.choerodon.kb.domain.entity.PermissionRoleConfig;
import io.choerodon.kb.domain.repository.PermissionRoleConfigRepository;
import io.choerodon.kb.infra.enums.PermissionConstants;
import io.choerodon.swagger.annotation.Permission;

import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import org.hzero.mybatis.helper.SecurityTokenHelper;

/**
 * 知识库权限矩阵 管理 API-平台级
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
@RestController("permissionRoleConfigSiteController.v1")
@RequestMapping("/v1/permission/role-config")
public class PermissionRoleConfigSiteController extends BaseController {


    @Autowired
    private PermissionRoleConfigRepository permissionRoleConfigRepository;
    @Autowired
    private PermissionRoleConfigService permissionRoleConfigService;

    @ApiOperation(value = "知识库权限矩阵列表")
    @Permission(level = ResourceLevel.SITE)
    @GetMapping("/list")
    public ResponseEntity<List<PermissionRoleConfig>> listPermissionRoleConfig(
            PermissionRoleConfig queryParam
    ) {
        if(queryParam == null) {
            queryParam = new PermissionRoleConfig();
        }
        queryParam.setOrganizationId(PermissionConstants.EMPTY_ID_PLACEHOLDER).setProjectId(PermissionConstants.EMPTY_ID_PLACEHOLDER);
        List<PermissionRoleConfig> list = permissionRoleConfigRepository.select(queryParam);
        return Results.success(list.stream().map(PermissionRoleConfig::translatePermissionCode).collect(Collectors.toList()));
    }

    @ApiOperation(value = "批量保存知识库权限矩阵")
    @Permission(level = ResourceLevel.SITE)
    @PutMapping("/save/batch")
    public ResponseEntity<List<PermissionRoleConfig>> updatePermissionRoleConfig(
            @RequestBody List<PermissionRoleConfig> permissionRoleConfigs
    ) {
        this.validList(permissionRoleConfigs);
        SecurityTokenHelper.validToken(permissionRoleConfigs.stream().filter(permissionRoleConfig -> permissionRoleConfig.getId() != null).collect(Collectors.toList()));
        permissionRoleConfigs = permissionRoleConfigService.batchCreateOrUpdate(PermissionConstants.EMPTY_ID_PLACEHOLDER, PermissionConstants.EMPTY_ID_PLACEHOLDER, permissionRoleConfigs);
        return Results.success(permissionRoleConfigs);
    }

}
