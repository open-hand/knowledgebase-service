package io.choerodon.kb.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.app.service.KnowledgeBaseTemplateService;
import io.choerodon.swagger.annotation.Permission;

import org.hzero.core.util.Results;

/**
 * @author zhaotianxin
 * @date 2021/11/23 09:56
 */
@RestController
@RequestMapping(value = "/v1")
public class KnowledgeBaseTemplateController {
    @Autowired
    private KnowledgeBaseTemplateService workSpaceTemplateService;

    @Permission(level = ResourceLevel.SITE)
    @ApiOperation("初始化系统预置知识模版")
    @GetMapping("/knowledge_base_template/init")
    public ResponseEntity<Void> initWorkSpaceTemplate() {
        workSpaceTemplateService.initWorkSpaceTemplate();
        return Results.success();
    }
}
