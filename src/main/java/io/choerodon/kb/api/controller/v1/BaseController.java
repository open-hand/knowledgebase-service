package io.choerodon.kb.api.controller.v1;

import java.util.Optional;
import com.github.pagehelper.PageInfo;
import io.choerodon.core.annotation.Permission;
import io.choerodon.core.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.kb.app.service.BaseService;
import io.choerodon.kb.infra.feign.vo.ProjectDO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author zhaotianxin
 * @since 2020/1/3
 */
@RestController
@RequestMapping("/v1")
public class BaseController {
    @Autowired
    private BaseService baseService;

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,InitRoleCode.PROJECT_MEMBER})
    @ApiOperation("分页查找组织下所有项目")
    @GetMapping(value = "/projects/{project_id}/list_project")
    public ResponseEntity<PageInfo<ProjectDO>> pageProjectInfo(@ApiParam(value = "项目id", required = true)
                                                                  @PathVariable(value = "project_id") Long projectId,
                                                                  @ApiParam(value = "组织id", required = true)
                                                                  @RequestParam Long organizationId,
                                                                  @SortDefault Pageable pageable) {

        return Optional.ofNullable(baseService.pageProjectInfo(organizationId,projectId,pageable))
                .map(result->new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.query.project"));

    }
}
