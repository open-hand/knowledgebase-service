package io.choerodon.kb.api.controller.v1;

import io.choerodon.swagger.annotation.Permission;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.api.vo.FullTextSearchResultVO;
import io.choerodon.kb.api.vo.PageAutoSaveVO;
import io.choerodon.kb.api.vo.PageCreateVO;
import io.choerodon.kb.api.vo.WorkSpaceInfoVO;
import io.choerodon.kb.app.service.PageService;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.dto.PageContentDTO;
import io.choerodon.kb.infra.utils.EsRestUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.hzero.starter.keyencrypt.core.IEncryptionService;
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
@RequestMapping(value = "/v1/projects/{project_id}/page")
public class PageProjectController {

    private PageService pageService;
    private EsRestUtil esRestUtil;
    private IEncryptionService encryptionService;

    public PageProjectController(PageService pageService, EsRestUtil esRestUtil, IEncryptionService encryptionService) {
        this.pageService = pageService;
        this.esRestUtil = esRestUtil;
        this.encryptionService = encryptionService;
    }

    @ResponseBody
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("导出文章为pdf")
    @GetMapping(value = "/export_pdf")
    public void exportMd2Pdf(@ApiParam(value = "项目id", required = true)
                             @PathVariable(value = "project_id") Long projectId,
                             @ApiParam(value = "组织id", required = true)
                             @RequestParam Long organizationId,
                             @ApiParam(value = "页面id", required = true)
                             @RequestParam @Encrypt Long pageId,
                             HttpServletResponse response) {
        pageService.exportMd2Pdf(organizationId, projectId, pageId, response);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("导入word文档为markdown数据（目前只支持docx）")
    @PostMapping(value = "/import_word")
    public ResponseEntity<String> importDocx2Md(@ApiParam(value = "项目id", required = true)
                                                @PathVariable(value = "project_id") Long projectId,
                                                @ApiParam(value = "组织id", required = true)
                                                @RequestParam Long organizationId,
                                                @ApiParam(value = "word文档", required = true)
                                                @RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(pageService.importDocx2Md(organizationId, projectId, file), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("创建页面（带有内容）")
    @PostMapping
    public ResponseEntity<WorkSpaceInfoVO> createPageByImport(@ApiParam(value = "项目id", required = true)
                                                              @PathVariable(value = "project_id") Long projectId,
                                                              @ApiParam(value = "组织id", required = true)
                                                              @RequestParam Long organizationId,
                                                              @ApiParam(value = "创建对象", required = true)
                                                              @RequestBody @Encrypt PageCreateVO create) {
        return new ResponseEntity<>(pageService.createPageWithContent(organizationId, projectId, create), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("文章自动保存")
    @PutMapping(value = "/auto_save")
    public ResponseEntity autoSavePage(@ApiParam(value = "项目id", required = true)
                                       @PathVariable(value = "project_id") Long projectId,
                                       @ApiParam(value = "组织id", required = true)
                                       @RequestParam Long organizationId,
                                       @ApiParam(value = "页面id", required = true)
                                       @RequestParam @Encrypt Long pageId,
                                       @ApiParam(value = "草稿对象", required = true)
                                       @RequestBody PageAutoSaveVO autoSave) {
        pageService.autoSavePage(organizationId, projectId, pageId, autoSave);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("页面恢复草稿")
    @GetMapping(value = "/draft_page")
    public ResponseEntity<String> queryDraftPage(@ApiParam(value = "项目id", required = true)
                                                 @PathVariable(value = "project_id") Long projectId,
                                                 @ApiParam(value = "组织id", required = true)
                                                 @RequestParam Long organizationId,
                                                 @ApiParam(value = "页面id", required = true)
                                                 @RequestParam @Encrypt Long pageId) {
        PageContentDTO contentDO = pageService.queryDraftContent(organizationId, projectId, pageId);
        return new ResponseEntity<>(contentDO != null ? contentDO.getContent() : null, HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("删除草稿")
    @DeleteMapping(value = "/delete_draft")
    public ResponseEntity deleteDraftContent(@ApiParam(value = "项目id", required = true)
                                             @PathVariable(value = "project_id") Long projectId,
                                             @ApiParam(value = "组织id", required = true)
                                             @RequestParam Long organizationId,
                                             @ApiParam(value = "页面id", required = true)
                                             @RequestParam @Encrypt Long pageId) {
        pageService.deleteDraftContent(organizationId, projectId, pageId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("全文搜索")
    @GetMapping(value = "/full_text_search")
    public ResponseEntity<List<FullTextSearchResultVO>> fullTextSearch(@ApiParam(value = "项目id", required = true)
                                                                       @PathVariable(value = "project_id") Long projectId,
                                                                       @ApiParam(value = "组织id", required = true)
                                                                       @RequestParam Long organizationId,
                                                                       @RequestParam @Encrypt Long baseId,
                                                                       @ApiParam(value = "搜索内容", required = true)
                                                                       @RequestParam String searchStr) {
        return new ResponseEntity<>(esRestUtil.fullTextSearch(organizationId, projectId, BaseStage.ES_PAGE_INDEX, searchStr,baseId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("创建页面（可以选择按模板）")
    @PostMapping("/with_template")
    public ResponseEntity<WorkSpaceInfoVO> createPageByTemplate(@ApiParam(value = "项目id", required = true)
                                                              @PathVariable(value = "project_id") Long projectId,
                                                              @ApiParam(value = "组织id", required = true)
                                                              @RequestParam Long organizationId,
                                                                @ApiParam(value = "模板id", required = true)
                                                              @RequestParam @Encrypt Long templateId,
                                                              @ApiParam(value = "创建对象", required = true)
                                                              @RequestBody @Encrypt PageCreateVO create) {
        return new ResponseEntity<>(pageService.createPageByTemplate(organizationId, projectId, create,templateId), HttpStatus.OK);
    }

}
