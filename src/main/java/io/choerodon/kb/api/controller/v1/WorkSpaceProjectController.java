package io.choerodon.kb.api.controller.v1;

import java.util.List;
import java.util.Map;
import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.kb.api.dao.*;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.infra.common.enums.PageResourceType;

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
        return new ResponseEntity<>(workSpaceService.queryDetail(projectId, id, PageResourceType.PROJECT.getResourceType()), HttpStatus.OK);
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
     * 删除项目下工作空间节点页面
     *
     * @param projectId 项目id
     * @param id        工作空间目录id
     * @return ResponseEntity
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "删除项目下工作空间节点页面")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity delete(@ApiParam(value = "项目id", required = true)
                                 @PathVariable(value = "project_id") Long projectId,
                                 @ApiParam(value = "工作空间目录id", required = true)
                                 @PathVariable Long id) {
        workSpaceService.delete(projectId, id, PageResourceType.PROJECT.getResourceType());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 查询组织的工作空间
     *
     * @param projectId 项目id
     * @return WorkSpaceOrganizationTreeDTO
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询组织的工作空间")
    @GetMapping(value = "/organization/tree")
    public ResponseEntity<WorkSpaceOrganizationTreeDTO> queryOrganizationTree(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return new ResponseEntity<>(workSpaceService.queryOrganizationTree(projectId), HttpStatus.OK);
    }

    /**
     * 查询首次加载项目文章的树形结构
     *
     * @param projectId 项目id
     * @return WorkSpaceFirstTreeDTO
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询首次加载项目文章的树形结构")
    @GetMapping(value = "/first/tree")
    public ResponseEntity<WorkSpaceFirstTreeDTO> queryFirstTree(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return new ResponseEntity<>(workSpaceService.queryFirstTree(projectId,
                PageResourceType.PROJECT.getResourceType()),
                HttpStatus.OK);
    }

    /**
     * 查询项目文章的树形结构
     *
     * @param projectId 项目id
     * @param parentIds 工作空间目录父级ids
     * @return Map<Long, WorkSpaceTreeDTO>
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询项目文章的树形结构")
    @PostMapping(value = "/tree")
    public ResponseEntity<Map<Long, WorkSpaceTreeDTO>> queryByTree(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "工作空间目录父级ids", required = true)
            @RequestBody List<Long> parentIds) {
        return new ResponseEntity<>(workSpaceService.queryTree(projectId,
                parentIds,
                PageResourceType.PROJECT.getResourceType()),
                HttpStatus.OK);
    }

    /**
     * 查询文章父级的树形结构
     *
     * @param projectId 项目id
     * @param id        工作空间id
     * @return Map<Long, WorkSpaceTreeDTO>
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询文章父级的树形结构")
    @GetMapping(value = "/{id}/parent_tree")
    public ResponseEntity<Map<Long, WorkSpaceTreeDTO>> queryParentByTree(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "工作空间目录id", required = true)
            @PathVariable Long id) {
        return new ResponseEntity<>(workSpaceService.queryParentTree(projectId,
                id,
                PageResourceType.PROJECT.getResourceType()),
                HttpStatus.OK);
    }

    /**
     * 移动文章
     *
     * @param projectId        项目id
     * @param id               工作空间目录id
     * @param moveWorkSpaceDTO 移动信息
     * @return ResponseEntity
     */
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
}
