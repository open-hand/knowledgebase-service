package io.choerodon.kb.api.controller.v1;

import io.choerodon.kb.app.service.impl.WorkSpaceServiceImpl;
import io.choerodon.kb.infra.utils.EncrtpyUtil;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Zenger on 2019/4/30.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/work_space")
public class WorkSpaceProjectController {

    private WorkSpaceService workSpaceService;
    private IEncryptionService encryptionService;

    public WorkSpaceProjectController(WorkSpaceService workSpaceService, IEncryptionService encryptionService) {
        this.workSpaceService = workSpaceService;
        this.encryptionService = encryptionService;
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "项目下创建页面和空页面")
    @PostMapping
    public ResponseEntity<WorkSpaceInfoVO> createWorkSpaceAndPage(@ApiParam(value = "项目ID", required = true)
                                                                  @PathVariable(value = "project_id") Long projectId,
                                                                  @ApiParam(value = "组织id", required = true)
                                                                  @RequestParam Long organizationId,
                                                                  @ApiParam(value = "页面信息", required = true)
                                                                  @RequestBody @Valid @Encrypt PageCreateWithoutContentVO pageCreateVO) {
        return new ResponseEntity<>(workSpaceService.createWorkSpaceAndPage(organizationId, projectId, pageCreateVO), HttpStatus.CREATED);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询项目下工作空间节点页面")
    @GetMapping(value = "/{id}")
    public ResponseEntity<WorkSpaceInfoVO> queryWorkSpaceInfo(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "工作空间目录id", required = true)
            @PathVariable @Encrypt Long id,
            @ApiParam(value = "组织id", required = true)
            @RequestParam Long organizationId,
            @ApiParam(value = "应用于全文检索时，对单篇文章，根据检索内容高亮内容")
            @RequestParam(required = false) String searchStr) {
        WorkSpaceInfoVO infoVO = workSpaceService.queryWorkSpaceInfo(organizationId, projectId, id, searchStr);
        infoVO.setRoute(EncrtpyUtil.entryRoute(infoVO.getRoute(), encryptionService));
        if (Objects.nonNull(infoVO.getWorkSpace())){
            infoVO.getWorkSpace().setRoute(EncrtpyUtil.entryRoute(infoVO.getWorkSpace().getRoute(), encryptionService));
        }
        return new ResponseEntity<>(infoVO, HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "更新项目下工作空间节点页面")
    @PutMapping(value = "/{id}")
    public ResponseEntity<WorkSpaceInfoVO> updateWorkSpaceAndPage(@ApiParam(value = "项目id", required = true)
                                                                  @PathVariable(value = "project_id") Long projectId,
                                                                  @ApiParam(value = "组织id", required = true)
                                                                  @RequestParam Long organizationId,
                                                                  @ApiParam(value = "工作空间目录id", required = true)
                                                                  @PathVariable @Encrypt Long id,
                                                                  @ApiParam(value = "应用于全文检索时，对单篇文章，根据检索内容高亮内容")
                                                                  @RequestParam(required = false) String searchStr,
                                                                  @ApiParam(value = "空间信息", required = true)
                                                                  @RequestBody @Valid PageUpdateVO pageUpdateVO) {
        WorkSpaceInfoVO infoVO = workSpaceService.updateWorkSpaceAndPage(organizationId, projectId, id, searchStr, pageUpdateVO);
        infoVO.setRoute(EncrtpyUtil.entryRoute(infoVO.getRoute(), encryptionService));
        return new ResponseEntity<>(infoVO, HttpStatus.CREATED);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "移动文章")
    @PostMapping(value = "/to_move/{id}")
    public ResponseEntity moveWorkSpace(@ApiParam(value = "项目id", required = true)
                                        @PathVariable(value = "project_id") Long projectId,
                                        @ApiParam(value = "组织id", required = true)
                                        @RequestParam Long organizationId,
                                        @ApiParam(value = "工作空间目录id", required = true)
                                        @PathVariable @Encrypt(ignoreValue = "0") Long id,
                                        @ApiParam(value = "移动信息", required = true)
                                        @RequestBody @Valid @Encrypt MoveWorkSpaceVO moveWorkSpaceVO) {
        workSpaceService.moveWorkSpace(organizationId, projectId, id, moveWorkSpaceVO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询空间树形结构")
    @GetMapping(value = "/all_tree")
    public ResponseEntity<Map<String, Object>> queryAllTreeList(@ApiParam(value = "项目id", required = true)
                                                                             @PathVariable(value = "project_id") Long projectId,
                                                                             @ApiParam(value = "组织id", required = true)
                                                                             @RequestParam Long organizationId,
                                                                             @ApiParam(value = "知识库id", required = true)
                                                                             @RequestParam @Encrypt Long baseId,
                                                                             @ApiParam(value = "展开的空间id")
                                                                             @RequestParam(required = false) @Encrypt Long expandWorkSpaceId) {
        Map<String, Object> map = workSpaceService.queryAllTreeList(organizationId, projectId, expandWorkSpaceId,baseId);
        Map<String, Object> map1 = (Map<String, Object>)map.get(WorkSpaceServiceImpl.TREE_DATA);
        Map<String, WorkSpaceTreeVO> wsMap = Optional.of(map1)
                .map(map2 -> map2.get(WorkSpaceServiceImpl.ITEMS))
                .map(type -> (Map<Long, WorkSpaceTreeVO>)type)
                .map(ws -> ws.entrySet().stream()
                        .map(entry -> EncrtpyUtil.encryptWsMap(entry, encryptionService))
                        .collect(Collectors.toMap(Pair::getKey, Pair::getValue))).orElse(null);
        map1.put(WorkSpaceServiceImpl.ITEMS, wsMap);
        map1.put(WorkSpaceServiceImpl.ROOT_ID,
                encryptionService.encrypt(map1.get(WorkSpaceServiceImpl.ROOT_ID).toString(), ""));
        map.put(WorkSpaceServiceImpl.TREE_DATA, map1);
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询项目下的所有空间")
    @GetMapping
    public ResponseEntity<List<WorkSpaceVO>> queryAllSpaceByOptions(@ApiParam(value = "项目id", required = true)
                                                                    @PathVariable(value = "project_id") Long projectId,
                                                                    @ApiParam(value = "组织id", required = true)
                                                                    @RequestParam Long organizationId,
                                                                    @RequestParam @Encrypt Long baseId) {

        return new ResponseEntity<>(workSpaceService.queryAllSpaceByOptions(organizationId, projectId,baseId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询项目可用知识库下面的文档")
    @GetMapping("/all_space")
    public ResponseEntity<List<WorkSpaceVO>> listAllSpace(@ApiParam(value = "项目id", required = true)
                                                                    @PathVariable(value = "project_id") Long projectId,
                                                                    @ApiParam(value = "组织id", required = true)
                                                                    @RequestParam Long organizationId) {
        return new ResponseEntity<>(workSpaceService.listAllSpace(organizationId, projectId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "根据spaceIds查询空间列表")
    @PostMapping(value = "/query_by_space_ids")
    public ResponseEntity<List<WorkSpaceVO>> querySpaceByIds(@ApiParam(value = "项目id", required = true)
                                                             @PathVariable(value = "project_id") Long projectId,
                                                             @ApiParam(value = "space ids", required = true)
                                                             @RequestBody @Encrypt List<Long> spaceIdList) {
        return new ResponseEntity<>(workSpaceService.querySpaceByIds(projectId, spaceIdList), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "移除项目下工作空间及页面到回收站（管理员权限）")
    @PutMapping(value = "/remove/{id}")
    public ResponseEntity removeWorkSpaceAndPage(@ApiParam(value = "项目id", required = true)
                                                 @PathVariable(value = "project_id") Long projectId,
                                                 @ApiParam(value = "组织id", required = true)
                                                 @RequestParam Long organizationId,
                                                 @ApiParam(value = "工作空间目录id", required = true)
                                                 @PathVariable @Encrypt Long id) {
        workSpaceService.removeWorkSpaceAndPage(organizationId, projectId, id, true);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "移除项目下工作空间及页面到回收站（移除自己的空间）")
    @PutMapping(value = "/remove_my/{id}")
    public ResponseEntity removeWorkSpaceAndPageByMyWorkSpace(@ApiParam(value = "项目id", required = true)
                                                              @PathVariable(value = "project_id") Long projectId,
                                                              @ApiParam(value = "组织id", required = true)
                                                              @RequestParam Long organizationId,
                                                              @ApiParam(value = "工作空间目录id", required = true)
                                                              @PathVariable @Encrypt Long id) {
        workSpaceService.removeWorkSpaceAndPage(organizationId, projectId, id, false);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询最近更新的空间列表")
    @GetMapping(value = "/recent_update_list")
    public ResponseEntity<List<WorkSpaceRecentInfoVO>> recentUpdateList(@ApiParam(value = "项目id", required = true)
                                                                        @PathVariable(value = "project_id") Long projectId,
                                                                        @ApiParam(value = "组织id", required = true)
                                                                        @RequestParam Long organizationId,
                                                                        @RequestParam @Encrypt Long baseId) {
        return new ResponseEntity<>(workSpaceService.recentUpdateList(organizationId, projectId,baseId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询空间所属知识库是否存在")
    @GetMapping(value = "/belong_base_exist/{id}")
    public ResponseEntity<Boolean> belongToBaseDelete(@ApiParam(value = "项目id", required = true)
                                                                        @PathVariable(value = "project_id") Long projectId,
                                                                        @ApiParam(value = "组织id", required = true)
                                                                        @RequestParam Long organizationId,
                                                                         @ApiParam(value = "工作空间目录id", required = true)
                                                                         @PathVariable @Encrypt Long id) {
        return new ResponseEntity<>(workSpaceService.belongToBaseExist(null, projectId,id), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("项目层复制当前页面")
    @PostMapping("/clone_page")
    public ResponseEntity<WorkSpaceInfoVO> clonePage(@ApiParam(value = "项目id", required = true)
                                                     @PathVariable(value = "project_id") Long projectId,
                                                     @ApiParam(value = "组织id", required = true)
                                                     @RequestParam Long organizationId,
                                                     @ApiParam(value = "目录Id", required = true)
                                                     @RequestParam @Encrypt Long workSpaceId) {
        return new ResponseEntity<>(workSpaceService.clonePage(organizationId, projectId, workSpaceId), HttpStatus.OK);
    }
}
