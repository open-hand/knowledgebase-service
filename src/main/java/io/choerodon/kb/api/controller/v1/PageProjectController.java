package io.choerodon.kb.api.controller.v1;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.api.vo.FullTextSearchResultVO;
import io.choerodon.kb.api.vo.PageAutoSaveVO;
import io.choerodon.kb.api.vo.PageCreateVO;
import io.choerodon.kb.api.vo.WorkSpaceInfoVO;
import io.choerodon.kb.app.service.PageService;
import io.choerodon.kb.domain.repository.PageRepository;
import io.choerodon.kb.infra.dto.PageContentDTO;
import io.choerodon.kb.infra.enums.WorkSpaceType;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.Permission;

import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Created by Zenger on 2019/4/30.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/page")
public class PageProjectController {

    private final PageRepository pageRepository;
    private final PageService pageService;

    public PageProjectController(PageRepository pageRepository, PageService pageService) {
        this.pageRepository = pageRepository;
        this.pageService = pageService;
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
                                                @ApiParam(value = "知识库id, 用于鉴权", required = true)
                                                @RequestParam @Encrypt Long baseId,
                                                @ApiParam(value = "父工作空间id, 用于鉴权", required = true)
                                                @RequestParam @Encrypt(ignoreValue = "0") Long parentWorkSpaceId,
                                                @ApiParam(value = "word文档", required = true)
                                                @RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(pageService.importDocx2Md(organizationId, projectId, baseId, parentWorkSpaceId, file, false), HttpStatus.OK);
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
        create.setTemplateFlag(false);
        create.setType(WorkSpaceType.DOCUMENT.getValue());
        return new ResponseEntity<>(pageService.createPageWithContent(organizationId, projectId, create, false), HttpStatus.OK);
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
        PageContentDTO contentDO = pageRepository.queryDraftContent(organizationId, projectId, pageId);
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
                                                                       @ApiParam(value = "知识库id", required = true)
                                                                       @RequestParam @Encrypt Long baseId,
                                                                       @ApiParam(value = "搜索内容", required = true)
                                                                       @RequestParam String searchStr,
                                                                       @ApiIgnore
                                                                       @ApiParam(value = "分页信息", required = true)
                                                                               PageRequest pageRequest) {
        List<FullTextSearchResultVO> fullTextSearchResultVOS = pageService.fullTextSearch(pageRequest, organizationId, projectId, baseId, searchStr);
        return Results.success(fullTextSearchResultVOS);
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
        create.setTemplateFlag(false);
        return new ResponseEntity<>(pageService.createPageByTemplate(organizationId, projectId, create, templateId), HttpStatus.OK);
    }

}
