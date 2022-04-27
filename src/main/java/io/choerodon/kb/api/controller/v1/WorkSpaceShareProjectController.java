package io.choerodon.kb.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.validation.Valid;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.api.vo.WorkSpaceShareUpdateVO;
import io.choerodon.kb.api.vo.WorkSpaceShareVO;
import io.choerodon.kb.app.service.WorkSpaceShareService;
import io.choerodon.swagger.annotation.Permission;

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

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询分享链接（不存在则创建）")
    @GetMapping
    public ResponseEntity<WorkSpaceShareVO> queryShare(@ApiParam(value = "项目ID", required = true)
                                                       @PathVariable(value = "project_id") Long projectId,
                                                       @ApiParam(value = "组织id", required = true)
                                                       @RequestParam Long organizationId,
                                                       @ApiParam(value = "工作空间ID", required = true)
                                                       @RequestParam("work_space_id") @Encrypt Long workSpaceId) {
        return new ResponseEntity<>(workSpaceShareService.queryShare(organizationId, projectId, workSpaceId), HttpStatus.CREATED);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "修改分享链接类型")
    @PutMapping(value = "/{id}")
    public ResponseEntity<WorkSpaceShareVO> updateShare(@ApiParam(value = "项目ID", required = true)
                                                        @PathVariable(value = "project_id") Long projectId,
                                                        @ApiParam(value = "组织id", required = true)
                                                        @RequestParam Long organizationId,
                                                        @ApiParam(value = "分享id", required = true)
                                                        @PathVariable @Encrypt Long id,
                                                        @ApiParam(value = "修改信息", required = true)
                                                        @RequestBody @Valid WorkSpaceShareUpdateVO workSpaceShareUpdateVO) {
        return new ResponseEntity<>(workSpaceShareService.updateShare(organizationId, projectId, id, workSpaceShareUpdateVO), HttpStatus.CREATED);
    }
}
