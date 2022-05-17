package io.choerodon.kb.api.controller.v1;

import io.choerodon.kb.api.vo.ProjectDTO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.app.service.ProjectOperateService;
import io.choerodon.kb.infra.feign.vo.ProjectDO;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * @author zhaotianxin
 * @since 2020/1/3
 */
@RestController
@RequestMapping("/v1")
public class ProjectOperateController {
    @Autowired
    private ProjectOperateService projectOperateService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("分页查找组织下所有项目")
    @PostMapping(value = "/projects/{project_id}/project_operate/list_project")
    public ResponseEntity<Page<ProjectDO>> pageProjectInfo(@ApiParam(value = "项目id", required = true)
                                                           @PathVariable(value = "project_id") Long projectId,
                                                           @ApiParam(value = "组织id", required = true)
                                                           @RequestParam Long organizationId,
                                                           @ApiParam(value = "分页信息", required = true)
                                                           @SortDefault PageRequest pageRequest,
                                                           @ApiParam(value = "查询参数", required = true)
                                                           @RequestBody ProjectDTO project) {
        return Optional.ofNullable(projectOperateService.pageProjectInfo(organizationId, projectId, pageRequest, project))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.query.project"));

    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("组织层查找分页查找组织下所有项目")
    @PostMapping(value = "/organizations/{organization_id}/project_operate/list_project")
    public ResponseEntity<Page<ProjectDO>> listOrganizationProjectInfo(@ApiParam(value = "组织id", required = true)
                                                                       @PathVariable(value = "organization_id") Long organizationId,
                                                                       @ApiParam(value = "分页信息", required = true)
                                                                       @SortDefault PageRequest pageRequest,
                                                                       @ApiParam(value = "查询参数", required = true)
                                                                       @RequestBody ProjectDTO project) {
        return Optional.ofNullable(projectOperateService.pageProjectInfo(organizationId, 0L, pageRequest, project))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.query.project"));

    }
}
