package io.choerodon.kb.api.controller.v1;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.kb.api.vo.PageCommentVO;
import io.choerodon.kb.api.vo.PageCreateCommentVO;
import io.choerodon.kb.api.vo.PageUpdateCommentVO;
import io.choerodon.kb.app.service.PageCommentService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Created by Zenger on 2019/4/30.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/page_comment")
public class PageCommentProjectController {

    @Autowired
    private PageCommentService pageCommentService;

    /**
     * 创建page评论
     *
     * @param projectId           项目ID
     * @param pageCreateCommentVO 评论信息
     * @return List<PageCommentVO>
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation("创建page评论")
    @PostMapping
    public ResponseEntity<PageCommentVO> create(@ApiParam(value = "项目ID", required = true)
                                                @PathVariable(value = "project_id") Long projectId,
                                                @ApiParam(value = "组织id", required = true)
                                                @RequestParam Long organizationId,
                                                @ApiParam(value = "评论信息", required = true)
                                                @RequestBody @Valid PageCreateCommentVO pageCreateCommentVO) {
        return new ResponseEntity<>(pageCommentService.create(organizationId, projectId, pageCreateCommentVO), HttpStatus.CREATED);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = " 查询页面评论")
    @GetMapping(value = "/list")
    public ResponseEntity<List<PageCommentVO>> queryByPageId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "组织id", required = true)
            @RequestParam Long organizationId,
            @ApiParam(value = "页面id", required = true)
            @RequestParam Long pageId) {
        return new ResponseEntity<>(pageCommentService.queryByPageId(organizationId, projectId, pageId), HttpStatus.OK);
    }

    /**
     * 更新page评论
     *
     * @param projectId           项目ID
     * @param id                  评论id
     * @param pageUpdateCommentVO 评论信息
     * @return
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation("更新page评论")
    @PutMapping(value = "/{id}")
    public ResponseEntity<PageCommentVO> update(@ApiParam(value = "项目ID", required = true)
                                                @PathVariable(value = "project_id") Long projectId,
                                                @ApiParam(value = "组织id", required = true)
                                                @RequestParam Long organizationId,
                                                @ApiParam(value = "评论id", required = true)
                                                @PathVariable Long id,
                                                @ApiParam(value = "评论信息", required = true)
                                                @RequestBody @Valid PageUpdateCommentVO pageUpdateCommentVO) {
        return new ResponseEntity<>(pageCommentService.update(organizationId, projectId, id, pageUpdateCommentVO), HttpStatus.CREATED);
    }

    /**
     * 通过id删除评论（管理员权限）
     *
     * @param projectId 项目ID
     * @param id        评论id
     * @return ResponseEntity
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation("通过id删除评论（管理员权限）")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity deleteComment(@ApiParam(value = "项目ID", required = true)
                                        @PathVariable(value = "project_id") Long projectId,
                                        @ApiParam(value = "组织id", required = true)
                                        @RequestParam Long organizationId,
                                        @ApiParam(value = "评论id", required = true)
                                        @PathVariable Long id) {
        pageCommentService.delete(organizationId, projectId, id, true);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * 通过id删除评论（删除自己的评论）
     *
     * @param projectId 项目ID
     * @param id        评论id
     * @return ResponseEntity
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation("通过id删除评论（删除自己的评论）")
    @DeleteMapping(value = "/delete_my/{id}")
    public ResponseEntity deleteMyComment(@ApiParam(value = "项目ID", required = true)
                                          @PathVariable(value = "project_id") Long projectId,
                                          @ApiParam(value = "组织id", required = true)
                                          @RequestParam Long organizationId,
                                          @ApiParam(value = "评论id", required = true)
                                          @PathVariable Long id) {
        pageCommentService.delete(organizationId, projectId, id, false);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
