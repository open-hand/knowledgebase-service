package io.choerodon.kb.api.controller.v1;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.api.vo.PageCommentVO;
import io.choerodon.kb.api.vo.PageCreateCommentVO;
import io.choerodon.kb.api.vo.PageUpdateCommentVO;
import io.choerodon.kb.app.service.PageCommentService;
import io.choerodon.swagger.annotation.Permission;
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
 * @author superlee
 * @since 2021-09-16
 */
@RestController
@RequestMapping({"/v1/organizations/{organization_id}/page_comment"})
public class PageCommentOrganizationController {

    @Autowired
    private PageCommentService pageCommentService;


    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("创建page评论")
    @PostMapping
    public ResponseEntity<PageCommentVO> create(@ApiParam(value = "组织ID", required = true)
                                                @PathVariable("organization_id") Long organizationId,
                                                @ApiParam(value = "评论信息", required = true)
                                                @RequestBody @Valid @Encrypt PageCreateCommentVO pageCreateCommentVO) {
        return new ResponseEntity(pageCommentService.create(organizationId, null, pageCreateCommentVO), HttpStatus.CREATED);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(" 查询页面评论")
    @GetMapping({"/list"})
    public ResponseEntity<List<PageCommentVO>> queryByList(@ApiParam(value = "组织id", required = true)
                                                           @PathVariable("organization_id") Long organizationId,
                                                           @ApiParam(value = "页面id", required = true)
                                                           @RequestParam @Encrypt(ignoreUserConflict = true) Long pageId) {
        return new ResponseEntity(pageCommentService.queryByPageId(organizationId, null, pageId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("更新page评论")
    @PutMapping({"/{id}"})
    public ResponseEntity<PageCommentVO> update(@ApiParam(value = "组织ID", required = true)
                                                @PathVariable("organization_id") Long organizationId,
                                                @ApiParam(value = "评论id", required = true)
                                                @PathVariable @Encrypt Long id,
                                                @ApiParam(value = "评论信息", required = true)
                                                @RequestBody @Valid PageUpdateCommentVO pageUpdateCommentVO) {
        return new ResponseEntity(pageCommentService.update(organizationId, null, id, pageUpdateCommentVO), HttpStatus.CREATED);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("通过id删除评论（管理员权限）")
    @DeleteMapping({"/{id}"})
    public ResponseEntity deleteComment(@ApiParam(value = "组织ID", required = true)
                                        @PathVariable("organization_id") Long organizationId,
                                        @ApiParam(value = "评论id", required = true)
                                        @PathVariable @Encrypt Long id) {
        pageCommentService.delete(organizationId, null, id, true);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("通过id删除评论（删除自己的评论）")
    @DeleteMapping({"/delete_my/{id}"})
    public ResponseEntity deleteMyComment(@ApiParam(value = "组织ID", required = true)
                                          @PathVariable("organization_id") Long organizationId,
                                          @ApiParam(value = "评论id", required = true)
                                          @PathVariable @Encrypt Long id) {
        pageCommentService.delete(organizationId, null, id, false);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
