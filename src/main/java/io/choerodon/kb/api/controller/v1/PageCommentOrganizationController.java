package io.choerodon.kb.api.controller.v1;

import io.choerodon.swagger.annotation.Permission;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.api.vo.PageCommentVO;
import io.choerodon.kb.api.vo.PageCreateCommentVO;
import io.choerodon.kb.api.vo.PageUpdateCommentVO;
import io.choerodon.kb.app.service.PageCommentService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
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
@RequestMapping(value = "/v1/organizations/{organization_id}/page_comment")
public class PageCommentOrganizationController {

    @Autowired
    private PageCommentService pageCommentService;

    /**
     * 创建page评论
     *
     * @param organizationId      组织id
     * @param pageCreateCommentVO 评论信息
     * @return List<PageCommentVO>
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("创建page评论")
    @PostMapping
    public ResponseEntity<PageCommentVO> create(@ApiParam(value = "组织ID", required = true)
                                                @PathVariable(value = "organization_id") Long organizationId,
                                                @ApiParam(value = "评论信息", required = true)
                                                @RequestBody @Valid @Encrypt PageCreateCommentVO pageCreateCommentVO) {
        return new ResponseEntity<>(pageCommentService.create(organizationId, null, pageCreateCommentVO), HttpStatus.CREATED);
    }


    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = " 查询页面评论")
    @GetMapping(value = "/list")
    public ResponseEntity<List<PageCommentVO>> queryByList(
            @ApiParam(value = "组织id", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "页面id", required = true)
            @RequestParam @Encrypt Long pageId) {
        return new ResponseEntity<>(pageCommentService.queryByPageId(organizationId, null, pageId), HttpStatus.OK);
    }

    /**
     * 更新page评论
     *
     * @param organizationId      组织id
     * @param id                  评论id
     * @param pageUpdateCommentVO 评论信息
     * @return
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("更新page评论")
    @PutMapping(value = "/{id}")
    public ResponseEntity<PageCommentVO> update(@ApiParam(value = "组织ID", required = true)
                                                @PathVariable(value = "organization_id") Long organizationId,
                                                @ApiParam(value = "评论id", required = true)
                                                @PathVariable Long id,
                                                @ApiParam(value = "评论信息", required = true)
                                                @RequestBody @Valid @Encrypt PageUpdateCommentVO pageUpdateCommentVO) {
        return new ResponseEntity<>(pageCommentService.update(organizationId, null, id, pageUpdateCommentVO), HttpStatus.CREATED);
    }

    /**
     * 通过id删除评论（管理员权限）
     *
     * @param organizationId 组织ID
     * @param id             评论id
     * @return ResponseEntity
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("通过id删除评论（管理员权限）")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity deleteComment(@ApiParam(value = "组织ID", required = true)
                                        @PathVariable(value = "organization_id") Long organizationId,
                                        @ApiParam(value = "评论id", required = true)
                                        @PathVariable @Encrypt Long id) {
        pageCommentService.delete(organizationId, null, id, true);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * 通过id删除评论（删除自己的评论）
     *
     * @param organizationId 组织ID
     * @param id             评论id
     * @return ResponseEntity
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("通过id删除评论（删除自己的评论）")
    @DeleteMapping(value = "/delete_my/{id}")
    public ResponseEntity deleteMyComment(@ApiParam(value = "组织ID", required = true)
                                          @PathVariable(value = "organization_id") Long organizationId,
                                          @ApiParam(value = "评论id", required = true)
                                          @PathVariable @Encrypt Long id) {
        pageCommentService.delete(organizationId, null, id, false);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


}
