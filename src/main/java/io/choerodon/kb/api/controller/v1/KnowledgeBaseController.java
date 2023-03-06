package io.choerodon.kb.api.controller.v1;

import java.util.List;
import java.util.Optional;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.api.vo.KnowledgeBaseInfoVO;
import io.choerodon.kb.api.vo.KnowledgeBaseListVO;
import io.choerodon.kb.app.service.KnowledgeBaseService;
import io.choerodon.swagger.annotation.Permission;

import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author zhaotianxin
 * @since 2019/12/30
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/knowledge_base")
public class KnowledgeBaseController {

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("创建知识库")
    @PostMapping(value = "/create")
    public ResponseEntity<KnowledgeBaseInfoVO> createKnowledgeBase(@ApiParam(value = "项目id", required = true)
                                                                   @PathVariable(value = "project_id") Long projectId,
                                                                   @ApiParam(value = "组织id", required = true)
                                                                   @RequestParam Long organizationId,
                                                                   @ApiParam(value = "创建vo", required = true)
                                                                   @RequestBody @Encrypt KnowledgeBaseInfoVO knowledgeBaseInfoVO) {

        return Results.success(knowledgeBaseService.create(organizationId, projectId, knowledgeBaseInfoVO, false));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("修改知识库配置")
    @PutMapping(value = "/update")
    public ResponseEntity<KnowledgeBaseInfoVO> updateKnowledgeBase(@ApiParam(value = "项目id", required = true)
                                                                   @PathVariable(value = "project_id") Long projectId,
                                                                   @ApiParam(value = "组织id", required = true)
                                                                   @RequestParam Long organizationId,
                                                                   @ApiParam(value = "更新vo", required = true)
                                                                   @RequestBody @Encrypt KnowledgeBaseInfoVO knowledgeBaseInfoVO) {

        return Results.success(knowledgeBaseService.update(organizationId, projectId, knowledgeBaseInfoVO));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("移除项目下知识库到回收站（移除自己的知识库）")
    @PutMapping(value = "/remove_my/{base_id}")
    public ResponseEntity<Void> removeKnowledgeBase(@ApiParam(value = "项目id", required = true)
                                              @PathVariable(value = "project_id") Long projectId,
                                              @ApiParam(value = "组织id", required = true)
                                              @RequestParam Long organizationId,
                                              @ApiParam(value = "知识库Id", required = true)
                                              @PathVariable(value = "base_id") @Encrypt Long baseId) {
        knowledgeBaseService.removeKnowledgeBase(organizationId, projectId, baseId);
        return Results.success();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("查询所有知识库")
    @GetMapping(value = "/query/list")
    public ResponseEntity<List<List<KnowledgeBaseListVO>>> queryKnowledgeBase(@ApiParam(value = "项目id", required = true)
                                                                              @PathVariable(value = "project_id") Long projectId,
                                                                              @ApiParam(value = "组织id", required = true)
                                                                              @RequestParam Long organizationId) {

        return Optional.ofNullable(knowledgeBaseService.queryKnowledgeBaseWithRecent(organizationId, projectId, false))
                .map(Results::success)
                .orElseThrow(() -> new CommonException("error.queryOrganizationById.knowledge"));

    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("查询创建的知识库是否初始化成功")
    @GetMapping(value = "/{id}/init-completed")
    public ResponseEntity<Boolean> queryInitCompleted(@ApiParam(value = "组织ID", required = true)
                                                      @PathVariable(value = "project_id") Long projectId,
                                                      @PathVariable(value = "id") Long id) {
        return Results.success(knowledgeBaseService.queryInitCompleted(id));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("基于模版创建文档")
    @PostMapping(value = "/{id}/create/base-template")
    public ResponseEntity createBaseTemplate(@ApiParam(value = "组织ID", required = true)
                                             @PathVariable(value = "project_id") Long projectId,
                                             @PathVariable(value = "id") Long id,
                                             @RequestParam Long organizationId,
                                             @RequestBody KnowledgeBaseInfoVO knowledgeBaseInfoVO) {
        knowledgeBaseService.createBaseTemplate(organizationId, projectId, id, knowledgeBaseInfoVO);
        return Results.success();
    }


}
