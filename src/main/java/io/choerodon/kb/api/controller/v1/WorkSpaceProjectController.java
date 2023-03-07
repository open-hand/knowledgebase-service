package io.choerodon.kb.api.controller.v1;

import java.util.List;
import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.api.vo.*;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.domain.repository.WorkSpaceRepository;
import io.choerodon.kb.infra.enums.FileSourceType;
import io.choerodon.kb.infra.utils.EncryptUtil;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.hzero.starter.keyencrypt.core.IEncryptionService;

/**
 * Created by Zenger on 2019/4/30.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/work_space")
public class WorkSpaceProjectController {

    private final WorkSpaceRepository workSpaceRepository;
    private final WorkSpaceService workSpaceService;
    private final IEncryptionService encryptionService;

    public WorkSpaceProjectController(
            WorkSpaceRepository workSpaceRepository,
            WorkSpaceService workSpaceService,
            IEncryptionService encryptionService
    ) {
        this.workSpaceRepository = workSpaceRepository;
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
        pageCreateVO.setTemplateFlag(false);
        return Results.success(workSpaceService.createWorkSpaceAndPage(organizationId, projectId, pageCreateVO, false));
    }


    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询项目下工作空间节点页面")
    @GetMapping(value = "/{id}")
    public ResponseEntity<WorkSpaceInfoVO> queryWorkSpaceInfo(@ApiParam(value = "项目id", required = true)
                                                              @PathVariable(value = "project_id") Long projectId,
                                                              @ApiParam(value = "工作空间目录id", required = true)
                                                              @PathVariable @Encrypt Long id,
                                                              @ApiParam(value = "组织id", required = true)
                                                              @RequestParam Long organizationId,
                                                              @ApiParam(value = "应用于全文检索时，对单篇文章，根据检索内容高亮内容")
                                                              @RequestParam(required = false) String searchStr) {
        WorkSpaceInfoVO infoVO = workSpaceRepository.queryWorkSpaceInfo(organizationId, projectId, id, searchStr, true,false);
        infoVO.setRoute(EncryptUtil.entryRoute(infoVO.getRoute(), encryptionService));
        if (infoVO.getWorkSpace() != null) {
            infoVO.getWorkSpace().setRoute(EncryptUtil.entryRoute(infoVO.getWorkSpace().getRoute(), encryptionService));
        }
        return Results.success(infoVO);
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
        WorkSpaceInfoVO infoVO = workSpaceService.updateWorkSpaceAndPage(organizationId, projectId, id, searchStr, pageUpdateVO, true,false);
        infoVO.setRoute(EncryptUtil.entryRoute(infoVO.getRoute(), encryptionService));
        return Results.success(infoVO);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "移动文章")
    @PostMapping(value = "/to_move/{id}")
    public ResponseEntity<Void> moveWorkSpace(@ApiParam(value = "项目id", required = true)
                                        @PathVariable(value = "project_id") Long projectId,
                                        @ApiParam(value = "组织id", required = true)
                                        @RequestParam Long organizationId,
                                        @ApiParam(value = "工作空间目录id", required = true)
                                        @PathVariable @Encrypt(ignoreValue = "0") Long id,
                                        @ApiParam(value = "移动信息", required = true)
                                        @RequestBody @Valid @Encrypt MoveWorkSpaceVO moveWorkSpaceVO) {
        workSpaceService.moveWorkSpace(organizationId, projectId, id, moveWorkSpaceVO);
        return Results.success();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询空间树形结构")
    @GetMapping(value = "/all_tree")
    public ResponseEntity<WorkSpaceTreeVO> queryAllTreeList(@ApiParam(value = "项目id", required = true)
                                                                @PathVariable(value = "project_id") Long projectId,
                                                                @ApiParam(value = "组织id", required = true)
                                                                @RequestParam Long organizationId,
                                                                @ApiParam(value = "知识库id", required = true)
                                                                @RequestParam @Encrypt Long baseId,
                                                                @ApiParam(value = "展开的空间id")
                                                                @RequestParam(required = false) @Encrypt Long expandWorkSpaceId,
                                                                @RequestParam(name = "exclude_type", required = false, defaultValue = "") String excludeType) {
        return Results.success(workSpaceRepository.queryAllTreeList(organizationId, projectId, baseId, expandWorkSpaceId, excludeType));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询项目下的所有空间")
    @GetMapping
    public ResponseEntity<List<WorkSpaceVO>> queryAllSpaceByOptions(@ApiParam(value = "项目id", required = true)
                                                                    @PathVariable(value = "project_id") Long projectId,
                                                                    @ApiParam(value = "组织id", required = true)
                                                                    @RequestParam Long organizationId,
                                                                    @ApiParam(value = "空间目录id", required = true)
                                                                    @RequestParam(required = false, defaultValue = "-1", value = "work_space_id") @Encrypt Long workSpaceId,
                                                                    @ApiParam(value = "知识库id", required = true)
                                                                    @RequestParam @Encrypt Long baseId,
                                                                    @RequestParam(name = "exclude_type", required = false, defaultValue = "") String excludeType) {

        return Results.success(workSpaceRepository.queryAllSpaceByOptions(organizationId, projectId, baseId, workSpaceId, excludeType));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询项目可用知识库下面的文档(敏捷工作项关联知识库用到了)")
    @GetMapping("/all_space")
    public ResponseEntity<List<WorkSpaceVO>> listAllSpace(@ApiParam(value = "项目id", required = true)
                                                          @PathVariable(value = "project_id") Long projectId,
                                                          @ApiParam(value = "组织id", required = true)
                                                          @RequestParam Long organizationId) {
        return Results.success(workSpaceRepository.listAllSpace(organizationId, projectId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "根据spaceIds查询空间列表")
    @PostMapping(value = "/query_by_space_ids")
    public ResponseEntity<List<WorkSpaceVO>> querySpaceByIds(@ApiParam(value = "项目id", required = true)
                                                             @PathVariable(value = "project_id") Long projectId,
                                                             @ApiParam(value = "space ids", required = true)
                                                             @RequestBody @Encrypt List<Long> spaceIdList) {
        return Results.success(workSpaceRepository.querySpaceByIds(projectId, spaceIdList));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "移除项目下工作空间及页面到回收站（管理员权限）")
    @PutMapping(value = "/remove/{id}")
    public ResponseEntity<Void> removeWorkSpaceAndPage(@ApiParam(value = "项目id", required = true)
                                                 @PathVariable(value = "project_id") Long projectId,
                                                 @ApiParam(value = "组织id", required = true)
                                                 @RequestParam Long organizationId,
                                                 @ApiParam(value = "工作空间目录id", required = true)
                                                 @PathVariable @Encrypt Long id) {
        workSpaceService.moveToRecycle(organizationId, projectId, id, true, true,false);
        return Results.success();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "移除项目下工作空间及页面到回收站（移除自己的空间）")
    @PutMapping(value = "/remove_my/{id}")
    public ResponseEntity<Void> removeWorkSpaceAndPageByMyWorkSpace(@ApiParam(value = "项目id", required = true)
                                                              @PathVariable(value = "project_id") Long projectId,
                                                              @ApiParam(value = "组织id", required = true)
                                                              @RequestParam Long organizationId,
                                                              @ApiParam(value = "工作空间目录id", required = true)
                                                              @PathVariable @Encrypt Long id) {
        workSpaceService.moveToRecycle(organizationId, projectId, id, false, true,false);
        return Results.success();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询最近更新的空间列表")
    @GetMapping(value = "/recent_update_list")
    public ResponseEntity<Page<WorkSpaceRecentInfoVO>> recentUpdateList(@ApiParam(value = "项目id", required = true)
                                                                        @PathVariable(value = "project_id") Long projectId,
                                                                        @ApiParam(value = "组织id", required = true)
                                                                        @RequestParam Long organizationId,
                                                                        @ApiParam(value = "知识库id", required = true)
                                                                        @RequestParam @Encrypt Long baseId,
                                                                        @ApiIgnore
                                                                        @ApiParam(value = "分页信息", required = true)
                                                                                PageRequest pageRequest) {
        return Results.success(workSpaceRepository.recentUpdateList(organizationId, projectId, baseId, pageRequest));
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
        return Results.success(workSpaceRepository.belongToBaseExist(null, projectId, id));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("项目层复制当前页面")
    @PostMapping("/clone_page")
    public ResponseEntity<WorkSpaceInfoVO> clonePage(@ApiParam(value = "项目id", required = true)
                                                     @PathVariable(value = "project_id") Long projectId,
                                                     @ApiParam(value = "组织id", required = true)
                                                     @RequestParam Long organizationId,
                                                     @ApiParam(value = "目录Id", required = true)
                                                     @RequestParam @Encrypt(ignoreValue = "0") Long workSpaceId,
                                                     @ApiParam(value = "parent_id", required = true)
                                                     @RequestParam(value = "parent_id") @Encrypt(ignoreValue = "0") Long parentId) {
        return Results.success(workSpaceService.clonePage(organizationId, projectId, workSpaceId, parentId, null));
    }


    @PostMapping("/upload")
    @ApiOperation("上传文件")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<WorkSpaceInfoVO> upload(@ApiParam(value = "项目id", required = true)
                                                  @PathVariable("project_id") Long projectId,
                                                  @ApiParam(value = "组织id", required = true)
                                                  @RequestParam Long organizationId,
                                                  @ApiParam(value = "页面创建vo", required = true)
                                                  @RequestBody PageCreateWithoutContentVO pageCreateWithoutContentVO) {
        pageCreateWithoutContentVO.setFileSourceType(FileSourceType.UPLOAD.getFileSourceType());
        pageCreateWithoutContentVO.setSourceType(ResourceLevel.PROJECT.value());
        pageCreateWithoutContentVO.setSourceId(projectId);
        return Results.success(workSpaceService.upload(projectId, organizationId, pageCreateWithoutContentVO, false));
    }

    @GetMapping("/upload/status")
    @ApiOperation("项目层查询文件上传状态")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<UploadFileStatusVO> queryUploadStatus(@ApiParam(value = "项目id", required = true)
                                                             @PathVariable("project_id") Long projectId,
                                                             @ApiParam(value = "组织id", required = true)
                                                             @RequestParam(value = "organization_id") Long organizationId,
                                                             @ApiParam(value = "页面创建vo", required = true)
                                                             @Encrypt @RequestParam(value = "ref_id") Long refId,
                                                             @RequestParam(value = "source_type") String sourceType) {
        return Results.success(workSpaceRepository.queryUploadStatus(projectId, organizationId, refId, sourceType));
    }


    @GetMapping("/folder/{id}")
    @ApiOperation("根据文件夹id，概览文件夹")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @CustomPageRequest
    public ResponseEntity<Page<WorkSpaceInfoVO>> queryFolder(@ApiParam(value = "项目id", required = true)
                                                             @PathVariable("project_id") Long projectId,
                                                             @ApiParam(value = "组织id", required = true)
                                                             @RequestParam("organization_id") Long organizationId,
                                                             @ApiParam(value = "文件夹id", required = true)
                                                             @PathVariable("id") @Encrypt Long id,
                                                             @ApiParam(value = "分页信息", required = true)
                                                             @SortDefault(value = "rank", direction = Sort.Direction.ASC) PageRequest pageRequest) {
        return Results.success(workSpaceRepository.pageQueryFolder(organizationId, projectId, id, pageRequest));
    }


    @PutMapping("/rename/{id}")
    @ApiOperation("workSpace的重命名")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<Void> renameWorkSpace(@ApiParam(value = "项目id", required = true)
                                                @PathVariable("project_id") Long projectId,
                                                @ApiParam(value = "组织id", required = true)
                                                @RequestParam("organization_id") Long organizationId,
                                                @ApiParam(value = "空间目录id", required = true)
                                                @PathVariable("id") @Encrypt Long id,
                                                @ApiParam(value = "新名称", required = true)
                                                @RequestParam(name = "new_name") String newName) {
        workSpaceService.renameWorkSpace(projectId, organizationId, id, newName);
        return Results.success();
    }


    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询平台预置的文档")
    @GetMapping("/default/template")
    public ResponseEntity<List<WorkSpaceVO>> queryDefaultTemplate(@ApiParam(value = "组织id", required = true)
                                                                  @PathVariable("project_id") Long projectId,
                                                                  @RequestParam(value = "params") String params) {
        return Results.success(workSpaceRepository.queryDefaultTemplate(0L, projectId, params));
    }


}
