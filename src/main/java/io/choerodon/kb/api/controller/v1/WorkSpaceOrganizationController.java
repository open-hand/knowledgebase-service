package io.choerodon.kb.api.controller.v1;

import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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
@RequestMapping(value = "/v1/organizations/{organization_id}/work_space")
public class WorkSpaceOrganizationController {

    private WorkSpaceService workSpaceService;

    public WorkSpaceOrganizationController(WorkSpaceService workSpaceService) {
        this.workSpaceService = workSpaceService;
    }

    /**
     * 组织下创建页面
     *
     * @param organizationId 组织id
     * @param pageCreateDTO  页面信息
     * @return PageDTO
     */
    @Permission(type = ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR,
                    InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation(value = "组织下创建页面")
    @PostMapping
    public ResponseEntity<PageDTO> create(
            @ApiParam(value = "组织id", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "页面信息", required = true)
            @RequestBody @Valid PageCreateDTO pageCreateDTO) {
        return new ResponseEntity<>(workSpaceService.create(organizationId,
                pageCreateDTO,
                PageResourceType.ORGANIZATION.getResourceType()), HttpStatus.CREATED);
    }

    /**
     * 查询组织下工作空间节点页面
     *
     * @param organizationId 组织id
     * @param id             工作空间目录id
     * @return PageDTO
     */
    @Permission(type = ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR,
                    InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation(value = "查询组织下工作空间节点页面")
    @GetMapping(value = "/{id}")
    public ResponseEntity<PageDTO> query(
            @ApiParam(value = "组织id", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "工作空间目录id", required = true)
            @PathVariable Long id) {
        return new ResponseEntity<>(workSpaceService.queryDetail(id), HttpStatus.OK);
    }

    /**
     * 更新组织下工作空间节点页面
     *
     * @param organizationId 组织id
     * @param id             工作空间目录id
     * @param pageUpdateDTO  页面信息
     * @return PageDTO
     */
    @Permission(type = ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR,
                    InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation(value = "更新组织下工作空间节点页面")
    @PutMapping(value = "/{id}")
    public ResponseEntity<PageDTO> update(@ApiParam(value = "组织id", required = true)
                                          @PathVariable(value = "organization_id") Long organizationId,
                                          @ApiParam(value = "工作空间目录id", required = true)
                                          @PathVariable Long id,
                                          @ApiParam(value = "空间信息", required = true)
                                          @RequestBody @Valid PageUpdateDTO pageUpdateDTO) {
        return new ResponseEntity<>(workSpaceService.update(
                organizationId,
                id,
                pageUpdateDTO,
                PageResourceType.ORGANIZATION.getResourceType()),
                HttpStatus.CREATED);
    }

    /**
     * 删除组织下工作空间节点页面（管理员权限）
     *
     * @param organizationId 组织id
     * @param id             工作空间目录id
     * @return ResponseEntity
     */
    @Permission(type = ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = " 删除组织下工作空间节点页面（管理员权限）")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity delete(@ApiParam(value = "组织id", required = true)
                                 @PathVariable(value = "organization_id") Long organizationId,
                                 @ApiParam(value = "工作空间目录id", required = true)
                                 @PathVariable Long id) {
        workSpaceService.delete(organizationId, id, PageResourceType.ORGANIZATION.getResourceType(), true);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 删除组织下工作空间节点页面（删除自己的空间）
     *
     * @param organizationId 组织id
     * @param id             工作空间目录id
     * @return ResponseEntity
     */
    @Permission(type = ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR,
                    InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation(value = " 删除组织下工作空间节点页面（删除自己的空间）")
    @DeleteMapping(value = "/delete_my/{id}")
    public ResponseEntity deleteMyWorkSpace(@ApiParam(value = "组织id", required = true)
                                            @PathVariable(value = "organization_id") Long organizationId,
                                            @ApiParam(value = "工作空间目录id", required = true)
                                            @PathVariable Long id) {
        workSpaceService.delete(organizationId, id, PageResourceType.ORGANIZATION.getResourceType(), false);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 查询首次加载组织文章的树形结构
     *
     * @param organizationId 组织id
     * @return WorkSpaceFirstTreeDTO
     */
    @Permission(type = ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR,
                    InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation(value = "查询首次加载组织文章的树形结构")
    @GetMapping(value = "/first/tree")
    public ResponseEntity<WorkSpaceFirstTreeDTO> queryFirstTree(
            @ApiParam(value = "组织id", required = true)
            @PathVariable(value = "organization_id") Long organizationId) {
        return new ResponseEntity<>(workSpaceService.queryFirstTree(organizationId,
                PageResourceType.ORGANIZATION.getResourceType()),
                HttpStatus.OK);
    }

    /**
     * 查询组织文章的树形结构
     *
     * @param organizationId 组织id
     * @param parentIds      工作空间目录父级ids
     * @return Map<Long ,   WorkSpaceTreeDTO>
     */
    @Permission(type = ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR,
                    InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation(value = "查询组织文章的树形结构")
    @PostMapping(value = "/tree")
    public ResponseEntity<Map<Long, WorkSpaceTreeDTO>> queryTree(
            @ApiParam(value = "组织id", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "工作空间目录父级ids", required = true)
            @RequestBody @NotNull List<Long> parentIds) {
        return new ResponseEntity<>(workSpaceService.queryTree(organizationId,
                parentIds,
                PageResourceType.ORGANIZATION.getResourceType()),
                HttpStatus.OK);
    }

    /**
     * 查询文章父级的树形结构
     *
     * @param organizationId 组织id
     * @param id             工作空间id
     * @return Map<Long ,   WorkSpaceTreeDTO>
     */
    @Permission(type = ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR,
                    InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation(value = "查询文章父级的树形结构")
    @GetMapping(value = "/{id}/parent_tree")
    public ResponseEntity<Map<Long, WorkSpaceTreeDTO>> queryParentTree(
            @ApiParam(value = "组织id", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "工作空间目录id", required = true)
            @PathVariable Long id) {
        return new ResponseEntity<>(workSpaceService.queryParentTree(organizationId,
                id,
                PageResourceType.ORGANIZATION.getResourceType()),
                HttpStatus.OK);
    }

    /**
     * 移动文章
     *
     * @param organizationId   组织id
     * @param id               工作空间目录id
     * @param moveWorkSpaceDTO 移动信息
     * @return ResponseEntity
     */
    @Permission(type = ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR,
                    InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation(value = "移动文章")
    @PostMapping(value = "/to_move/{id}")
    public ResponseEntity moveWorkSpace(@ApiParam(value = "组织id", required = true)
                                        @PathVariable(value = "organization_id") Long organizationId,
                                        @ApiParam(value = "工作空间目录id", required = true)
                                        @PathVariable Long id,
                                        @ApiParam(value = "移动信息", required = true)
                                        @RequestBody @Valid MoveWorkSpaceDTO moveWorkSpaceDTO) {
        workSpaceService.moveWorkSpace(organizationId,
                id,
                moveWorkSpaceDTO,
                PageResourceType.ORGANIZATION.getResourceType());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "迁移XWiki组织数据")
    @PostMapping(value = "/migration")
    public ResponseEntity migration(@ApiParam(value = "组织id", required = true)
                                    @PathVariable(value = "organization_id") Long organizationId,
                                    @ApiParam(value = "迁移信息")
                                    @RequestBody MigrationDTO migrationDTO) {
        workSpaceService.migration(migrationDTO,
                organizationId,
                PageResourceType.ORGANIZATION.getResourceType());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
