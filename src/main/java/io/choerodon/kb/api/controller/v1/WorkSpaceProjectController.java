package io.choerodon.kb.api.controller.v1;

import io.choerodon.core.annotation.Permission;
import io.choerodon.core.enums.ResourceType;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.kb.api.vo.*;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * Created by Zenger on 2019/4/30.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/work_space")
public class WorkSpaceProjectController {

    private WorkSpaceService workSpaceService;

    public WorkSpaceProjectController(WorkSpaceService workSpaceService) {
        this.workSpaceService = workSpaceService;
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下创建页面和空页面")
    @PostMapping
    public ResponseEntity<WorkSpaceInfoVO> createWorkSpaceAndPage(@ApiParam(value = "项目ID", required = true)
                                                                  @PathVariable(value = "project_id") Long projectId,
                                                                  @ApiParam(value = "组织id", required = true)
                                                                  @RequestParam Long organizationId,
                                                                  @ApiParam(value = "页面信息", required = true)
                                                                  @RequestBody @Valid PageCreateWithoutContentVO pageCreateVO) {
        return new ResponseEntity<>(workSpaceService.createWorkSpaceAndPage(organizationId, projectId, pageCreateVO), HttpStatus.CREATED);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询项目下工作空间节点页面")
    @GetMapping(value = "/{id}")
    public ResponseEntity<WorkSpaceInfoVO> queryWorkSpaceInfo(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "工作空间目录id", required = true)
            @PathVariable Long id,
            @ApiParam(value = "组织id", required = true)
            @RequestParam Long organizationId,
            @ApiParam(value = "应用于全文检索时，对单篇文章，根据检索内容高亮内容")
            @RequestParam(required = false) String searchStr) {
        return new ResponseEntity<>(workSpaceService.queryWorkSpaceInfo(organizationId, projectId, id, searchStr), HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "更新项目下工作空间节点页面")
    @PutMapping(value = "/{id}")
    public ResponseEntity<WorkSpaceInfoVO> updateWorkSpaceAndPage(@ApiParam(value = "项目id", required = true)
                                                                  @PathVariable(value = "project_id") Long projectId,
                                                                  @ApiParam(value = "组织id", required = true)
                                                                  @RequestParam Long organizationId,
                                                                  @ApiParam(value = "工作空间目录id", required = true)
                                                                  @PathVariable Long id,
                                                                  @ApiParam(value = "应用于全文检索时，对单篇文章，根据检索内容高亮内容")
                                                                  @RequestParam(required = false) String searchStr,
                                                                  @ApiParam(value = "空间信息", required = true)
                                                                  @RequestBody @Valid PageUpdateVO pageUpdateVO) {
        return new ResponseEntity<>(workSpaceService.updateWorkSpaceAndPage(organizationId, projectId, id, searchStr, pageUpdateVO), HttpStatus.CREATED);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "移动文章")
    @PostMapping(value = "/to_move/{id}")
    public ResponseEntity moveWorkSpace(@ApiParam(value = "项目id", required = true)
                                        @PathVariable(value = "project_id") Long projectId,
                                        @ApiParam(value = "组织id", required = true)
                                        @RequestParam Long organizationId,
                                        @ApiParam(value = "工作空间目录id", required = true)
                                        @PathVariable Long id,
                                        @ApiParam(value = "移动信息", required = true)
                                        @RequestBody @Valid MoveWorkSpaceVO moveWorkSpaceVO) {
        workSpaceService.moveWorkSpace(organizationId, projectId, id, moveWorkSpaceVO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询空间树形结构")
    @GetMapping(value = "/all_tree")
    public ResponseEntity<Map<String, Object>> queryAllTreeList(@ApiParam(value = "项目id", required = true)
                                                                             @PathVariable(value = "project_id") Long projectId,
                                                                             @ApiParam(value = "组织id", required = true)
                                                                             @RequestParam Long organizationId,
                                                                             @ApiParam(value = "知识库id", required = true)
                                                                             @RequestParam Long baseId,
                                                                             @ApiParam(value = "展开的空间id")
                                                                             @RequestParam(required = false) Long expandWorkSpaceId) {
        return new ResponseEntity<>(workSpaceService.queryAllTreeList(organizationId, projectId, expandWorkSpaceId,baseId), HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询项目下的所有空间")
    @GetMapping
    public ResponseEntity<List<WorkSpaceVO>> queryAllSpaceByOptions(@ApiParam(value = "项目id", required = true)
                                                                    @PathVariable(value = "project_id") Long projectId,
                                                                    @ApiParam(value = "组织id", required = true)
                                                                    @RequestParam Long organizationId,
                                                                    @RequestParam Long baseId) {
        return new ResponseEntity<>(workSpaceService.queryAllSpaceByOptions(organizationId, projectId,baseId), HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "根据spaceIds查询空间列表")
    @PostMapping(value = "/query_by_space_ids")
    public ResponseEntity<List<WorkSpaceVO>> querySpaceByIds(@ApiParam(value = "项目id", required = true)
                                                             @PathVariable(value = "project_id") Long projectId,
                                                             @ApiParam(value = "space ids", required = true)
                                                             @RequestBody List<Long> spaceIds) {
        return new ResponseEntity<>(workSpaceService.querySpaceByIds(projectId, spaceIds), HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "移除项目下工作空间及页面到回收站（管理员权限）")
    @PutMapping(value = "/remove/{id}")
    public ResponseEntity removeWorkSpaceAndPage(@ApiParam(value = "项目id", required = true)
                                                 @PathVariable(value = "project_id") Long projectId,
                                                 @ApiParam(value = "组织id", required = true)
                                                 @RequestParam Long organizationId,
                                                 @ApiParam(value = "工作空间目录id", required = true)
                                                 @PathVariable Long id) {
        workSpaceService.removeWorkSpaceAndPage(organizationId, projectId, id, true);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "移除项目下工作空间及页面到回收站（移除自己的空间）")
    @PutMapping(value = "/remove_my/{id}")
    public ResponseEntity removeWorkSpaceAndPageByMyWorkSpace(@ApiParam(value = "项目id", required = true)
                                                              @PathVariable(value = "project_id") Long projectId,
                                                              @ApiParam(value = "组织id", required = true)
                                                              @RequestParam Long organizationId,
                                                              @ApiParam(value = "工作空间目录id", required = true)
                                                              @PathVariable Long id) {
        workSpaceService.removeWorkSpaceAndPage(organizationId, projectId, id, false);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询最近更新的空间列表")
    @GetMapping(value = "/recent_update_list")
    public ResponseEntity<List<WorkSpaceRecentInfoVO>> recentUpdateList(@ApiParam(value = "项目id", required = true)
                                                                        @PathVariable(value = "project_id") Long projectId,
                                                                        @ApiParam(value = "组织id", required = true)
                                                                        @RequestParam Long organizationId,
                                                                        @RequestParam Long baseId) {
        return new ResponseEntity<>(workSpaceService.recentUpdateList(organizationId, projectId,baseId), HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询空间所属知识库是否存在")
    @GetMapping(value = "/belong_base_exist/{id}")
    public ResponseEntity<Boolean> belongToBaseDelete(@ApiParam(value = "项目id", required = true)
                                                                        @PathVariable(value = "project_id") Long projectId,
                                                                        @ApiParam(value = "组织id", required = true)
                                                                        @RequestParam Long organizationId,
                                                                         @ApiParam(value = "工作空间目录id", required = true)
                                                                         @PathVariable Long id) {
        return new ResponseEntity<>(workSpaceService.belongToBaseExist(organizationId, projectId,id), HttpStatus.OK);
    }
}
