package io.choerodon.kb.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.hzero.starter.keyencrypt.core.IEncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.api.vo.*;
import io.choerodon.kb.app.service.DocumentTemplateService;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * @author zhaotianxin
 * @since 2020/1/2
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/document_template")
public class DocumentTemplateController {

    @Autowired
    private DocumentTemplateService documentTemplateService;
    @Autowired
    private IEncryptionService encryptionService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("创建模板文件")
    @PostMapping(value = "/create")
    public ResponseEntity<DocumentTemplateInfoVO> create(@ApiParam(value = "项目ID", required = true)
                                                         @PathVariable(value = "project_id") Long projectId,
                                                         @ApiParam(value = "组织id", required = true)
                                                         @RequestParam Long organizationId,
                                                         @RequestParam(required = false) @Encrypt Long baseTemplateId,
                                                         @ApiParam(value = "页面信息", required = true)
                                                         @RequestBody @Valid @Encrypt PageCreateWithoutContentVO pageCreateVO) {
        return new ResponseEntity<>(documentTemplateService.createTemplate(projectId, 0L, pageCreateVO, baseTemplateId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "更新文档模板")
    @PutMapping(value = "/{id}")
    public ResponseEntity<WorkSpaceInfoVO> updateTemplate(@ApiParam(value = "项目id", required = true)
                                                          @PathVariable(value = "project_id") Long projectId,
                                                          @ApiParam(value = "组织id", required = true)
                                                          @RequestParam Long organizationId,
                                                          @ApiParam(value = "工作空间目录id", required = true)
                                                          @PathVariable @Encrypt Long id,
                                                          @ApiParam(value = "应用于全文检索时，对单篇文章，根据检索内容高亮内容")
                                                          @RequestParam(required = false) String searchStr,
                                                          @ApiParam(value = "空间信息", required = true)
                                                          @RequestBody @Valid PageUpdateVO pageUpdateVO) {
        return new ResponseEntity<>(documentTemplateService.updateTemplate(0L, projectId, id, searchStr, pageUpdateVO), HttpStatus.CREATED);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询模板列表")
    @PostMapping(value = "/template_list")
    public ResponseEntity<Page<DocumentTemplateInfoVO>> listTemplate(@ApiParam(value = "项目id", required = true)
                                                                     @PathVariable(value = "project_id") Long projectId,
                                                                     @ApiParam(value = "组织id", required = true)
                                                                     @RequestParam Long organizationId,
                                                                     @RequestParam @Encrypt Long baseId,
                                                                     @SortDefault PageRequest pageRequest,
                                                                     @RequestBody(required = false) @Encrypt SearchVO searchVO) {
        return new ResponseEntity<>(documentTemplateService.listTemplate(0L, projectId, baseId, pageRequest, searchVO), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("查询知识库模板")
    @PostMapping(value = "/list_system_template")
    public ResponseEntity<List<KnowledgeBaseTreeVO>> listSystemTemplate(@ApiParam(value = "项目id", required = true)
                                                                        @PathVariable(value = "project_id") Long projectId,
                                                                        @ApiParam(value = "组织id", required = true)
                                                                        @RequestParam Long organizationId,
                                                                        @RequestBody(required = false) SearchVO searchVO) {
        return new ResponseEntity<>(documentTemplateService.listSystemTemplate(organizationId, projectId, searchVO), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("模板页面上传附件")
    @PostMapping("/upload_attach")
    public ResponseEntity<List<PageAttachmentVO>> uploadAttach(@ApiParam(value = "项目ID", required = true)
                                                               @PathVariable(value = "project_id") Long projectId,
                                                               @ApiParam(value = "组织id", required = true)
                                                               @RequestParam Long organizationId,
                                                               @ApiParam(value = "页面ID", required = true)
                                                               @RequestParam @Encrypt Long pageId,
                                                               HttpServletRequest request) {
        return new ResponseEntity<>(documentTemplateService.createAttachment(0L, projectId, pageId, ((MultipartHttpServletRequest) request).getFiles("file")), HttpStatus.CREATED);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("模板页面删除附件")
    @DeleteMapping(value = "/delete_attach/{id}")
    public ResponseEntity deleteAttach(@ApiParam(value = "项目ID", required = true)
                                       @PathVariable(value = "project_id") Long projectId,
                                       @ApiParam(value = "组织id", required = true)
                                       @RequestParam Long organizationId,
                                       @ApiParam(value = "附件ID", required = true)
                                       @PathVariable @Encrypt Long id) {
        documentTemplateService.deleteAttachment(0L, projectId, id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "移除项目下工作空间及页面到回收站（管理员权限）")
    @PutMapping(value = "/remove/{id}")
    public ResponseEntity removeWorkSpaceAndPage(@ApiParam(value = "项目id", required = true)
                                                 @PathVariable(value = "project_id") Long projectId,
                                                 @ApiParam(value = "组织id", required = true)
                                                 @RequestParam Long organizationId,
                                                 @ApiParam(value = "工作空间目录id", required = true)
                                                 @PathVariable @Encrypt Long id) {
        documentTemplateService.removeWorkSpaceAndPage(0L, projectId, id, true);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
