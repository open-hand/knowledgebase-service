package io.choerodon.kb.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.app.service.DataFixService;
import io.choerodon.kb.infra.task.FixDataTask;
import io.choerodon.swagger.annotation.Permission;

import org.hzero.core.util.Results;

/**
 * @author: 25499
 * @date: 2020/1/6 17:46
 * @description:
 */
@RestController
@RequestMapping("/v1/fix")
public class DataFixController {

    @Autowired
    private DataFixService dataFixService;
    @Autowired
    private FixDataTask fixDataTask;

    @Permission(level = ResourceLevel.SITE)
    @ApiOperation("迁移数据")
    @GetMapping
    public ResponseEntity<Void> fix() {
        dataFixService.fixData();
        return Results.success();
    }

    @Permission(level = ResourceLevel.SITE)
    @ApiOperation("迁移数据")
    @GetMapping("/v2.2")
    public ResponseEntity<Void> fixV22() {
        fixDataTask.fixRouteAndPermission(null);
        return Results.success();
    }

}
