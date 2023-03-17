package io.choerodon.kb.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.api.vo.KnowledgeBaseListVO;
import io.choerodon.kb.app.service.KnowledgeBaseService;
import io.choerodon.swagger.annotation.Permission;

import org.hzero.core.util.Results;

@RestController
@RequestMapping("/v1/projects/{project_id}/knowledge_base/template")
public class KnowledgeBaseProjectTemplateController {

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;


    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "项目下下查询场景化模板")
    @GetMapping("/query/list")
    public ResponseEntity<List<List<KnowledgeBaseListVO>>> pageKnowledgeBase(@ApiParam(value = "组织id", required = true)
                                                                             @RequestParam Long organizationId,
                                                                             @ApiParam(value = "组织ID", required = true)
                                                                             @PathVariable(value = "project_id") Long projectId,
                                                                             @RequestParam(required = false) String params) {

        return Results.success(knowledgeBaseService.queryKnowledgeBaseWithRecent(organizationId, projectId, true,null, params));

    }

}
