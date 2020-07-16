package io.choerodon.kb.api.controller.v1;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.choerodon.swagger.annotation.Permission;
import io.choerodon.core.iam.ResourceLevel;

import io.choerodon.kb.api.vo.RecycleVO;
import io.choerodon.kb.api.vo.SearchDTO;
import io.choerodon.kb.app.service.RecycleService;

/**
 * @author: 25499
 * @date: 2020/1/3 10:23
 * @description:
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/recycle")
public class RecycleProjectController {
    @Autowired
    private RecycleService recycleService;


    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "分页查询回收站")
    @PostMapping(value = "/page_by_options")
    public ResponseEntity<Page<RecycleVO>> pageByOptions(@ApiParam(value = "项目id", required = true)
                                                             @PathVariable(value = "project_id") Long projectId,
                                                             @SortDefault PageRequest pageRequest,
                                                             @RequestBody(required = false) SearchDTO searchDTO,
                                                             @ApiParam(value = "组织id", required = true)
                                                             @RequestParam Long organizationId) {
        return new ResponseEntity<>(recycleService.pageList(projectId,organizationId, pageRequest, searchDTO), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "从回收站还原工作空间及页面")
    @PutMapping(value = "/restore/{id}")
    public ResponseEntity restoreWorkSpaceAndPage(@ApiParam(value = "项目id", required = true)
                                                  @PathVariable(value = "project_id") Long projectId,
                                                  @ApiParam(value = "组织id", required = true)
                                                  @RequestParam Long organizationId,
                                                  @ApiParam(value = "类型", required = true)
                                                  @RequestParam String type,
                                                  @PathVariable(value = "id") @Encrypt Long id,
                                                  @ApiParam(value = "所属知识库", required = false)
                                                   @RequestParam @Encrypt Long baseId) {
        recycleService.restoreWorkSpaceAndPage(organizationId, projectId, type,id,baseId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "从回收站彻底删除工作空间及页面")
    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity deleteWorkSpaceAndPage(@ApiParam(value = "项目id", required = true)
                                                 @PathVariable(value = "project_id") Long projectId,
                                                 @ApiParam(value = "组织id", required = true)
                                                 @RequestParam Long organizationId,
                                                 @ApiParam(value = "类型", required = true)
                                                 @RequestParam String type,
                                                 @PathVariable(value = "id") @Encrypt Long id) {
        recycleService.deleteWorkSpaceAndPage(organizationId, projectId, type,id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
