package io.choerodon.kb.api.controller.v1;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.api.vo.permission.OrganizationPermissionSettingVO;
import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.repository.PermissionRangeKnowledgeBaseSettingRepository;
import io.choerodon.kb.domain.service.PermissionRangeKnowledgeObjectSettingService;
import io.choerodon.kb.infra.enums.PermissionConstants;
import io.choerodon.swagger.annotation.Permission;

import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 知识库权限应用范围 管理 API
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
@RestController("permissionRangeController.v1")
@RequestMapping("/v1/{organizationId}/permission/range")
public class PermissionRangeController extends BaseController {

    @Autowired
    private PermissionRangeKnowledgeBaseSettingRepository permissionRangeKnowledgeBaseSettingRepository;
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
    public ResponseEntity<OrganizationPermissionSettingVO> saveOrganizationPermissionSettingVO(
            @PathVariable Long organizationId,
            @RequestBody OrganizationPermissionSettingVO organizationPermissionSetting) {
        OrganizationPermissionSettingVO settingVO = this.permissionRangeKnowledgeBaseSettingRepository.queryOrgPermissionSetting(organizationId);
        return Results.success(settingVO);
    }

    @ApiOperation(value = "查询组织层知识库文件夹/文档已有协作者")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/target/{targetValue}/collaborators")
    public ResponseEntity<List<PermissionRange>> queryOrganizationCollaborator(
            @PathVariable("organizationId") Long organizationId,
            @PathVariable @Encrypt Long targetValue) {
        Set<String> targetTypes = Sets.newHashSet(PermissionConstants.PermissionTargetType.FOLDER_ORG.toString(), PermissionConstants.PermissionTargetType.FILE_ORG.toString());
        List<PermissionRange> collaborator = permissionRangeKnowledgeObjectSettingService.queryFolderOrFileCollaborator(organizationId, 0L, targetTypes, targetValue);
        return Results.success(collaborator);
    }

    @ApiOperation(value = "查询项目层知识库文件夹/文档已有协作者")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/projects/{projectId}/target/{targetValue}/collaborators")
    public ResponseEntity<List<PermissionRange>> queryProjectCollaborator(
            @PathVariable Long organizationId,
            @PathVariable Long projectId,
            @PathVariable @Encrypt Long targetValue) {
        Set<String> targetTypes = Sets.newHashSet(PermissionConstants.PermissionTargetType.FOLDER_PROJECT.toString(), PermissionConstants.PermissionTargetType.FILE_PROJECT.toString());
        List<PermissionRange> collaborator = permissionRangeKnowledgeObjectSettingService.queryFolderOrFileCollaborator(organizationId, projectId, targetTypes, targetValue);
        return Results.success(collaborator);
    }

    @ApiOperation(value = "组织层修改知识库权限应用范围和安全设置")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/save-range-security")
    public ResponseEntity<PermissionDetailVO> orgSaveRangeAndSecurity(@PathVariable Long organizationId,
                                                                      @RequestBody @Validated PermissionDetailVO permissionDetailVO) {
        return Results.success(permissionRangeKnowledgeObjectSettingService.saveRangeAndSecurity(organizationId, 0L, permissionDetailVO));
    }

    @ApiOperation(value = "组织层修改知识库权限应用范围")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/save-range")
    public ResponseEntity<PermissionDetailVO> orgSaveRange(@PathVariable Long organizationId,
                                                           @RequestBody @Validated PermissionDetailVO permissionDetailVO) {
        return Results.success(permissionRangeKnowledgeObjectSettingService.saveRange(organizationId, 0L, permissionDetailVO));
    }

    @ApiOperation(value = "项目层修改知识库权限应用范围和安全设置")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/projectId/{projectId}/save-range-security")
    public ResponseEntity<PermissionDetailVO> projectSaveRangeAndSecurity(@PathVariable Long organizationId,
                                                                          @PathVariable Long projectId,
                                                                          @RequestBody @Validated PermissionDetailVO permissionDetailVO) {
        return Results.success(permissionRangeKnowledgeObjectSettingService.saveRangeAndSecurity(organizationId, projectId, permissionDetailVO));
    }

    @ApiOperation(value = "项目层修改知识库权限应用范围")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/projectId/{projectId}/save-range")
    public ResponseEntity<PermissionDetailVO> projectSaveRange(@PathVariable Long organizationId,
                                                               @PathVariable Long projectId,
                                                               @RequestBody @Validated PermissionDetailVO permissionDetailVO) {
        return Results.success(permissionRangeKnowledgeObjectSettingService.saveRange(organizationId, projectId, permissionDetailVO));
    }

}
