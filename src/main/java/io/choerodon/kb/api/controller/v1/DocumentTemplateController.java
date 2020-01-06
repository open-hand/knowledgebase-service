package io.choerodon.kb.api.controller.v1;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import com.github.pagehelper.PageInfo;
import io.choerodon.core.annotation.Permission;
import io.choerodon.core.enums.ResourceType;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.kb.api.vo.*;
import io.choerodon.kb.app.service.DocumentTemplateService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * @author zhaotianxin
 * @since 2020/1/2
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/document_template")
public class DocumentTemplateController {
    @Autowired
    private DocumentTemplateService documentTemplateService;

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation("创建模板文件")
    @PostMapping(value = "/create")
    public ResponseEntity<DocumentTemplateInfoVO> create(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "组织id", required = true)
            @RequestParam Long organizationId,
            @ApiParam(value = "页面信息", required = true)
            @RequestBody @Valid PageCreateWithoutContentVO pageCreateVO){
        return new ResponseEntity<>(documentTemplateService.createTemplate(projectId,0L,pageCreateVO), HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "更新文档模板")
    @PutMapping(value = "/{id}")
    public ResponseEntity<WorkSpaceInfoVO> updateTemplate(@ApiParam(value = "项目id", required = true)
                                                                  @PathVariable(value = "project_id") Long projectId,
                                                                  @ApiParam(value = "组织id", required = true)
                                                                  @RequestParam Long organizationId,
                                                                  @ApiParam(value = "工作空间目录id", required = true)
                                                                  @PathVariable Long id,
                                                                  @ApiParam(value = "应用于全文检索时，对单篇文章，根据检索内容高亮内容")
                                                                  @RequestParam(required = false) String searchStr,
                                                                  @ApiParam(value = "空间信息", required = true)
                                                                  @RequestBody @Valid PageUpdateVO pageUpdateVO) {
        return new ResponseEntity<>(documentTemplateService.updateTemplate(0L, projectId, id, searchStr, pageUpdateVO), HttpStatus.CREATED);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询模板列表")
    @PostMapping(value = "/template_list")
    public ResponseEntity<PageInfo<DocumentTemplateInfoVO>> listTemplate(@ApiParam(value = "项目id", required = true)
                                                                         @PathVariable(value = "project_id") Long projectId,
                                                                         @ApiParam(value = "组织id", required = true)
                                                                     @RequestParam Long organizationId,
                                                                         @RequestParam Long baseId,
                                                                         @SortDefault Pageable pageable,
                                                                         @RequestBody(required = false) SearchVO searchVO) {
        return new ResponseEntity<>(documentTemplateService.listTemplate(0L,projectId,baseId,pageable,searchVO), HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,InitRoleCode.PROJECT_MEMBER})
    @ApiOperation("查询知识库模板")
    @PostMapping(value = "/list_system_template")
    public ResponseEntity<List<KnowledgeBaseTreeVO>> listSystemTemplate(@ApiParam(value = "项目id", required = true)
                                                                  @PathVariable(value = "project_id") Long projectId,
                                                                  @ApiParam(value = "组织id", required = true)
                                                                  @RequestParam Long organizationId,
                                                                  @RequestBody(required = false) SearchVO searchVO) {
        return new ResponseEntity<>(documentTemplateService.listSystemTemplate(organizationId,projectId,searchVO),HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation("模板页面上传附件")
    @PostMapping
    public ResponseEntity<List<PageAttachmentVO>> create(@ApiParam(value = "项目ID", required = true)
                                                         @PathVariable(value = "project_id") Long projectId,
                                                         @ApiParam(value = "组织id", required = true)
                                                         @RequestParam Long organizationId,
                                                         @ApiParam(value = "页面ID", required = true)
                                                         @RequestParam Long pageId,
                                                         HttpServletRequest request) {
        return new ResponseEntity<>(documentTemplateService.createAttachment(0L, projectId, pageId, ((MultipartHttpServletRequest) request).getFiles("file")), HttpStatus.CREATED);
    }
}
