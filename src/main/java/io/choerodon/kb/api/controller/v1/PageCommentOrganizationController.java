package io.choerodon.kb.api.controller.v1;

import java.util.List;
import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.kb.api.dao.PageCommentDTO;
import io.choerodon.kb.api.dao.PageCommentUpdateDTO;
import io.choerodon.kb.app.service.PageCommentService;

/**
 * Created by Zenger on 2019/4/30.
 */
@RestController
@RequestMapping(value = "/v1/organizations/{organization_id}/page_comment")
public class PageCommentOrganizationController {

    private PageCommentService pageCommentService;

    public PageCommentOrganizationController(PageCommentService pageCommentService) {
        this.pageCommentService = pageCommentService;
    }

    /**
     * 创建page评论
     *
     * @param organizationId       组织id
     * @param pageCommentUpdateDTO 评论信息
     * @return List<PageCommentDTO>
     */
    @Permission(type = ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR,
                    InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation("创建page评论")
    @PostMapping
    public ResponseEntity<PageCommentDTO> create(@ApiParam(value = "组织ID", required = true)
                                                 @PathVariable(value = "organization_id") Long organizationId,
                                                 @ApiParam(value = "评论信息", required = true)
                                                 @RequestBody @Valid PageCommentUpdateDTO pageCommentUpdateDTO) {
        return new ResponseEntity<>(pageCommentService.create(pageCommentUpdateDTO), HttpStatus.CREATED);
    }


    @Permission(type = ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR,
                    InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation(value = " 查询页面评论")
    @GetMapping(value = "/list")
    public ResponseEntity<List<PageCommentDTO>> queryByList(
            @ApiParam(value = "组织id", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "页面id", required = true)
            @RequestParam Long pageId) {
        return new ResponseEntity<>(pageCommentService.queryByList(pageId), HttpStatus.OK);
    }

    /**
     * 更新page评论
     *
     * @param organizationId       组织id
     * @param id                   评论id
     * @param pageCommentUpdateDTO 评论信息
     * @return
     */
    @Permission(type = ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR,
                    InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation("更新page评论")
    @PutMapping(value = "/{id}")
    public ResponseEntity<PageCommentDTO> update(@ApiParam(value = "组织ID", required = true)
                                                 @PathVariable(value = "organization_id") Long organizationId,
                                                 @ApiParam(value = "评论id", required = true)
                                                 @PathVariable Long id,
                                                 @ApiParam(value = "评论信息", required = true)
                                                 @RequestBody @Valid PageCommentUpdateDTO pageCommentUpdateDTO) {
        return new ResponseEntity<>(pageCommentService.update(id,
                pageCommentUpdateDTO),
                HttpStatus.CREATED);
    }

    /**
     * 通过id删除评论
     *
     * @param organizationId 组织ID
     * @param id             评论id
     * @return ResponseEntity
     */
    @Permission(type = ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR,
                    InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation("通过id删除评论")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity deleteIssueComment(@ApiParam(value = "组织ID", required = true)
                                             @PathVariable(value = "organization_id") Long organizationId,
                                             @ApiParam(value = "评论id", required = true)
                                             @PathVariable Long id) {
        pageCommentService.delete(id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

}
