package io.choerodon.kb.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.api.vo.permission.OrganizationPermissionSettingVO;
import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.choerodon.kb.api.vo.permission.PermissionSearchVO;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.repository.PermissionRangeKnowledgeBaseSettingRepository;
import io.choerodon.kb.domain.service.PermissionRangeKnowledgeBaseSettingService;
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
@RequestMapping("/v1/organizations/{organizationId}/permission/range")
public class PermissionRangeController extends BaseController {

    @Autowired
    private PermissionRangeKnowledgeBaseSettingRepository permissionRangeKnowledgeBaseSettingRepository;
    @Autowired
    private PermissionRangeKnowledgeBaseSettingService permissionRangeKnowledgeBaseSettingService;
    @Autowired
    private PermissionRangeKnowledgeObjectSettingService permissionRangeKnowledgeObjectSettingService;

    @ApiOperation(value = "组织知识库权限设置查询(创建权限&默认权限)")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/setting")
    public ResponseEntity<OrganizationPermissionSettingVO> queryForOrganizationPermissionSettingVO(
            @PathVariable Long organizationId) {
        OrganizationPermissionSettingVO settingVO = this.permissionRangeKnowledgeBaseSettingRepository.queryOrgPermissionSetting(organizationId);
        return Results.success(settingVO);
    }

    @ApiOperation(value = "组织知识库权限设置保存(创建权限&默认权限)")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PutMapping("/setting")
    public ResponseEntity<Void> saveOrganizationPermissionSettingVO(
            @PathVariable Long organizationId,
            @RequestBody OrganizationPermissionSettingVO organizationPermissionSetting) {
        this.permissionRangeKnowledgeBaseSettingService.save(organizationId, organizationPermissionSetting);
        return Results.success();
    }

    @ApiOperation(value = "查询组织层知识库/文件夹/文档已有协作者")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/collaborators")
    public ResponseEntity<List<PermissionRange>> queryOrganizationCollaborator(
            @PathVariable("organizationId") Long organizationId,
            PermissionSearchVO permissionSearchVO) {
        validObject(permissionSearchVO);
        List<PermissionRange> collaborator = permissionRangeKnowledgeObjectSettingService.queryCollaboratorAndSecuritySetting(organizationId, 0L, permissionSearchVO);
        return Results.success(collaborator);
    }

    @ApiOperation(value = "查询项目层知识库/文件夹/文档已有协作者")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/projects/{projectId}/collaborators")
    public ResponseEntity<List<PermissionRange>> queryProjectCollaborator(
            @PathVariable Long organizationId,
            @PathVariable Long projectId,
            PermissionSearchVO permissionSearchVO) {
        validObject(permissionSearchVO);
        List<PermissionRange> collaborator = permissionRangeKnowledgeObjectSettingService.queryCollaboratorAndSecuritySetting(organizationId, projectId, permissionSearchVO);
        return Results.success(collaborator);
    }

    @ApiOperation(value = "组织层修改知识库权限应用范围和安全设置")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/save-range-security")
    public ResponseEntity<PermissionDetailVO> orgSaveRangeAndSecurity(@PathVariable Long organizationId,
                                                                      @RequestBody PermissionDetailVO permissionDetailVO) {
        validObject(permissionDetailVO);
        return Results.success(permissionRangeKnowledgeObjectSettingService.saveRangeAndSecurity(organizationId, 0L, permissionDetailVO));
    }

    @ApiOperation(value = "组织层修改知识库权限应用范围")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/save-range")
    public ResponseEntity<PermissionDetailVO> orgSaveRange(@PathVariable Long organizationId,
                                                           @RequestBody PermissionDetailVO permissionDetailVO) {
        validObject(permissionDetailVO);
        return Results.success(permissionRangeKnowledgeObjectSettingService.saveRange(organizationId, 0L, permissionDetailVO));
    }

    @ApiOperation(value = "项目层修改知识库权限应用范围和安全设置")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/projects/{projectId}/save-range-security")
    public ResponseEntity<PermissionDetailVO> projectSaveRangeAndSecurity(@PathVariable Long organizationId,
                                                                          @PathVariable Long projectId,
                                                                          @RequestBody PermissionDetailVO permissionDetailVO) {
        validObject(permissionDetailVO);
        return Results.success(permissionRangeKnowledgeObjectSettingService.saveRangeAndSecurity(organizationId, projectId, permissionDetailVO));
    }

    @ApiOperation(value = "项目层修改知识库权限应用范围")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/projects/{projectId}/save-range")
    public ResponseEntity<PermissionDetailVO> projectSaveRange(@PathVariable Long organizationId,
                                                               @PathVariable Long projectId,
                                                               @RequestBody PermissionDetailVO permissionDetailVO) {
        validObject(permissionDetailVO);
        return Results.success(permissionRangeKnowledgeObjectSettingService.saveRange(organizationId, projectId, permissionDetailVO));
    }

}
