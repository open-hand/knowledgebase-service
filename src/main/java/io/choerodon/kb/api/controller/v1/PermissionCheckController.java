package io.choerodon.kb.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.api.vo.permission.PermissionCheckVO;
import io.choerodon.kb.domain.service.PermissionCheckDomainService;
import io.choerodon.swagger.annotation.Permission;

import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 知识库鉴权 Controller
 * @author gaokuo.dai@zknow.com 2022-10-12
 */
@RestController
@RequestMapping("/v1/organizations/{organizationId}/permission")
public class PermissionCheckController extends BaseController {

    @Autowired
    private PermissionCheckDomainService permissionCheckDomainService;

    @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
    @ApiOperation("根据传入操作进行鉴权")
    @PostMapping(value = "/check")
    public ResponseEntity<List<PermissionCheckVO>> checkPermission(
            @ApiParam(value = "组织ID", required = true) @PathVariable(value = "organizationId") Long organizationId,
            @ApiParam(value = "项目ID") @RequestParam(required = false) Long projectId,
            @ApiParam(value = "控制对象类型") @RequestParam(required = false) String targetBaseType,
            @ApiParam(value = "控制对象类型") @RequestParam(required = false) String targetType,
            @ApiParam(value = "授权对象ID", required = true) @RequestParam @Encrypt Long targetValue,
            @RequestBody List<PermissionCheckVO> permissionsWaitCheck
    ) {
        return Results.success(
                this.permissionCheckDomainService.checkPermission(organizationId, projectId, targetBaseType, targetType, targetValue, permissionsWaitCheck)
        );
    }

}
