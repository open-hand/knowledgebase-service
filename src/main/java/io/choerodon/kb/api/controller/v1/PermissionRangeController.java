package io.choerodon.kb.api.controller.v1;

import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.api.vo.permission.OrganizationPermissionSettingVO;
import io.choerodon.kb.app.service.PermissionRangeService;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.repository.PermissionRangeRepository;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;

import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;

/**
 * 知识库权限应用范围 管理 API
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
@RestController("permissionRangeController.v1")
@RequestMapping("/v1/{organizationId}/permission/ranges")
public class PermissionRangeController extends BaseController {

    @Autowired
    private PermissionRangeRepository permissionRangeRepository;
    @Autowired
    private PermissionRangeService permissionRangeService;

    @ApiOperation(value = "知识库权限应用范围列表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/page")
    public ResponseEntity<Page<PermissionRange>> pagePermissionRange(
            @PathVariable("organizationId") Long organizationId,
            PermissionRange queryParam,
            @ApiIgnore @SortDefault(value = PermissionRange.FIELD_ID, direction = Sort.Direction.DESC) PageRequest pageRequest) {
        Page<PermissionRange> list = permissionRangeRepository.pageAndSort(pageRequest, queryParam);
        return Results.success(list);
    }

    @ApiOperation(value = "租户知识库权限设置查询(创建权限&默认权限)")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/setting")
    public ResponseEntity<OrganizationPermissionSettingVO> findPermissionRange(
            @PathVariable("organizationId") Long organizationId) {
        OrganizationPermissionSettingVO settingVO = permissionRangeService.queryOrgPermissionSetting(organizationId);
        return Results.success(settingVO);
    }

    @ApiOperation(value = "知识库权限应用范围明细")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{id}")
    public ResponseEntity<PermissionRange> findPermissionRangeById(
            @PathVariable("organizationId") Long organizationId,
            @PathVariable Long id) {
        PermissionRange permissionRange = permissionRangeRepository.selectByPrimaryKey(id);
        return Results.success(permissionRange);
    }

    @ApiOperation(value = "创建知识库权限应用范围")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<PermissionRange> create(
            @PathVariable("organizationId") Long organizationId,
            @RequestBody PermissionRange permissionRange) {
        permissionRangeService.create(organizationId, permissionRange);
        return Results.success(permissionRange);
    }

    @ApiOperation(value = "修改知识库权限应用范围")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PutMapping
    public ResponseEntity<PermissionRange> update(
            @PathVariable("organizationId") Long organizationId,
            @RequestBody PermissionRange permissionRange) {
        permissionRangeService.update(organizationId, permissionRange);
        return Results.success(permissionRange);
    }

    @ApiOperation(value = "修改知识库权限应用范围")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PutMapping("/projectId/{projectId}/save")
    public ResponseEntity<PermissionDetailVO> save(
            @PathVariable("organizationId") Long organizationId,
            @PathVariable("projectId") Long projectId,
            @RequestBody @Validated PermissionDetailVO permissionRanges) {
        return Results.success(permissionRangeService.save(organizationId, projectId, permissionRanges));
    }

    @ApiOperation(value = "删除知识库权限应用范围")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping
    public ResponseEntity<Void> remove(@RequestBody PermissionRange permissionRange) {
        permissionRangeService.remove(permissionRange);
        return Results.success();
    }

}
