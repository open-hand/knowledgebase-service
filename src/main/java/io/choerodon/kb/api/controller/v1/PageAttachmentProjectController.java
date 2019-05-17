package io.choerodon.kb.api.controller.v1;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.kb.api.dao.PageAttachmentDTO;
import io.choerodon.kb.app.service.PageAttachmentService;
import io.choerodon.kb.infra.common.enums.PageResourceType;

/**
 * Created by Zenger on 2019/4/30.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/page_attachment")
public class PageAttachmentProjectController {

    private PageAttachmentService pageAttachmentService;

    public PageAttachmentProjectController(PageAttachmentService pageAttachmentService) {
        this.pageAttachmentService = pageAttachmentService;
    }

    /**
     * 页面上传附件
     *
     * @param projectId 项目id
     * @param pageId    页面id
     * @param versionId 页面版本ID
     * @param request   文件信息
     * @return List<PageAttachmentDTO>
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation("页面上传附件")
    @PostMapping
    public ResponseEntity<List<PageAttachmentDTO>> create(@ApiParam(value = "项目ID", required = true)
                                                          @PathVariable(value = "project_id") Long projectId,
                                                          @ApiParam(value = "页面ID", required = true)
                                                          @RequestParam Long pageId,
                                                          @ApiParam(value = "页面版本ID", required = true)
                                                          @RequestParam Long versionId,
                                                          HttpServletRequest request) {
        return new ResponseEntity<>(pageAttachmentService.create(projectId,
                PageResourceType.PROJECT.getResourceType(),
                pageId,
                versionId,
                request), HttpStatus.CREATED);
    }

    /**
     * 页面删除附件
     *
     * @param projectId 项目id
     * @param id        附件id
     * @return ResponseEntity
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation("页面删除附件")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity delete(@ApiParam(value = "项目ID", required = true)
                                 @PathVariable(value = "project_id") Long projectId,
                                 @ApiParam(value = "附件ID", required = true)
                                 @PathVariable Long id) {
        pageAttachmentService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 上传附件，直接返回地址
     *
     * @param projectId 项目ID
     * @param request
     * @return List<String>
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation("上传附件，直接返回地址")
    @PostMapping(value = "/upload_for_address")
    public ResponseEntity<List<String>> uploadForAddress(@ApiParam(value = "项目ID", required = true)
                                                         @PathVariable(value = "project_id") Long projectId,
                                                         HttpServletRequest request) {
        return new ResponseEntity<>(pageAttachmentService.uploadForAddress(projectId,
                PageResourceType.PROJECT.getResourceType(),
                request), HttpStatus.CREATED);
    }
}
