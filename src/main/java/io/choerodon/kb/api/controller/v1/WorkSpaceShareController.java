package io.choerodon.kb.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.base.annotation.Permission;
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
    @PostMapping(value = "/tree")
    public ResponseEntity<WorkSpaceFirstTreeDTO> queryTree(
            @ApiParam(value = "分享链接token", required = true)
            @RequestParam("token") String token) {
        return new ResponseEntity<>(workSpaceShareService.queryTree(token),
                HttpStatus.OK);
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "查询分享链接的页面信息")
    @PostMapping(value = "/page")
    public ResponseEntity<PageDTO> queryPage(
            @ApiParam(value = "分享链接token", required = true)
            @RequestParam("token") String token) {
        return new ResponseEntity<>(workSpaceShareService.queryPage(token),
                HttpStatus.OK);
    }
}
