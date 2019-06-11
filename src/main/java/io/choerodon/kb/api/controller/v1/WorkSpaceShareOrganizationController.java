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
@RequestMapping(value = "/v1/organizations/{organization_id}/work_space_share")
public class WorkSpaceShareOrganizationController {

    private WorkSpaceShareService workSpaceShareService;

    public WorkSpaceShareOrganizationController(WorkSpaceShareService workSpaceShareService) {
        this.workSpaceShareService = workSpaceShareService;
    }

    @Permission(type = ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR,
                    InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation(value = "组织下创建页面分享链接")
    @PostMapping
    public ResponseEntity<WorkSpaceShareDTO> create(
            @ApiParam(value = "组织id", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "工作空间ID", required = true)
            @RequestParam("work_space_id") Long workSpaceId,
            @ApiParam(value = "是否分享子页面", required = true)
            @RequestParam(value = "is_contain", defaultValue = "false") Boolean isContain) {
        return new ResponseEntity<>(workSpaceShareService.create(workSpaceId,
                isContain), HttpStatus.CREATED);
    }

    @Permission(type = ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR,
                    InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation(value = "查询组织下页面分享链接")
    @GetMapping(value = "/{id}")
    public ResponseEntity<WorkSpaceShareDTO> query(
            @ApiParam(value = "组织id", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "页面分享链接id", required = true)
            @PathVariable Long id) {
        return new ResponseEntity<>(workSpaceShareService.query(id), HttpStatus.OK);
    }
}
