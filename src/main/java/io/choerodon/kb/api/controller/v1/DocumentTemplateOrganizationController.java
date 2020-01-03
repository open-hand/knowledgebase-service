package io.choerodon.kb.api.controller.v1;

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

/**
 * @author zhaotianxin
 * @since 2020/1/3
 */
@RestController
@RequestMapping("/v1/organizations/{organization_id}/document_template")
public class DocumentTemplateOrganizationController {
    @Autowired
    private DocumentTemplateService documentTemplateService;

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation("创建模板文件")
    @PostMapping(value = "/create")
    public ResponseEntity<DocumentTemplateInfoVO> create(
            @ApiParam(value = "组织ID", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "页面信息", required = true)
            @RequestBody @Valid PageCreateWithoutContentVO pageCreateVO){
        return new ResponseEntity<>(documentTemplateService.createTemplate(0L,organizationId,pageCreateVO), HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "更新文档模板")
    @PutMapping(value = "/{id}")
    public ResponseEntity<WorkSpaceInfoVO> updateTemplate(@ApiParam(value = "组织ID", required = true)
                                                          @PathVariable(value = "organization_id") Long organizationId,
                                                          @ApiParam(value = "工作空间目录id", required = true)
                                                          @PathVariable Long id,
                                                          @ApiParam(value = "应用于全文检索时，对单篇文章，根据检索内容高亮内容")
                                                          @RequestParam(required = false) String searchStr,
                                                          @ApiParam(value = "空间信息", required = true)
                                                          @RequestBody @Valid PageUpdateVO pageUpdateVO) {
        return new ResponseEntity<>(documentTemplateService.updateTemplate(organizationId, 0L, id, searchStr, pageUpdateVO), HttpStatus.CREATED);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询模板列表")
    @PostMapping(value = "/template_list")
    public ResponseEntity<PageInfo<DocumentTemplateInfoVO>> listTemplate(@ApiParam(value = "组织ID", required = true)
                                                                         @PathVariable(value = "organization_id") Long organizationId,
                                                                         @RequestParam Long baseId,
                                                                         @SortDefault Pageable pageable,
                                                                         @RequestBody(required = false) SearchVO searchVO) {
        return new ResponseEntity<>(documentTemplateService.listTemplate(organizationId,0L,baseId,pageable,searchVO), HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation("查询知识库模板")
    @PostMapping(value = "/list_system_template")
    public ResponseEntity<List<KnowledgeBaseTreeVO>> listTemplate(@ApiParam(value = "组织ID", required = true)
                                                                  @PathVariable(value = "organization_id") Long organizationId,
                                                                  @ApiParam(value = "项目Id", required = true)
                                                                  @RequestParam Long projectId,
                                                                  @RequestBody(required = false) SearchVO searchVO) {
        return new ResponseEntity<>(documentTemplateService.listSystemTemplate(organizationId,projectId,searchVO),HttpStatus.OK);
    }
}
