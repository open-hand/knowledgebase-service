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
        return new ResponseEntity<>(workSpaceService.queryDetail(organizationId, null, id), HttpStatus.OK);
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

    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR, InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation(value = "查询空间树形结构")
    @GetMapping(value = "/all_tree")
    public ResponseEntity<Map<String, Object>> queryAllTree(@ApiParam(value = "组织id", required = true)
                                                            @PathVariable(value = "organization_id") Long organizationId,
                                                            @ApiParam(value = "展开的空间id")
                                                            @RequestParam(required = false) Long expandWorkSpaceId) {
        return new ResponseEntity<>(workSpaceService.queryAllTree(organizationId, expandWorkSpaceId, PageResourceType.ORGANIZATION.getResourceType()), HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询组织下的所有空间")
    @GetMapping
    public ResponseEntity<List<WorkSpaceDTO>> queryAllSpaceByOptions(@ApiParam(value = "组织id", required = true)
                                                                     @PathVariable(value = "organization_id") Long organizationId) {
        return new ResponseEntity<>(workSpaceService.queryAllSpaceByOptions(organizationId, PageResourceType.ORGANIZATION.getResourceType()), HttpStatus.OK);
    }
}
