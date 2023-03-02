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
    @ApiOperation(value = "组织下查询场景化模板")
    @GetMapping("/query/list")
    public ResponseEntity<List<List<KnowledgeBaseListVO>>> pageKnowledgeBase(@ApiParam(value = "组织ID", required = true)
                                                                              @PathVariable(value = "organization_id") Long organizationId) {

        return Optional.ofNullable(knowledgeBaseService.queryKnowledgeBaseWithRecent(organizationId, null,true))
                .map(Results::success)
                .orElseThrow(() -> new CommonException("error.queryOrganizationById.knowledge"));

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
