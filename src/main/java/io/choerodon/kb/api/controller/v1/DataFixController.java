package io.choerodon.kb.api.controller.v1;

import io.choerodon.core.iam.ResourceLevel;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.swagger.annotation.Permission;
import io.choerodon.kb.app.service.DataFixService;

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

    @Permission(level = ResourceLevel.SITE)
    @ApiOperation("迁移数据")
    @GetMapping
    public ResponseEntity fix() {
        dataFixService.fixData();
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
