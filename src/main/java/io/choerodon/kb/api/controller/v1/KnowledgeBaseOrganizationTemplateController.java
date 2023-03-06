package io.choerodon.kb.api.controller.v1;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.api.vo.KnowledgeBaseInfoVO;
import io.choerodon.kb.api.vo.KnowledgeBaseListVO;
import io.choerodon.kb.api.vo.WorkSpaceInfoVO;
import io.choerodon.kb.app.service.KnowledgeBaseService;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/organizations/{organization_id}/knowledge_base/template")
public class KnowledgeBaseOrganizationTemplateController {

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;


    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "组织下创建场景化模板")
    @PostMapping
    public ResponseEntity<KnowledgeBaseInfoVO> createKnowledgeBaseTemplate(@ApiParam(value = "组织ID", required = true)
                                                                           @PathVariable(value = "organization_id") Long organizationId,
                                                                           @ApiParam(value = "创建vo", required = true)
                                                                           @Encrypt @RequestBody KnowledgeBaseInfoVO knowledgeBaseInfoVO) {

        return Results.success(knowledgeBaseService.create(organizationId, null, knowledgeBaseInfoVO, false));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "组织下查询场景化模板")
    @GetMapping("/query/list")
    public ResponseEntity<List<List<KnowledgeBaseListVO>>> queryKnowledgeBaseTemplate(@ApiParam(value = "组织ID", required = true)
                                                                                      @PathVariable(value = "organization_id") Long organizationId,
                                                                                      @RequestParam(required = false) String params) {
        return Optional.ofNullable(knowledgeBaseService.queryKnowledgeBaseWithRecent(organizationId, null, true, params))
                .map(Results::success)
                .orElseThrow(() -> new CommonException("error.queryOrganizationById.knowledge"));

    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("组织层重名场景化模板")
    @PutMapping(value = "/update")
    public ResponseEntity<Void> updateKnowledgeBaseTemplate(@ApiParam(value = "组织ID", required = true)
                                                            @PathVariable(value = "organization_id") Long organizationId,
                                                            @Encrypt @RequestBody KnowledgeBaseInfoVO knowledgeBaseInfoVO) {
        knowledgeBaseService.updateKnowledgeBaseTemplate(organizationId, knowledgeBaseInfoVO);
        return Results.success();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("组织下删除场景化模板到回收站（移除自己的知识库）")
    @PutMapping(value = "/remove_my/{base_id}")
    public ResponseEntity<Void> removeKnowledgeBaseTemplate(@ApiParam(value = "组织ID", required = true)
                                                            @PathVariable(value = "organization_id") Long organizationId,
                                                            @ApiParam(value = "知识库Id", required = true)
                                                            @PathVariable(value = "base_id") @Encrypt Long baseId) {
        knowledgeBaseService.removeKnowledgeBase(organizationId, null, baseId);
        return Results.success();
    }


    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "组织下发布知识库模板")
    @PutMapping("/publish")
    public ResponseEntity<KnowledgeBaseInfoVO> publishKnowledgeBaseTemplate(@ApiParam(value = "组织id", required = true)
                                                                            @PathVariable(value = "organization_id") Long organizationId,
                                                                            @Encrypt @RequestParam(value = "knowledge_base_id") Long knowledgeBaseId) {
        knowledgeBaseService.publishKnowledgeBaseTemplate(organizationId, knowledgeBaseId);
        return Results.success();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "组织下取消发布知识库模板")
    @PutMapping("/un-publish")
    public ResponseEntity<WorkSpaceInfoVO> unPublishKnowledgeBaseTemplate(@ApiParam(value = "组织id", required = true)
                                                                          @PathVariable(value = "organization_id") Long organizationId,
                                                                          @Encrypt @RequestParam(value = "knowledge_base_id") Long knowledgeBaseId) {
        knowledgeBaseService.unPublishKnowledgeBaseTemplate(organizationId, knowledgeBaseId);
        return Results.success();
    }

}
