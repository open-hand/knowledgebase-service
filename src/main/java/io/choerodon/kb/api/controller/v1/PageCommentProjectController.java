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
import io.choerodon.kb.api.dao.PageCreateCommentDTO;
import io.choerodon.kb.api.dao.PageUpdateCommentDTO;
import io.choerodon.kb.app.service.PageCommentService;

/**
 * Created by Zenger on 2019/4/30.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/page_comment")
public class PageCommentProjectController {

    private PageCommentService pageCommentService;

    public PageCommentProjectController(PageCommentService pageCommentService) {
        this.pageCommentService = pageCommentService;
    }

    /**
     * 创建page评论
     *
     * @param projectId            项目ID
     * @param pageCreateCommentDTO 评论信息
     * @return List<PageCommentDTO>
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation("创建page评论")
    @PostMapping
    public ResponseEntity<PageCommentDTO> create(@ApiParam(value = "项目ID", required = true)
                                                 @PathVariable(value = "project_id") Long projectId,
                                                 @ApiParam(value = "评论信息", required = true)
                                                 @RequestBody @Valid PageCreateCommentDTO pageCreateCommentDTO) {
        return new ResponseEntity<>(pageCommentService.create(pageCreateCommentDTO), HttpStatus.CREATED);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = " 查询页面评论")
    @GetMapping(value = "/list")
    public ResponseEntity<List<PageCommentDTO>> queryByList(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "页面id", required = true)
            @RequestParam Long pageId) {
        return new ResponseEntity<>(pageCommentService.queryByList(pageId), HttpStatus.OK);
    }

    /**
     * 更新page评论
     *
     * @param projectId            项目ID
     * @param id                   评论id
     * @param pageUpdateCommentDTO 评论信息
     * @return
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation("更新page评论")
    @PutMapping(value = "/{id}")
    public ResponseEntity<PageCommentDTO> update(@ApiParam(value = "项目ID", required = true)
                                                 @PathVariable(value = "project_id") Long projectId,
                                                 @ApiParam(value = "评论id", required = true)
                                                 @PathVariable Long id,
                                                 @ApiParam(value = "评论信息", required = true)
                                                 @RequestBody @Valid PageUpdateCommentDTO pageUpdateCommentDTO) {
        return new ResponseEntity<>(pageCommentService.update(
                id,
                pageUpdateCommentDTO),
                HttpStatus.CREATED);
    }

    /**
     * 通过id删除评论
     *
     * @param projectId 项目ID
     * @param id        评论id
     * @return ResponseEntity
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation("通过id删除评论")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity deleteIssueComment(@ApiParam(value = "项目ID", required = true)
                                             @PathVariable(value = "project_id") Long projectId,
                                             @ApiParam(value = "评论id", required = true)
                                             @PathVariable Long id) {
        pageCommentService.delete(id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
