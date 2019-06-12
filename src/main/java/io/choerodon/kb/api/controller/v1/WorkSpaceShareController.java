package io.choerodon.kb.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.base.annotation.Permission;
import io.choerodon.kb.api.dao.PageAttachmentDTO;
import io.choerodon.kb.api.dao.PageDTO;
import io.choerodon.kb.api.dao.WorkSpaceFirstTreeDTO;
import io.choerodon.kb.app.service.WorkSpaceShareService;

/**
 * Created by Zenger on 2019/6/11.
 */
@RestController
@RequestMapping(value = "/v1/work_space_share")
public class WorkSpaceShareController {

    private WorkSpaceShareService workSpaceShareService;

    public WorkSpaceShareController(WorkSpaceShareService workSpaceShareService) {
        this.workSpaceShareService = workSpaceShareService;
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "查询分享链接的树形结构")
    @GetMapping(value = "/tree")
    public ResponseEntity<WorkSpaceFirstTreeDTO> queryTree(
            @ApiParam(value = "分享链接token", required = true)
            @RequestParam("token") String token) {
        return new ResponseEntity<>(workSpaceShareService.queryTree(token),
                HttpStatus.OK);
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "查询分享链接的页面信息")
    @GetMapping(value = "/page")
    public ResponseEntity<PageDTO> queryPage(
            @ApiParam(value = "工作空间ID", required = true)
            @RequestParam("work_space_id") Long workSpaceId,
            @ApiParam(value = "分享链接token", required = true)
            @RequestParam("token") String token) {
        return new ResponseEntity<>(workSpaceShareService.queryPage(workSpaceId, token),
                HttpStatus.OK);
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "查询分享链接的页面附件")
    @GetMapping(value = "/page_attachment")
    public ResponseEntity<List<PageAttachmentDTO>> queryPageAttachment(
            @ApiParam(value = "工作空间ID", required = true)
            @RequestParam("work_space_id") Long workSpaceId,
            @ApiParam(value = "分享链接token", required = true)
            @RequestParam("token") String token) {
        return new ResponseEntity<>(workSpaceShareService.queryPageAttachment(workSpaceId, token),
                HttpStatus.OK);
    }
}
