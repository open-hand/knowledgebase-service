package io.choerodon.kb.api.controller.v1;

import io.choerodon.swagger.annotation.Permission;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.api.vo.PageAttachmentVO;
import io.choerodon.kb.app.service.PageAttachmentService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.hzero.starter.keyencrypt.core.IEncryptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by Zenger on 2019/4/30.
 */
@RestController
@RequestMapping(value = "/v1/organizations/{organization_id}/page_attachment")
public class PageAttachmentOrganizationController {

    private PageAttachmentService pageAttachmentService;
    private IEncryptionService encryptionService;

    public PageAttachmentOrganizationController(PageAttachmentService pageAttachmentService,
                                                IEncryptionService encryptionService) {
        this.pageAttachmentService = pageAttachmentService;
        this.encryptionService = encryptionService;
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("页面上传附件")
    @PostMapping
    public ResponseEntity<List<PageAttachmentVO>> create(@ApiParam(value = "组织ID", required = true)
                                                         @PathVariable(value = "organization_id") Long organizationId,
                                                         @ApiParam(value = "页面ID", required = true)
                                                         @RequestParam @Encrypt Long pageId,
                                                         HttpServletRequest request) {
        return new ResponseEntity<>(pageAttachmentService.create(organizationId, pageId, pageId, ((MultipartHttpServletRequest) request).getFiles("file")), HttpStatus.CREATED);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询页面附件")
    @GetMapping(value = "/list")
    public ResponseEntity<List<PageAttachmentVO>> queryByList(
            @ApiParam(value = "组织id", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "页面id", required = true)
            @RequestParam @Encrypt Long pageId) {
        return new ResponseEntity<>(pageAttachmentService.queryByList(organizationId, null, pageId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("页面删除附件")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity delete(@ApiParam(value = "组织ID", required = true)
                                 @PathVariable(value = "organization_id") Long organizationId,
                                 @ApiParam(value = "附件ID", required = true)
                                 @PathVariable @Encrypt Long id) {
        pageAttachmentService.delete(organizationId, null, id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("页面批量删除附件")
    @PostMapping(value = "/batch_delete")
    public ResponseEntity batchDelete(@ApiParam(value = "项目ID", required = true)
                                      @PathVariable(value = "organization_id") Long organizationId,
                                      @ApiParam(value = "附件ID", required = true)
                                      @RequestBody @Encrypt List<Long> idList) {
        pageAttachmentService.batchDelete(organizationId, null, idList);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("上传附件，直接返回地址")
    @PostMapping(value = "/upload_for_address")
    public ResponseEntity<List<String>> uploadForAddress(@ApiParam(value = "组织ID", required = true)
                                                         @PathVariable(value = "organization_id") Long organizationId,
                                                         HttpServletRequest request) {
        return new ResponseEntity<>(pageAttachmentService.uploadForAddress(organizationId, ((MultipartHttpServletRequest) request).getFiles("file")),
                HttpStatus.CREATED);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("根据文件名获取附件地址，用于编辑文档中快捷找到附件地址")
    @GetMapping(value = "/query_by_file_name")
    public ResponseEntity<PageAttachmentVO> queryByFileName(@ApiParam(value = "组织ID", required = true)
                                                            @PathVariable(value = "organization_id") Long organizationId,
                                                            @ApiParam(value = "文件名", required = true)
                                                            @RequestParam String fileName) {
        return new ResponseEntity<>(pageAttachmentService.queryByFileName(organizationId, null, fileName), HttpStatus.OK);
    }
}
