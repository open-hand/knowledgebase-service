package io.choerodon.kb.api.controller.v1;

import java.util.List;
import java.util.Optional;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.Permission;
import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.vo.*;
import io.choerodon.kb.app.service.KnowledgeBaseService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                                 @RequestBody  @Encrypt KnowledgeBaseInfoVO knowledgeBaseInfoVO) {

        return new ResponseEntity(knowledgeBaseService.create(organizationId,projectId,knowledgeBaseInfoVO), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("修改知识库配置")
    @PutMapping(value = "/update")
    public ResponseEntity<KnowledgeBaseInfoVO> updateKnowledgeBase(@ApiParam(value = "项目id", required = true)
                                                      @PathVariable(value = "project_id") Long projectId,
                                                      @ApiParam(value = "组织id", required = true)
                                                      @RequestParam Long organizationId,
                                                      @RequestBody @Encrypt KnowledgeBaseInfoVO knowledgeBaseInfoVO) {

        return new ResponseEntity(knowledgeBaseService.update(organizationId,projectId,knowledgeBaseInfoVO), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("移除项目下知识库到回收站（移除自己的知识库）")
    @PutMapping(value = "/remove_my/{base_id}")
    public ResponseEntity removeKnowledgeBase(@ApiParam(value = "项目id", required = true)
                                                                   @PathVariable(value = "project_id") Long projectId,
                                                                   @ApiParam(value = "组织id", required = true)
                                                                   @RequestParam Long organizationId,
                                                                   @ApiParam(value = "知识库Id", required = true)
                                                                   @PathVariable(value = "base_id") @Encrypt Long baseId) {
        knowledgeBaseService.removeKnowledgeBase(organizationId,projectId,baseId);
        return new ResponseEntity( HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("查询所有知识库")
    @GetMapping(value = "/query/list")
    public ResponseEntity<List<List<KnowledgeBaseListVO>>> queryKnowledgeBase(@ApiParam(value = "项目id", required = true)
                                               @PathVariable(value = "project_id") Long projectId,
                                                                        @ApiParam(value = "组织id", required = true)
                                               @RequestParam Long organizationId) {

        return Optional.ofNullable(knowledgeBaseService.queryKnowledgeBaseWithRecent(organizationId,projectId))
                .map(result->new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.query.knowledge"));

    }


}
