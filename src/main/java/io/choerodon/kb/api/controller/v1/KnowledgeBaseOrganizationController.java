package io.choerodon.kb.api.controller.v1;

import java.util.List;
import java.util.Optional;
import io.choerodon.core.annotation.Permission;
import io.choerodon.core.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.kb.api.vo.KnowledgeBaseInfoVO;
import io.choerodon.kb.api.vo.KnowledgeBaseListVO;
import io.choerodon.kb.app.service.KnowledgeBaseService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author zhaotianxin
 * @since 2020/1/3
 */
@RestController
@RequestMapping("/v1/organizations/{organization_id}/knowledge_base")
public class KnowledgeBaseOrganizationController {
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR, InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation("组织下创建知识库")
    @PostMapping(value = "/create")
    public ResponseEntity<KnowledgeBaseInfoVO> createKnowledgeBase(@ApiParam(value = "组织ID", required = true)
                                                                   @PathVariable(value = "organization_id") Long organizationId,
                                                                   @RequestBody KnowledgeBaseInfoVO knowledgeBaseInfoVO) {

        return new ResponseEntity(knowledgeBaseService.create(organizationId,null,knowledgeBaseInfoVO), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR, InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation("组织下修改知识库配置")
    @PutMapping(value = "/update")
    public ResponseEntity<KnowledgeBaseInfoVO> updateKnowledgeBase(@ApiParam(value = "组织ID", required = true)
                                                                   @PathVariable(value = "organization_id") Long organizationId,
                                                                   @RequestBody KnowledgeBaseInfoVO knowledgeBaseInfoVO) {

        return new ResponseEntity(knowledgeBaseService.update(organizationId,null,knowledgeBaseInfoVO), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR, InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation("组织下移除项目下知识库到回收站（移除自己的知识库）")
    @PutMapping(value = "/remove_my/{base_id}")
    public ResponseEntity removeKnowledgeBase(@ApiParam(value = "组织ID", required = true)
                                              @PathVariable(value = "organization_id") Long organizationId,
                                              @ApiParam(value = "知识库Id", required = true)
                                              @PathVariable(value = "base_id")Long baseId) {
        knowledgeBaseService.removeKnowledgeBase(organizationId,null,baseId);
        return new ResponseEntity( HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation("组织下删除回收站中的知识库")
    @DeleteMapping(value = "/delete/{base_id}")
    public ResponseEntity deleteKnowledgeBase(@ApiParam(value = "组织ID", required = true)
                                              @PathVariable(value = "organization_id") Long organizationId,
                                              @ApiParam(value = "知识库Id", required = true)
                                              @PathVariable(value = "base_id")Long baseId) {
        knowledgeBaseService.deleteKnowledgeBase(organizationId,null,baseId);
        return new ResponseEntity( HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR, InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation("组织下恢复知识库")
    @PutMapping(value = "/restore/{base_id}")
    public ResponseEntity restoreKnowledgeBase(@ApiParam(value = "组织ID", required = true)
                                               @PathVariable(value = "organization_id") Long organizationId,
                                               @ApiParam(value = "知识库Id", required = true)
                                               @PathVariable(value = "base_id")Long baseId) {
        knowledgeBaseService.restoreKnowledgeBase(organizationId,null,baseId);
        return new ResponseEntity( HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR, InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation("组织下查询所有知识库")
    @GetMapping(value = "/query/list")
    public ResponseEntity<List<KnowledgeBaseListVO>> queryKnowledgeBase(@ApiParam(value = "组织ID", required = true)
                                                                        @PathVariable(value = "organization_id") Long organizationId,
                                                                        @ApiParam(value = "项目id")
                                                                        @RequestParam(required = false) Long projectId) {

        return Optional.ofNullable(knowledgeBaseService.queryKnowledgeBaseWithRecent(organizationId, projectId))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.query.knowledge"));

    }
}
