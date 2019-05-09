package io.choerodon.kb.api.controller.v1;

import java.util.List;
import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.kb.api.dao.PageCreateDTO;
import io.choerodon.kb.api.dao.PageDTO;
import io.choerodon.kb.api.dao.PageUpdateDTO;
import io.choerodon.kb.api.dao.WorkSpaceTreeDTO;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.infra.common.BaseStage;
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
                    BaseStage.ORGANIZATION_MEMBER})
    @ApiOperation(value = "组织下创建页面")
    @PostMapping
    public ResponseEntity<PageDTO> create(
            @ApiParam(value = "组织ID", required = true)
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
                    BaseStage.ORGANIZATION_MEMBER})
    @ApiOperation(value = "查询组织下工作空间节点页面")
    @GetMapping(value = "/{id}")
    public ResponseEntity<PageDTO> query(
            @ApiParam(value = "组织ID", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "工作空间目录ID", required = true)
            @PathVariable Long id) {
        return new ResponseEntity<>(workSpaceService.queryDetail(organizationId, id, PageResourceType.ORGANIZATION.getResourceType()), HttpStatus.OK);
    }

    /**
     * 更新组织下工作空间节点页面
     *
     * @param organizationId 组织id
     * @param id             工作空间目录ID
     * @param pageUpdateDTO  页面信息
     * @return PageDTO
     */
    @Permission(type = ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR,
                    BaseStage.ORGANIZATION_MEMBER})
    @ApiOperation(value = "更新组织下工作空间节点页面")
    @PutMapping(value = "/{id}")
    public ResponseEntity<PageDTO> update(@ApiParam(value = "组织ID", required = true)
                                          @PathVariable(value = "organization_id") Long organizationId,
                                          @ApiParam(value = "工作空间目录ID", required = true)
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
     * 删除组织下工作空间节点页面
     *
     * @param organizationId 组织id
     * @param id             工作空间目录ID
     * @return ResponseEntity
     */
    @Permission(type = ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR,
                    BaseStage.ORGANIZATION_MEMBER})
    @ApiOperation(value = " 删除组织下工作空间节点页面")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity delete(@ApiParam(value = "组织ID", required = true)
                                 @PathVariable(value = "organization_id") Long organizationId,
                                 @ApiParam(value = "工作空间目录ID", required = true)
                                 @PathVariable Long id) {
        workSpaceService.delete(organizationId, id, PageResourceType.ORGANIZATION.getResourceType());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 查询组织文章的树形结构
     *
     * @param organizationId 组织id
     * @param parentIds      工作空间目录父级ID
     * @return PageDTO
     */
    @Permission(type = ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR,
                    BaseStage.ORGANIZATION_MEMBER})
    @ApiOperation(value = "查询组织文章的树形结构")
    @PostMapping(value = "/tree")
    public ResponseEntity<List<WorkSpaceTreeDTO>> queryByTree(
            @ApiParam(value = "组织ID", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "工作空间目录父级ID", required = true)
            @RequestBody List<Long> parentIds) {
        return new ResponseEntity<>(workSpaceService.queryByTree(organizationId,
                parentIds,
                PageResourceType.ORGANIZATION.getResourceType()),
                HttpStatus.OK);
    }
}
