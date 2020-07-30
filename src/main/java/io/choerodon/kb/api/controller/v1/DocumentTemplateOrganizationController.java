package io.choerodon.kb.api.controller.v1;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.Permission;

import io.choerodon.kb.api.vo.*;
import io.choerodon.kb.app.service.DocumentTemplateService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.hzero.starter.keyencrypt.core.IEncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * @author zhaotianxin
 * @since 2020/1/3
 */
@RestController
@RequestMapping("/v1/organizations/{organization_id}/document_template")
public class DocumentTemplateOrganizationController {
    @Autowired
    private DocumentTemplateService documentTemplateService;
    @Autowired
    private IEncryptionService encryptionService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("创建模板文件")
    @PostMapping(value = "/create")
    public ResponseEntity<DocumentTemplateInfoVO> create(
            @ApiParam(value = "组织ID", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @RequestParam(required = false) @Encrypt Long baseTemplateId,
            @ApiParam(value = "页面信息", required = true)
            @RequestBody @Valid PageCreateWithoutContentVO pageCreateVO) {
        return new ResponseEntity<>(documentTemplateService.createTemplate(0L, organizationId, pageCreateVO, baseTemplateId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "更新文档模板")
    @PutMapping(value = "/{id}")
    public ResponseEntity<WorkSpaceInfoVO> updateTemplate(@ApiParam(value = "组织ID", required = true)
                                                          @PathVariable(value = "organization_id") Long organizationId,
                                                          @ApiParam(value = "工作空间目录id", required = true)
                                                          @PathVariable @Encrypt Long id,
                                                          @ApiParam(value = "应用于全文检索时，对单篇文章，根据检索内容高亮内容")
                                                          @RequestParam(required = false) String searchStr,
                                                          @ApiParam(value = "空间信息", required = true)
                                                          @RequestBody @Valid PageUpdateVO pageUpdateVO) {
        return new ResponseEntity<>(documentTemplateService.updateTemplate(organizationId, 0L, id, searchStr, pageUpdateVO), HttpStatus.CREATED);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询模板列表")
    @PostMapping(value = "/template_list")
    public ResponseEntity<Page<DocumentTemplateInfoVO>> listTemplate(@ApiParam(value = "组织ID", required = true)
                                                                     @PathVariable(value = "organization_id") Long organizationId,
                                                                     @RequestParam @Encrypt Long baseId,
                                                                     @SortDefault PageRequest pageRequest,
                                                                     @RequestBody(required = false) SearchVO searchVO) {
        return new ResponseEntity<>(documentTemplateService.listTemplate(organizationId, 0L, baseId, pageRequest, searchVO), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("查询知识库模板")
    @PostMapping(value = "/list_system_template")
    public ResponseEntity<List<KnowledgeBaseTreeVO>> listSystemTemplate(@ApiParam(value = "组织ID", required = true)
                                                                        @PathVariable(value = "organization_id") Long organizationId,
                                                                        @RequestBody(required = false) SearchVO searchVO) {
        return new ResponseEntity<>(documentTemplateService.listSystemTemplate(organizationId, 0L, searchVO), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("组织层模板页面上传附件")
    @PostMapping("/upload_attach")
    public ResponseEntity<List<PageAttachmentVO>> uploadAttach(@ApiParam(value = "组织ID", required = true)
                                                               @PathVariable(value = "organization_id") Long organizationId,
                                                               @ApiParam(value = "页面ID", required = true)
                                                               @RequestParam @Encrypt Long pageId,
                                                               HttpServletRequest request) {
        return new ResponseEntity<>(documentTemplateService.createAttachment(organizationId, 0L, pageId, ((MultipartHttpServletRequest) request).getFiles("file")), HttpStatus.CREATED);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("组织层模板页面删除附件")
    @DeleteMapping(value = "/delete_attach/{id}")
    public ResponseEntity deleteAttach(@ApiParam(value = "组织ID", required = true)
                                       @PathVariable(value = "organization_id") Long organizationId,
                                       @ApiParam(value = "附件ID", required = true)
                                       @PathVariable @Encrypt Long id) {
        documentTemplateService.deleteAttachment(organizationId, 0L, id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "移除组织下工作空间及页面（管理员权限）")
    @PutMapping(value = "/remove/{id}")
    public ResponseEntity removeWorkSpaceAndPage(@ApiParam(value = "组织id", required = true)
                                                 @PathVariable(value = "organization_id") Long organizationId,
                                                 @ApiParam(value = "工作空间目录id", required = true)
                                                 @PathVariable @Encrypt Long id) {
        documentTemplateService.removeWorkSpaceAndPage(organizationId, 0L, id, true);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
