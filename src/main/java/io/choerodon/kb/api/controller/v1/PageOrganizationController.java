package io.choerodon.kb.api.controller.v1;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.kb.api.dao.FullTextSearchResultDTO;
import io.choerodon.kb.api.dao.PageAutoSaveDTO;
import io.choerodon.kb.api.dao.PageCreateDTO;
import io.choerodon.kb.api.dao.PageDTO;
import io.choerodon.kb.app.service.PageService;
import io.choerodon.kb.infra.common.enums.PageResourceType;
import io.choerodon.kb.infra.common.utils.EsRestUtil;
import io.choerodon.kb.infra.dataobject.PageContentDO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Created by Zenger on 2019/4/30.
 */
@RestController
@RequestMapping(value = "/v1/organizations/{organization_id}/page")
public class PageOrganizationController {

    private PageService pageService;
    private EsRestUtil esRestUtil;

    public PageOrganizationController(PageService pageService, EsRestUtil esRestUtil) {
        this.pageService = pageService;
        this.esRestUtil = esRestUtil;
    }

    /**
     * 校验是否为页面的创建者
     *
     * @param organizationId 组织id
     * @param id             页面id
     * @return Boolean
     */
    @Permission(type = ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR,
                    InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation(value = "校验是否为页面的创建者")
    @GetMapping(value = "/{id}")
    public ResponseEntity<Boolean> checkPageCreate(
            @ApiParam(value = "组织ID", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "页面ID", required = true)
            @PathVariable Long id) {
        return new ResponseEntity<>(pageService.checkPageCreate(id), HttpStatus.OK);
    }

    @ResponseBody
    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR, InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation("导出文章为pdf")
    @GetMapping(value = "/export_pdf")
    public void exportMd2Pdf(@ApiParam(value = "组织id", required = true)
                             @PathVariable(value = "organization_id") Long organizationId,
                             @ApiParam(value = "页面id", required = true)
                             @RequestParam Long pageId,
                             HttpServletResponse response) {
        pageService.exportMd2Pdf(organizationId, null, pageId, response);
    }

    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR, InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation("导入word文档为markdown数据")
    @PostMapping(value = "/import_word")
    public ResponseEntity<String> importDocx2Md(@ApiParam(value = "组织id", required = true)
                                                @PathVariable(value = "organization_id") Long organizationId,
                                                @ApiParam(value = "word文档", required = true)
                                                @RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(pageService.importDocx2Md(organizationId, null, file, PageResourceType.PROJECT.getResourceType()), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR, InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation("创建页面")
    @PostMapping
    public ResponseEntity<PageDTO> createPage(@ApiParam(value = "组织id", required = true)
                                              @PathVariable(value = "organization_id") Long organizationId,
                                              @ApiParam(value = "创建对象", required = true)
                                              @RequestBody PageCreateDTO create) {
        return new ResponseEntity<>(pageService.createPage(organizationId, create, PageResourceType.ORGANIZATION.getResourceType()), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR, InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation("文章自动保存")
    @PutMapping(value = "/auto_save")
    public ResponseEntity autoSavePage(@ApiParam(value = "组织id", required = true)
                                       @PathVariable(value = "organization_id") Long organizationId,
                                       @ApiParam(value = "页面id", required = true)
                                       @RequestParam Long pageId,
                                       @ApiParam(value = "草稿对象", required = true)
                                       @RequestBody PageAutoSaveDTO autoSave) {
        pageService.autoSavePage(organizationId, null, pageId, autoSave);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR, InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation("页面恢复草稿")
    @GetMapping(value = "/draft_page")
    public ResponseEntity<String> queryDraftPage(@ApiParam(value = "组织id", required = true)
                                                 @PathVariable(value = "organization_id") Long organizationId,
                                                 @ApiParam(value = "页面id", required = true)
                                                 @RequestParam Long pageId) {
        PageContentDO contentDO = pageService.queryDraftContent(organizationId, null, pageId);
        return new ResponseEntity<>(contentDO != null ? contentDO.getContent() : null, HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR, InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation("删除草稿")
    @DeleteMapping(value = "/delete_draft")
    public ResponseEntity deleteDraftContent(@ApiParam(value = "组织id", required = true)
                                             @PathVariable(value = "organization_id") Long organizationId,
                                             @ApiParam(value = "页面id", required = true)
                                             @RequestParam Long pageId) {
        pageService.deleteDraftContent(organizationId, null, pageId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR, InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation("全文搜索")
    @GetMapping(value = "/full_text_search")
    public ResponseEntity<List<FullTextSearchResultDTO>> fullTextSearch(@ApiParam(value = "组织id", required = true)
                                                                        @PathVariable(value = "organization_id") Long organizationId,
                                                                        @ApiParam(value = "搜索内容", required = true)
                                                                        @RequestParam String searchStr) {
        return new ResponseEntity<>(esRestUtil.fullTextSearch(organizationId, null, searchStr), HttpStatus.OK);
    }
}
