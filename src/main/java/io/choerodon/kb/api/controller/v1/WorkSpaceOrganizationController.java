package io.choerodon.kb.api.controller.v1;

import io.choerodon.core.domain.Page;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.app.service.impl.WorkSpaceServiceImpl;
import io.choerodon.kb.infra.utils.EncrtpyUtil;
import io.choerodon.mybatis.domain.AuditDomain;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.api.vo.*;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.tuple.Pair;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.hzero.starter.keyencrypt.core.IEncryptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Zenger on 2019/4/30.
 */
@RestController
@RequestMapping(value = "/v1/organizations/{organization_id}/work_space")
public class WorkSpaceOrganizationController {

    private WorkSpaceService workSpaceService;
    private IEncryptionService encryptionService;

    public WorkSpaceOrganizationController(WorkSpaceService workSpaceService, IEncryptionService encryptionService) {
        this.workSpaceService = workSpaceService;
        this.encryptionService = encryptionService;
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "组织下创建页面和空页面")
    @PostMapping
    public ResponseEntity<WorkSpaceInfoVO> createWorkSpaceAndPage(
            @ApiParam(value = "组织id", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "页面信息", required = true)
            @RequestBody @Valid @Encrypt PageCreateWithoutContentVO pageCreateVO) {
        return new ResponseEntity<>(workSpaceService.createWorkSpaceAndPage(organizationId, null, pageCreateVO), HttpStatus.CREATED);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
    @ApiOperation(value = "查询组织下工作空间节点页面")
    @GetMapping(value = "/{id}")
    public ResponseEntity<WorkSpaceInfoVO> query(
            @ApiParam(value = "组织id", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "工作空间目录id", required = true)
            @PathVariable @Encrypt Long id,
            @ApiParam(value = "应用于全文检索时，对单篇文章，根据检索内容高亮内容")
            @RequestParam(required = false) String searchStr) {
        //组织层设置成permissionLogin=true，因此需要单独校验权限
        workSpaceService.checkOrganizationPermission(organizationId);
        WorkSpaceInfoVO ws = workSpaceService.queryWorkSpaceInfo(organizationId, null, id, searchStr);
        ws.setRoute(EncrtpyUtil.entryRoute(ws.getRoute(),encryptionService));
        if (Objects.nonNull(ws.getWorkSpace())){
            ws.getWorkSpace().setRoute(EncrtpyUtil.entryRoute(ws.getWorkSpace().getRoute(), encryptionService));
        }
        return new ResponseEntity<>(ws, HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "更新组织下工作空间节点页面")
    @PutMapping(value = "/{id}")
    public ResponseEntity<WorkSpaceInfoVO> update(@ApiParam(value = "组织id", required = true)
                                                  @PathVariable(value = "organization_id") Long organizationId,
                                                  @ApiParam(value = "工作空间目录id", required = true)
                                                  @PathVariable @Encrypt Long id,
                                                  @ApiParam(value = "应用于全文检索时，对单篇文章，根据检索内容高亮内容")
                                                  @RequestParam(required = false) String searchStr,
                                                  @ApiParam(value = "空间信息", required = true)
                                                  @RequestBody @Valid PageUpdateVO pageUpdateVO) {
        WorkSpaceInfoVO ws = workSpaceService.updateWorkSpaceAndPage(organizationId, null, id, searchStr, pageUpdateVO);
        ws.setRoute(EncrtpyUtil.entryRoute(ws.getRoute(), encryptionService));
        return new ResponseEntity<>(ws, HttpStatus.CREATED);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "移动文章")
    @PostMapping(value = "/to_move/{id}")
    public ResponseEntity moveWorkSpace(@ApiParam(value = "组织id", required = true)
                                        @PathVariable(value = "organization_id") Long organizationId,
                                        @ApiParam(value = "工作空间目录id", required = true)
                                        @PathVariable @Encrypt(ignoreValue = "0") Long id,
                                        @ApiParam(value = "移动信息", required = true)
                                        @RequestBody @Valid @Encrypt MoveWorkSpaceVO moveWorkSpaceVO) {
        workSpaceService.moveWorkSpace(organizationId, null, id, moveWorkSpaceVO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
    @ApiOperation(value = "查询空间树形结构")
    @GetMapping(value = "/all_tree")
    public ResponseEntity<Map<String, Object>> queryAllTreeList(@ApiParam(value = "组织id", required = true)
                                                                             @PathVariable(value = "organization_id") Long organizationId,
                                                                             @ApiParam(value = "知识库的id")
                                                                             @RequestParam(required = false) @Encrypt Long baseId,
                                                                             @ApiParam(value = "展开的空间id")
                                                                             @RequestParam(required = false) @Encrypt Long expandWorkSpaceId) {
        //组织层设置成permissionLogin=true，因此需要单独校验权限
        workSpaceService.checkOrganizationPermission(organizationId);
        Map<String, Object> map = workSpaceService.queryAllTreeList(organizationId, null, expandWorkSpaceId,baseId);
        Map<String, Object> map1 = (Map<String, Object>)map.get(WorkSpaceServiceImpl.TREE_DATA);
        Map<String, WorkSpaceTreeVO> wsMap = Optional.of(map1)
                .map(map2 -> (Map)map2.get(WorkSpaceServiceImpl.ITEMS))
                .map(type -> (Map<Long, WorkSpaceTreeVO>)type)
                .map(ws -> ws.entrySet().stream()
                        .map(entry -> EncrtpyUtil.encryptWsMap(entry, encryptionService))
                        .collect(Collectors.toMap(Pair::getKey, Pair::getValue))).orElse(null);
        map1.put(WorkSpaceServiceImpl.ITEMS, wsMap);
        map1.put(WorkSpaceServiceImpl.ROOT_ID,
                encryptionService.encrypt(map1.get(WorkSpaceServiceImpl.ROOT_ID).toString(), EncrtpyUtil.BLANK_KEY));
        map.put(WorkSpaceServiceImpl.TREE_DATA, map1);
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询组织下的所有空间")
    @GetMapping
    public ResponseEntity<List<WorkSpaceVO>> queryAllSpaceByOptions(@ApiParam(value = "组织id", required = true)
                                                                    @PathVariable(value = "organization_id") Long organizationId,
                                                                    @RequestParam @Encrypt Long baseId) {
        return new ResponseEntity<>(workSpaceService.queryAllSpaceByOptions(organizationId, null,baseId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "移除组织下工作空间及页面（管理员权限）")
    @PutMapping(value = "/remove/{id}")
    public ResponseEntity removeWorkSpaceAndPage(@ApiParam(value = "组织id", required = true)
                                                 @PathVariable(value = "organization_id") Long organizationId,
                                                 @ApiParam(value = "工作空间目录id", required = true)
                                                 @PathVariable @Encrypt Long id) {
        workSpaceService.removeWorkSpaceAndPage(organizationId, null, id, true);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "移除组织下工作空间及页面（删除自己的空间）")
    @PutMapping(value = "/remove_my/{id}")
    public ResponseEntity removeWorkSpaceAndPageMyWorkSpace(@ApiParam(value = "组织id", required = true)
                                                            @PathVariable(value = "organization_id") Long organizationId,
                                                            @ApiParam(value = "工作空间目录id", required = true)
                                                            @PathVariable @Encrypt Long id) {
        workSpaceService.removeWorkSpaceAndPage(organizationId, null, id, false);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
    @ApiOperation(value = "查询最近更新的空间列表")
    @GetMapping(value = "/recent_update_list")
    public ResponseEntity<List<WorkSpaceRecentInfoVO>> recentUpdateList(@ApiParam(value = "组织id", required = true)
                                                                        @PathVariable(value = "organization_id") Long organizationId,
                                                                        @RequestParam @Encrypt Long baseId) {
        //组织层设置成permissionLogin=true，因此需要单独校验权限
        workSpaceService.checkOrganizationPermission(organizationId);
        return new ResponseEntity<>(workSpaceService.recentUpdateList(organizationId, null,baseId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询空间所属知识库是否存在")
    @GetMapping(value = "/belong_base_exist/{id}")
    public ResponseEntity<Boolean> belongToBaseDelete(@ApiParam(value = "组织id", required = true)
                                                      @RequestParam Long organizationId,
                                                      @ApiParam(value = "工作空间目录id", required = true)
                                                      @PathVariable @Encrypt Long id) {
        return new ResponseEntity<>(workSpaceService.belongToBaseExist(organizationId, null,id), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("组织层复制当前页面")
    @PostMapping("/clone_page")
    public ResponseEntity<WorkSpaceInfoVO> clonePage(@ApiParam(value = "组织id", required = true)
                                                     @RequestParam Long organizationId,
                                                     @ApiParam(value = "目录Id", required = true)
                                                     @RequestParam @Encrypt Long workSpaceId) {
        return new ResponseEntity<>(workSpaceService.clonePage(organizationId, null, workSpaceId), HttpStatus.OK);
    }


    @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
    @ApiOperation(value = "查询项目最近更新的空间列表")
    @GetMapping(value = "/recent_project_update_list")
    public ResponseEntity<Page<WorkBenchRecentVO>> selectProjectRecentList(@ApiParam(value = "项目id", required = true)
                                                                           @PathVariable(value = "organization_id") Long organizationId,
                                                                           @RequestParam(value = "projectId", required = false) Long projectId,
                                                                           @ApiParam(value = "组织id", required = true)
                                                                           @SortDefault(sort = AuditDomain.FIELD_LAST_UPDATE_DATE,
                                                                                   direction = Sort.Direction.DESC)
                                                                                   PageRequest pageRequest) {
        Map<String, String> map = new HashMap<>();
        map.put("lastUpdateDate", "kp.LAST_UPDATE_DATE");
        pageRequest.resetOrder("kp", map);
        return new ResponseEntity<>(workSpaceService.selectProjectRecentList(pageRequest, organizationId, projectId, false), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
    @ApiOperation(value = "查询个人最近更新的空间列表")
    @GetMapping(value = "/recent_project_update_list/self")
    public ResponseEntity<Page<WorkBenchRecentVO>> selectSelfRecentList(@ApiParam(value = "项目id", required = true)
                                                                        @PathVariable(value = "organization_id") Long organizationId,
                                                                        @RequestParam(value = "projectId", required = false) Long projectId,
                                                                        @ApiParam(value = "组织id", required = true)
                                                                        @SortDefault(sort = AuditDomain.FIELD_LAST_UPDATE_DATE,
                                                                                direction = Sort.Direction.DESC)
                                                                                PageRequest pageRequest) {
        Map<String, String> map = new HashMap<>();
        map.put("lastUpdateDate", "kp.LAST_UPDATE_DATE");
        pageRequest.resetOrder("kp", map);
        return new ResponseEntity<>(workSpaceService.selectProjectRecentList(pageRequest, organizationId, projectId, true), HttpStatus.OK);
    }
}
