package io.choerodon.kb.api.controller.v1;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.kb.api.dao.*;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.infra.common.enums.PageResourceType;
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

    /**
     * 项目下创建页面
     *
     * @param projectId     项目id
     * @param pageCreateDTO 页面信息
     * @return PageDTO
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下创建页面")
    @PostMapping
    public ResponseEntity<PageDTO> create(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "页面信息", required = true)
            @RequestBody @Valid PageCreateDTO pageCreateDTO) {
        return new ResponseEntity<>(workSpaceService.create(projectId,
                pageCreateDTO,
                PageResourceType.PROJECT.getResourceType()), HttpStatus.CREATED);
    }

    /**
     * 查询项目下工作空间节点页面
     *
     * @param projectId 项目id
     * @param id        工作空间目录id
     * @return PageDTO
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询项目下工作空间节点页面")
    @GetMapping(value = "/{id}")
    public ResponseEntity<PageDTO> query(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "工作空间目录id", required = true)
            @PathVariable Long id) {
        return new ResponseEntity<>(workSpaceService.queryDetail(null, projectId, id), HttpStatus.OK);
    }

    /**
     * 更新项目下工作空间节点页面
     *
     * @param projectId     项目id
     * @param id            工作空间目录id
     * @param pageUpdateDTO 页面信息
     * @return PageDTO
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "更新项目下工作空间节点页面")
    @PutMapping(value = "/{id}")
    public ResponseEntity<PageDTO> update(@ApiParam(value = "项目id", required = true)
                                          @PathVariable(value = "project_id") Long projectId,
                                          @ApiParam(value = "工作空间目录id", required = true)
                                          @PathVariable Long id,
                                          @ApiParam(value = "空间信息", required = true)
                                          @RequestBody @Valid PageUpdateDTO pageUpdateDTO) {
        return new ResponseEntity<>(workSpaceService.update(
                projectId,
                id,
                pageUpdateDTO,
                PageResourceType.PROJECT.getResourceType()),
                HttpStatus.CREATED);
    }

    /**
     * 删除项目下工作空间节点页面（管理员权限）
     *
     * @param projectId 项目id
     * @param id        工作空间目录id
     * @return ResponseEntity
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "删除项目下工作空间节点页面（管理员权限）")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity delete(@ApiParam(value = "项目id", required = true)
                                 @PathVariable(value = "project_id") Long projectId,
                                 @ApiParam(value = "工作空间目录id", required = true)
                                 @PathVariable Long id) {
        workSpaceService.delete(projectId, id, PageResourceType.PROJECT.getResourceType(), true);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 删除项目下工作空间节点页面（删除自己的空间）
     *
     * @param projectId 项目id
     * @param id        工作空间目录id
     * @return ResponseEntity
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "删除项目下工作空间节点页面（删除自己的空间）")
    @DeleteMapping(value = "/delete_my/{id}")
    public ResponseEntity deleteMyWorkSpace(@ApiParam(value = "项目id", required = true)
                                            @PathVariable(value = "project_id") Long projectId,
                                            @ApiParam(value = "工作空间目录id", required = true)
                                            @PathVariable Long id) {
        workSpaceService.delete(projectId, id, PageResourceType.PROJECT.getResourceType(), false);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "移动文章")
    @PostMapping(value = "/to_move/{id}")
    public ResponseEntity moveWorkSpace(@ApiParam(value = "项目id", required = true)
                                        @PathVariable(value = "project_id") Long projectId,
                                        @ApiParam(value = "工作空间目录id", required = true)
                                        @PathVariable Long id,
                                        @ApiParam(value = "移动信息", required = true)
                                        @RequestBody @Valid MoveWorkSpaceDTO moveWorkSpaceDTO) {
        workSpaceService.moveWorkSpace(projectId,
                id,
                moveWorkSpaceDTO,
                PageResourceType.PROJECT.getResourceType());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询空间树形结构")
    @GetMapping(value = "/all_tree")
    public ResponseEntity<Map<String, Object>> queryAllTree(@ApiParam(value = "项目id", required = true)
                                                            @PathVariable(value = "project_id") Long projectId,
                                                            @ApiParam(value = "展开的空间id")
                                                            @RequestParam(required = false) Long expandWorkSpaceId) {
        return new ResponseEntity<>(workSpaceService.queryAllTree(projectId, expandWorkSpaceId, PageResourceType.PROJECT.getResourceType()), HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询项目下的所有空间")
    @GetMapping(value = "/project_space")
    public ResponseEntity<List<WorkSpaceDTO>> queryAllSpaceByOptions(@ApiParam(value = "项目id", required = true)
                                                                     @PathVariable(value = "project_id") Long projectId) {
        return new ResponseEntity<>(workSpaceService.queryAllSpaceByOptions(projectId, PageResourceType.PROJECT.getResourceType()), HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "根据spaceIds查询空间列表")
    @GetMapping(value = "/query_by_space_ids")
    public ResponseEntity<List<WorkSpaceDTO>> querySpaceByIds(@ApiParam(value = "项目id", required = true)
                                                                   @PathVariable(value = "project_id") Long projectId,
                                                                   @ApiParam(value = "space ids", required = true)
                                                                   @RequestBody List<Long> spaceIds) {
        return new ResponseEntity<>(workSpaceService.querySpaceByIds(projectId, spaceIds), HttpStatus.OK);
    }
}
