package io.choerodon.kb.api.controller.v1;

import io.choerodon.swagger.annotation.Permission;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.api.vo.UserSettingVO;
import io.choerodon.kb.app.service.UserSettingService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * Created by HuangFuqiang@choerodon.io on 2019/07/02.
 * Email: fuqianghuang01@gmail.com
 */
@RestController
@RequestMapping(value = "/v1/organizations/{organization_id}/user_setting")
public class UserSettingOrganizationController {


    @Autowired
    private UserSettingService userSettingService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("组织层创建或更新个人设置")
    @PostMapping
    public ResponseEntity createOrUpdate(@ApiParam(value = "组织id", required = true)
                                         @PathVariable(name = "organization_id") Long organizationId,
                                         @ApiParam(value = "user setting VO", required = true)
                                         @RequestBody UserSettingVO userSettingVO) {
        userSettingService.createOrUpdate(organizationId, null, userSettingVO);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
