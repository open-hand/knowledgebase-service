package io.choerodon.kb.api.controller.v1;

import java.util.Optional;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.Permission;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.app.service.ProjectOperateService;
import io.choerodon.kb.infra.feign.vo.ProjectDO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping(value = "/projects/{project_id}/project_operate/list_project")
    public ResponseEntity<Page<ProjectDO>> pageProjectInfo(@ApiParam(value = "项目id", required = true)
                                                           @PathVariable(value = "project_id") Long projectId,
                                                           @ApiParam(value = "组织id", required = true)
                                                           @RequestParam Long organizationId,
                                                           @SortDefault PageRequest pageRequest) {
        return Optional.ofNullable(projectOperateService.pageProjectInfo(organizationId, projectId, pageRequest))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.query.project"));

    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("组织层查找分页查找组织下所有项目")
    @GetMapping(value = "/organizations/{organization_id}/project_operate/list_project")
    public ResponseEntity<Page<ProjectDO>> listOrganizationProjectInfo(@ApiParam(value = "组织id", required = true)
                                                                       @PathVariable(value = "organization_id") Long organizationId,
                                                                       @SortDefault PageRequest pageRequest) {
        return Optional.ofNullable(projectOperateService.pageProjectInfo(organizationId, 0L, pageRequest))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.query.project"));

    }
}
