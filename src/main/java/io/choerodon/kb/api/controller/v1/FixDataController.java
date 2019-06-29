package io.choerodon.kb.api.controller.v1;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.infra.dataobject.WorkSpaceDO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/v1/fix_data")
public class FixDataController {

    @Autowired
    private WorkSpaceService workSpaceService;

    @Permission(type = ResourceType.SITE, roles = {InitRoleCode.SITE_ADMINISTRATOR, InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "查询所有项目空间")
    @GetMapping(value = "/all_project_space")
    public ResponseEntity<List<WorkSpaceDO>> queryAllSpaceByProject() {
        return new ResponseEntity<>(workSpaceService.queryAllSpaceByProject(), HttpStatus.OK);
    }

}
