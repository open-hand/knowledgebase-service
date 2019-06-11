package io.choerodon.kb.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.kb.api.dao.WorkSpaceShareDTO;
import io.choerodon.kb.app.service.WorkSpaceShareService;

/**
 * Created by Zenger on 2019/6/10.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/work_space_share")
public class WorkSpaceShareProjectController {

    private WorkSpaceShareService workSpaceShareService;

    public WorkSpaceShareProjectController(WorkSpaceShareService workSpaceShareService) {
        this.workSpaceShareService = workSpaceShareService;
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下创建页面分享链接")
    @PostMapping
    public ResponseEntity<WorkSpaceShareDTO> create(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "工作空间ID", required = true)
            @RequestParam("work_space_id") Long workSpaceId,
            @ApiParam(value = "是否分享子页面", required = true)
            @RequestParam(value = "is_contain", defaultValue = "false") Boolean isContain) {
        return new ResponseEntity<>(workSpaceShareService.create(workSpaceId,
                isContain), HttpStatus.CREATED);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询项目下页面分享链接")
    @GetMapping(value = "/{id}")
    public ResponseEntity<WorkSpaceShareDTO> query(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "页面分享链接id", required = true)
            @PathVariable Long id) {
        return new ResponseEntity<>(workSpaceShareService.query(id), HttpStatus.OK);
    }
}
