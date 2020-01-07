package io.choerodon.kb.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.core.annotation.Permission;
import io.choerodon.core.enums.ResourceType;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.kb.app.service.DataMigrateService;

/**
 * @author: 25499
 * @date: 2020/1/6 17:46
 * @description:
 */
@RestController
@RequestMapping("/v1/fix")
public class DataMigrateController {
    @Autowired
    private DataMigrateService dataMigrateService;

    @Permission(type = ResourceType.SITE, roles = {InitRoleCode.SITE_ADMINISTRATOR, InitRoleCode.SITE_DEVELOPER})
    @ApiOperation("迁移数据")
    @GetMapping
    public ResponseEntity fix() {
        dataMigrateService.migrateWorkSpace();
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
