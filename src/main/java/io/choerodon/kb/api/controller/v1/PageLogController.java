package io.choerodon.kb.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.kb.api.dao.PageLogVO;
import io.choerodon.kb.app.service.PageLogService;

/**
 * Created by Zenger on 2019/5/17.
 */
@RestController
@RequestMapping(value = "/v1")
public class PageLogController {

    private PageLogService pageLogService;

    public PageLogController(PageLogService pageLogService) {
        this.pageLogService = pageLogService;
    }

    /**
     * 查询页面操作日志
     *
     * @param organizationId 组织ID
     * @param pageId         页面id
     * @return List<PageLogVO>
     */
    @Permission(type = ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR,
                    InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation("查询页面操作日志")
    @GetMapping(value = "/organizations/{organization_id}/page_log/{page_id}")
    public ResponseEntity<List<PageLogVO>> listOrgByPageId(@ApiParam(value = "组织ID", required = true)
                                                            @PathVariable(value = "organization_id") Long organizationId,
                                                           @ApiParam(value = "页面id", required = true)
                                                            @PathVariable(name = "page_id") Long pageId) {
        return new ResponseEntity<>(pageLogService.listByPageId(pageId), HttpStatus.OK);
    }

    /**
     * 查询页面操作日志
     *
     * @param projectId 项目ID
     * @param pageId    页面id
     * @return List<PageLogVO>
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation("查询页面操作日志")
    @GetMapping(value = "/projects/{project_id}/page_log/{page_id}")
    public ResponseEntity<List<PageLogVO>> listProjectByPageId(@ApiParam(value = "项目ID", required = true)
                                                                @PathVariable(value = "project_id") Long projectId,
                                                               @ApiParam(value = "页面id", required = true)
                                                                @PathVariable(name = "page_id") Long pageId) {
        return new ResponseEntity<>(pageLogService.listByPageId(pageId), HttpStatus.OK);
    }
}
