package io.choerodon.kb.api.controller.v1;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.kb.api.dao.PageVersionCompareDTO;
import io.choerodon.kb.api.dao.PageVersionDTO;
import io.choerodon.kb.api.dao.PageVersionInfoDTO;
import io.choerodon.kb.app.service.PageVersionService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author shinan.chen
 * @since 2019/5/17
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/page_version")
public class PageVersionProjectController {

    @Autowired
    private PageVersionService pageVersionService;

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询页面的版本列表")
    @GetMapping("/list")
    public ResponseEntity<List<PageVersionDTO>> listQuery(@ApiParam(value = "项目id", required = true)
                                                          @PathVariable("project_id") Long projectId,
                                                          @ApiParam(value = "组织id", required = true)
                                                          @RequestParam Long organizationId,
                                                          @ApiParam(value = "页面id", required = true)
                                                          @RequestParam Long pageId) {
        return new ResponseEntity<>(pageVersionService.queryByPageId(organizationId, projectId, pageId), HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询版本内容")
    @GetMapping(value = "/{version_id}")
    public ResponseEntity<PageVersionInfoDTO> queryById(@ApiParam(value = "项目id", required = true)
                                                        @PathVariable("project_id") Long projectId,
                                                        @ApiParam(value = "版本id", required = true)
                                                        @PathVariable("version_id") Long versionId,
                                                        @ApiParam(value = "组织id", required = true)
                                                        @RequestParam Long organizationId,
                                                        @ApiParam(value = "页面id", required = true)
                                                        @RequestParam Long pageId) {
        return new ResponseEntity<>(pageVersionService.queryById(organizationId, projectId, pageId, versionId), HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "版本比较")
    @GetMapping(value = "/compare")
    public ResponseEntity<PageVersionCompareDTO> compareVersion(@ApiParam(value = "项目id", required = true)
                                                                @PathVariable("project_id") Long projectId,
                                                                @ApiParam(value = "组织id", required = true)
                                                                @RequestParam Long organizationId,
                                                                @ApiParam(value = "第一个版本id", required = true)
                                                                @RequestParam Long firstVersionId,
                                                                @ApiParam(value = "第二个版本id", required = true)
                                                                @RequestParam Long secondVersionId,
                                                                @ApiParam(value = "页面id", required = true)
                                                                @RequestParam Long pageId) {
        return new ResponseEntity<>(pageVersionService.compareVersion(organizationId, projectId, pageId, firstVersionId, secondVersionId), HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "版本回退")
    @GetMapping(value = "/rollback")
    public ResponseEntity rollbackVersion(@ApiParam(value = "项目id", required = true)
                                          @PathVariable("project_id") Long projectId,
                                          @ApiParam(value = "组织id", required = true)
                                          @RequestParam Long organizationId,
                                          @ApiParam(value = "版本id", required = true)
                                          @RequestParam Long versionId,
                                          @ApiParam(value = "页面id", required = true)
                                          @RequestParam Long pageId) {
        pageVersionService.rollbackVersion(organizationId, projectId, pageId, versionId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
