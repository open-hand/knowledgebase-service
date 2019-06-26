package io.choerodon.kb.api.controller.v1;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.kb.api.dao.PageAutoSaveDTO;
import io.choerodon.kb.api.dao.PageCreateDTO;
import io.choerodon.kb.api.dao.PageDTO;
import io.choerodon.kb.app.service.PageService;
import io.choerodon.kb.infra.common.enums.PageResourceType;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by Zenger on 2019/4/30.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/page")
public class PageProjectController {

    private PageService pageService;

    public PageProjectController(PageService pageService) {
        this.pageService = pageService;
    }

    /**
     * 校验是否为页面的创建者
     *
     * @param projectId 项目id
     * @param id        页面id
     * @return Boolean
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "校验是否为页面的创建者")
    @GetMapping(value = "/{id}")
    public ResponseEntity<Boolean> checkPageCreate(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "页面ID", required = true)
            @PathVariable Long id) {
        return new ResponseEntity<>(pageService.checkPageCreate(id), HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "获取文章标题")
    @GetMapping(value = "/{id}/toc")
    public ResponseEntity<String> pageToc(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "页面ID", required = true)
            @PathVariable Long id) {
        return new ResponseEntity<>(pageService.pageToc(id), HttpStatus.OK);
    }

    @ResponseBody
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation("导出文章为pdf")
    @GetMapping(value = "/export_pdf")
    public void exportMd2Pdf(@ApiParam(value = "项目id", required = true)
                             @PathVariable(value = "project_id") Long projectId,
                             @ApiParam(value = "组织id", required = true)
                             @RequestParam Long organizationId,
                             @ApiParam(value = "页面id", required = true)
                             @RequestParam Long pageId,
                             HttpServletResponse response) {
        pageService.exportMd2Pdf(organizationId, projectId, pageId, response);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation("导入word文档为markdown数据（目前只支持docx）")
    @PostMapping(value = "/import_word")
    public ResponseEntity<String> importDocx2Md(@ApiParam(value = "项目id", required = true)
                                                @PathVariable(value = "project_id") Long projectId,
                                                @ApiParam(value = "组织id", required = true)
                                                @RequestParam Long organizationId,
                                                @ApiParam(value = "word文档", required = true)
                                                @RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(pageService.importDocx2Md(organizationId, projectId, file, PageResourceType.PROJECT.getResourceType()), HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation("创建页面")
    @PostMapping
    public ResponseEntity<PageDTO> createPage(@ApiParam(value = "项目id", required = true)
                                              @PathVariable(value = "project_id") Long projectId,
                                              @ApiParam(value = "组织id", required = true)
                                              @RequestParam Long organizationId,
                                              @ApiParam(value = "创建对象", required = true)
                                              @RequestBody PageCreateDTO create) {
        return new ResponseEntity<>(pageService.createPage(projectId, create, PageResourceType.PROJECT.getResourceType()), HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation("文章自动保存")
    @PutMapping(value = "/auto_save")
    public ResponseEntity autoSavePage(@ApiParam(value = "项目id", required = true)
                                       @PathVariable(value = "project_id") Long projectId,
                                       @ApiParam(value = "组织id", required = true)
                                       @RequestParam Long organizationId,
                                       @ApiParam(value = "页面id", required = true)
                                       @RequestParam Long pageId,
                                       @ApiParam(value = "草稿对象", required = true)
                                       @RequestBody PageAutoSaveDTO autoSave) {
        pageService.autoSavePage(organizationId, projectId, pageId, autoSave);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
