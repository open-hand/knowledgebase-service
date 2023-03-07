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

        return Optional.ofNullable(knowledgeBaseService.queryKnowledgeBaseWithRecent(organizationId, projectId, true, params))
                .map(Results::success)
                .orElseThrow(() -> new CommonException("error.queryOrganizationById.knowledge"));

    }


}
