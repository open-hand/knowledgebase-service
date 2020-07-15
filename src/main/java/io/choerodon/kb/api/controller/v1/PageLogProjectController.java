package io.choerodon.kb.api.controller.v1;

import io.choerodon.swagger.annotation.Permission;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.api.vo.PageLogVO;
import io.choerodon.kb.app.service.PageLogService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by Zenger on 2019/5/17.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/page_log")
public class PageLogProjectController {

    @Autowired
    private PageLogService pageLogService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("查询页面操作日志")
    @GetMapping(value = "/{page_id}")
    public ResponseEntity<List<PageLogVO>> listByPageId(@ApiParam(value = "项目ID", required = true)
                                                        @PathVariable(value = "project_id") Long projectId,
                                                        @ApiParam(value = "组织id", required = true)
                                                        @RequestParam Long organizationId,
                                                        @ApiParam(value = "页面id", required = true)
                                                        @PathVariable(name = "page_id")
                                                        @Encrypt Long pageId) {
        return new ResponseEntity<>(pageLogService.listByPageId(organizationId, projectId, pageId), HttpStatus.OK);
    }

}
