package io.choerodon.kb.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.app.service.PermissionRoleConfigService;
import io.choerodon.kb.domain.entity.PermissionRoleConfig;
import io.choerodon.kb.domain.repository.PermissionRoleConfigRepository;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;

import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;

/**
 * 知识库权限矩阵 管理 API
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
@RestController("pmsRoleConfigController.v1")
@RequestMapping("/v1/{organizationId}/pms-role-configs")
public class PermissionRoleConfigController extends BaseController {

    @Autowired
    private PermissionRoleConfigRepository permissionRoleConfigRepository;
    @Autowired
    private PermissionRoleConfigService permissionRoleConfigService;

    @ApiOperation(value = "知识库权限矩阵列表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/page")
    public ResponseEntity<Page<PermissionRoleConfig>> pagePermissionRoleConfig(
            @PathVariable("organizationId") Long organizationId,
            PermissionRoleConfig queryParam,
            @ApiIgnore @SortDefault(value = PermissionRoleConfig.FIELD_ID, direction = Sort.Direction.DESC) PageRequest pageRequest
    ) {
        Page<PermissionRoleConfig> list = permissionRoleConfigRepository.pageAndSort(pageRequest, queryParam);
        return Results.success(list);
    }

    @ApiOperation(value = "知识库权限矩阵明细")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{id}")
    public ResponseEntity<PermissionRoleConfig> findPermissionRoleConfigById(
            @PathVariable("organizationId") Long organizationId,
            @PathVariable Long id
    ) {
        PermissionRoleConfig permissionRoleConfig = permissionRoleConfigRepository.selectByPrimaryKey(id);
        return Results.success(permissionRoleConfig);
    }

    @ApiOperation(value = "创建知识库权限矩阵")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<PermissionRoleConfig> createPermissionRoleConfig(
            @PathVariable("organizationId") Long organizationId,
            @RequestBody PermissionRoleConfig permissionRoleConfig
    ) {
        permissionRoleConfigService.create(organizationId, permissionRoleConfig);
        return Results.success(permissionRoleConfig);
    }

    @ApiOperation(value = "修改知识库权限矩阵")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PutMapping
    public ResponseEntity<PermissionRoleConfig> updatePermissionRoleConfig(
            @PathVariable("organizationId") Long organizationId,
            @RequestBody PermissionRoleConfig permissionRoleConfig
    ) {
        permissionRoleConfigService.update(organizationId, permissionRoleConfig);
        return Results.success(permissionRoleConfig);
    }

    @ApiOperation(value = "删除知识库权限矩阵")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping
    public ResponseEntity<Void> removePermissionRoleConfig(@RequestBody PermissionRoleConfig permissionRoleConfig) {
        permissionRoleConfigService.remove(permissionRoleConfig);
        return Results.success();
    }

}
