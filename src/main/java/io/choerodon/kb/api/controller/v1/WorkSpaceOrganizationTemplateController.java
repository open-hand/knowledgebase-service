package io.choerodon.kb.api.controller.v1;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.api.vo.PageCreateWithoutContentVO;
import io.choerodon.kb.api.vo.PageUpdateVO;
import io.choerodon.kb.api.vo.UploadFileStatusVO;
import io.choerodon.kb.api.vo.WorkSpaceInfoVO;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.domain.repository.WorkSpaceRepository;
import io.choerodon.kb.infra.enums.FileSourceType;
import io.choerodon.kb.infra.utils.EncryptUtil;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.hzero.starter.keyencrypt.core.IEncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/v1/organizations/{organization_id}/work_space/template")
public class WorkSpaceOrganizationTemplateController {

    @Autowired
    private WorkSpaceService workSpaceService;
    @Autowired
    private IEncryptionService encryptionService;
    @Autowired
    private WorkSpaceRepository workSpaceRepository;


    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "组织下创建知识库模板")
    @PostMapping
    public ResponseEntity<WorkSpaceInfoVO> workSpaceTemplate(@ApiParam(value = "组织id", required = true)
                                                             @PathVariable(value = "organization_id") Long organizationId,
                                                             @ApiParam(value = "页面信息", required = true)
                                                             @RequestBody @Valid @Encrypt PageCreateWithoutContentVO pageCreateVO) {
        return Results.success(workSpaceService.createWorkSpaceAndPage(organizationId, null, pageCreateVO, false, true));
    }


    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "组织下编辑知识库模板")
    @PutMapping(value = "/{id}")
    public ResponseEntity<WorkSpaceInfoVO> update(@ApiParam(value = "组织id", required = true)
                                                  @PathVariable(value = "organization_id") Long organizationId,
                                                  @ApiParam(value = "工作空间目录id", required = true)
                                                  @PathVariable @Encrypt Long id,
                                                  @ApiParam(value = "应用于全文检索时，对单篇文章，根据检索内容高亮内容")
                                                  @RequestParam(required = false) String searchStr,
                                                  @ApiParam(value = "空间信息", required = true)
                                                  @RequestBody @Valid PageUpdateVO pageUpdateVO) {
        WorkSpaceInfoVO ws = workSpaceService.updateWorkSpaceAndPage(organizationId, null, id, searchStr, pageUpdateVO,
                true, true);
        ws.setRoute(EncryptUtil.entryRoute(ws.getRoute(), encryptionService));
        return Results.success(ws);
    }


    @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
    @ApiOperation(value = "组织下查询知识库模板")
    @GetMapping(value = "/{id}")
    public ResponseEntity<WorkSpaceInfoVO> query(@ApiParam(value = "组织id", required = true)
                                                 @PathVariable(value = "organization_id") Long organizationId,
                                                 @ApiParam(value = "工作空间目录id", required = true)
                                                 @PathVariable @Encrypt Long id,
                                                 @ApiParam(value = "应用于全文检索时，对单篇文章，根据检索内容高亮内容")
                                                 @RequestParam(required = false) String searchStr) {
        WorkSpaceInfoVO ws = workSpaceRepository.queryWorkSpaceInfo(organizationId, null, id, searchStr, true, true);
        ws.setRoute(EncryptUtil.entryRoute(ws.getRoute(), encryptionService));
        if (ws.getWorkSpace() != null) {
            ws.getWorkSpace().setRoute(EncryptUtil.entryRoute(ws.getWorkSpace().getRoute(), encryptionService));
        }
        return Results.success(ws);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "组织下移除知识库模板（管理员权限）")
    @PutMapping(value = "/remove/{id}")
    public ResponseEntity<Void> removeWorkSpaceAndPage(@ApiParam(value = "组织id", required = true)
                                                       @PathVariable(value = "organization_id") Long organizationId,
                                                       @ApiParam(value = "工作空间目录id", required = true)
                                                       @PathVariable @Encrypt Long id) {
        workSpaceService.moveToRecycle(organizationId, null, id, true, true, true);
        return Results.success();
    }

    @PostMapping("/upload")
    @ApiOperation("上传文件")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<WorkSpaceInfoVO> upload(@ApiParam(value = "项目id", required = true)
                                                  @PathVariable(value = "organization_id") Long organizationId,
                                                  @ApiParam(value = "页面创建vo", required = true)
                                                  @RequestBody PageCreateWithoutContentVO pageCreateWithoutContentVO) {
        pageCreateWithoutContentVO.setFileSourceType(FileSourceType.UPLOAD.getFileSourceType());
        pageCreateWithoutContentVO.setSourceType(ResourceLevel.ORGANIZATION.value());
        pageCreateWithoutContentVO.setSourceId(organizationId);
        return Results.success(workSpaceService.upload(null, organizationId, pageCreateWithoutContentVO, true));
    }

    @GetMapping("/upload/status")
    @ApiOperation("项目层查询文件上传状态")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<UploadFileStatusVO> queryUploadStatus(
            @ApiParam(value = "组织id", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "页面创建vo", required = true)
            @Encrypt @RequestParam(value = "ref_id") Long refId,
            @RequestParam(value = "source_type") String sourceType) {
        return Results.success(workSpaceRepository.queryUploadStatus(null, organizationId, refId, sourceType));
    }



    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "组织下启用知识库模板")
    @PutMapping("/enable")
    public ResponseEntity<Void> enableWorkSpaceTemplate(@ApiParam(value = "组织id", required = true)
                                                        @PathVariable(value = "organization_id") Long organizationId,
                                                        @Encrypt @RequestParam(value = "work_space_id") Long workSpaceId) {
        workSpaceService.enableWorkSpaceTemplate(organizationId, workSpaceId);
        return Results.success();
    }


    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "组织下停用知识库模板")
    @PutMapping("/disable")
    public ResponseEntity<WorkSpaceInfoVO> disableWorkSpaceTemplate(@ApiParam(value = "组织id", required = true)
                                                                    @PathVariable(value = "organization_id") Long organizationId,
                                                                    @Encrypt @RequestParam(value = "work_space_id") Long workSpaceId) {
        workSpaceService.disableWorkSpaceTemplate(organizationId, workSpaceId);
        return Results.success();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "组织下发布知识库模板")
    @PutMapping("/publish")
    public ResponseEntity<WorkSpaceInfoVO> publishWorkSpaceTemplate(@ApiParam(value = "组织id", required = true)
                                                                    @PathVariable(value = "organization_id") Long organizationId,
                                                                    @Encrypt @RequestParam(value = "work_space_id") Long workSpaceId) {
        workSpaceService.publishWorkSpaceTemplate(organizationId, workSpaceId);
        return Results.success();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "组织下取消发布知识库模板")
    @PutMapping("/un-publish")
    public ResponseEntity<WorkSpaceInfoVO> unPublishWorkSpaceTemplate(@ApiParam(value = "组织id", required = true)
                                                                      @PathVariable(value = "organization_id") Long organizationId,
                                                                      @Encrypt @RequestParam(value = "work_space_id") Long workSpaceId) {
        workSpaceService.unPublishWorkSpaceTemplate(organizationId, workSpaceId);
        return Results.success();
    }

}
