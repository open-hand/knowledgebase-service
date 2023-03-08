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
import io.choerodon.kb.api.vo.KnowledgeBaseInitProgress;
import io.choerodon.kb.api.vo.KnowledgeBaseListVO;
import io.choerodon.kb.app.service.KnowledgeBaseService;
import io.choerodon.swagger.annotation.Permission;

import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author zhaotianxin
 * @since 2020/1/3
 */
@RestController
@RequestMapping("/v1/organizations/{organization_id}/knowledge_base")
public class KnowledgeBaseOrganizationController {
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("组织下创建知识库")
    @PostMapping(value = "/create")
    public ResponseEntity<KnowledgeBaseInfoVO> createKnowledgeBase(@ApiParam(value = "组织ID", required = true)
                                                                   @PathVariable(value = "organization_id") Long organizationId,
                                                                   @ApiParam(value = "创建vo", required = true)
                                                                   @RequestBody KnowledgeBaseInfoVO knowledgeBaseInfoVO) {

        return Results.success(knowledgeBaseService.create(organizationId, null, knowledgeBaseInfoVO, false));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("组织下修改知识库配置")
    @PutMapping(value = "/update")
    public ResponseEntity<KnowledgeBaseInfoVO> updateKnowledgeBase(@ApiParam(value = "组织ID", required = true)
                                                                   @PathVariable(value = "organization_id") Long organizationId,
                                                                   @ApiParam(value = "更新vo", required = true)
                                                                   @RequestBody KnowledgeBaseInfoVO knowledgeBaseInfoVO) {

        return Results.success(knowledgeBaseService.update(organizationId, null, knowledgeBaseInfoVO));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("组织下移除项目下知识库到回收站（移除自己的知识库）")
    @PutMapping(value = "/remove_my/{base_id}")
    public ResponseEntity<Void> removeKnowledgeBase(@ApiParam(value = "组织ID", required = true)
                                                    @PathVariable(value = "organization_id") Long organizationId,
                                                    @ApiParam(value = "知识库Id", required = true)
                                                    @PathVariable(value = "base_id") @Encrypt Long baseId) {
        knowledgeBaseService.removeKnowledgeBase(organizationId, null, baseId);
        return Results.success();
    }


    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("组织下查询所有知识库")
    @GetMapping(value = "/query/list")
    public ResponseEntity<List<List<KnowledgeBaseListVO>>> queryKnowledgeBase(@ApiParam(value = "组织ID", required = true)
                                                                              @PathVariable(value = "organization_id") Long organizationId) {

        return Optional.ofNullable(knowledgeBaseService.queryKnowledgeBaseWithRecent(organizationId, null, false, null))
                .map(Results::success)
                .orElseThrow(() -> new CommonException("error.queryOrganizationById.knowledge"));

    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("查询创建的知识库是否初始化成功")
    @GetMapping(value = "/{id}/init-completed")
    public ResponseEntity<Boolean> queryInitCompleted(@ApiParam(value = "组织ID", required = true)
                                                      @PathVariable(value = "organization_id") Long organizationId,
                                                      @PathVariable(value = "id") @Encrypt Long id) {
        return Results.success(knowledgeBaseService.queryInitCompleted(id));
    }


    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("根据uuid从redis查询进度")
    @GetMapping(value = "/uuid/{uuid}")
    public ResponseEntity<KnowledgeBaseInitProgress> queryProgressByUuid(@ApiParam(value = "组织ID", required = true)
                                                                         @PathVariable(value = "organization_id") Long organizationId,
                                                                         @PathVariable(value = "uuid") String uuid) {
        return Results.success(knowledgeBaseService.queryProgressByUuid(uuid));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("查询知识库是否是模板")
    @GetMapping(value = "/{id}")
    public ResponseEntity<KnowledgeBaseInfoVO> queryKnowledgeBaseById(@ApiParam(value = "组织ID", required = true)
                                                                      @PathVariable(value = "organization_id") Long organizationId,
                                                                      @ApiParam(value = "base是预置的还是自建的")
                                                                      @RequestParam(required = false) String category,
                                                                      @PathVariable(value = "id") @Encrypt Long id) {
        return Results.success(knowledgeBaseService.queryKnowledgeBaseById(organizationId, null, id, category));
    }


    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("基于模版创建文档")
    @PostMapping(value = "/{id}/create/base-template")
    public ResponseEntity createBaseTemplate(@ApiParam(value = "组织ID", required = true)
                                             @PathVariable(value = "organization_id") Long organizationId,
                                             @PathVariable(value = "id") @Encrypt Long id,
                                             @RequestBody KnowledgeBaseInfoVO knowledgeBaseInfoVO) {
        knowledgeBaseService.createBaseTemplate(organizationId, null, id, knowledgeBaseInfoVO);
        return Results.success();
    }

}
