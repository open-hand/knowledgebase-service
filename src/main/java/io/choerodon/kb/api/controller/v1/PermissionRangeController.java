package io.choerodon.kb.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.api.vo.permission.OrganizationPermissionSettingVO;
import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.choerodon.kb.domain.repository.PermissionRangeKnowledgeBaseSettingRepository;
import io.choerodon.kb.domain.service.PermissionRangeKnowledgeObjectSettingService;
import io.choerodon.swagger.annotation.Permission;

import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;

/**
 * 知识库权限应用范围 管理 API
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
@RestController("permissionRangeController.v1")
@RequestMapping("/v1/{organizationId}/permission/range")
public class PermissionRangeController extends BaseController {

    @Autowired
    private PermissionRangeKnowledgeBaseSettingRepository permissionRangeKBSettingRepository;
    @Autowired
    private PermissionRangeKnowledgeObjectSettingService permissionRangeKnowledgeObjectSettingService;

    @ApiOperation(value = "组织知识库权限设置查询(创建权限&默认权限)")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/setting")
    public ResponseEntity<OrganizationPermissionSettingVO> queryForOrganizationPermissionSettingVO(
            @PathVariable Long organizationId) {
        OrganizationPermissionSettingVO settingVO = this.permissionRangeKBSettingRepository.queryOrgPermissionSetting(organizationId);
        return Results.success(settingVO);
    }

    @ApiOperation(value = "组织知识库权限设置保存(创建权限&默认权限)")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PutMapping("/setting")
    public ResponseEntity<OrganizationPermissionSettingVO> saveOrganizationPermissionSettingVO(
            @PathVariable Long organizationId,
            @RequestBody OrganizationPermissionSettingVO organizationPermissionSetting) {
        OrganizationPermissionSettingVO settingVO = this.permissionRangeKBSettingRepository.queryOrgPermissionSetting(organizationId);
        return Results.success(settingVO);
    }

    @ApiOperation(value = "修改知识库权限应用范围")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PutMapping("/projectId/{projectId}/save")
    public ResponseEntity<PermissionDetailVO> save(
            @PathVariable Long organizationId,
            @PathVariable Long projectId,
            @RequestBody @Validated PermissionDetailVO permissionRanges) {
        return Results.success(permissionRangeKnowledgeObjectSettingService.save(organizationId, projectId, permissionRanges));
    }

}
