package io.choerodon.kb.api.controller.v1;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.api.vo.ProjectDTO;
import io.choerodon.kb.app.service.ProjectOperateProService;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * @author zhaotianxin
 * @since 2020/1/3
 */
@RestController
@RequestMapping("/v1")
public class ProjectOperateController {
    @Autowired
    private ProjectOperateProService projectOperateProService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("分页查找组织下所有项目")
    @PostMapping(value = "/projects/{project_id}/project_operate/list_project")
    public ResponseEntity<Page<ProjectDTO>> pageProjectInfo(@ApiParam(value = "项目id", required = true)
                                                           @PathVariable(value = "project_id") Long projectId,
                                                           @ApiParam(value = "组织id", required = true)
                                                           @RequestParam Long organizationId,
                                                           @SortDefault PageRequest pageRequest,
                                                           @RequestBody ProjectDTO project) {
        return Optional.ofNullable(projectOperateProService.pageProjectInfo(organizationId, projectId, pageRequest, project))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.query.project"));

    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("组织层查找分页查找组织下所有项目")
    @PostMapping(value = "/organizations/{organization_id}/project_operate/list_project")
    public ResponseEntity<Page<ProjectDTO>> listOrganizationProjectInfo(@ApiParam(value = "组织id", required = true)
                                                                       @PathVariable(value = "organization_id") Long organizationId,
                                                                       @SortDefault PageRequest pageRequest,
                                                                       @RequestBody ProjectDTO project) {
        return Optional.ofNullable(projectOperateProService.pageProjectInfo(organizationId, 0L, pageRequest, project))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.query.project"));

    }
}
