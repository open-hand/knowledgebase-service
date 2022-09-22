package io.choerodon.kb.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.app.service.SecurityConfigService;
import io.choerodon.kb.domain.entity.SecurityConfig;
import io.choerodon.kb.domain.repository.SecurityConfigRepository;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;

import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;

/**
 * 知识库安全设置 管理 API
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
@RestController("securityConfigController.v1")
@RequestMapping("/v1/{organizationId}/security-configs")
public class SecurityConfigController extends BaseController {

    @Autowired
    private SecurityConfigRepository securityConfigRepository;
    @Autowired
    private SecurityConfigService securityConfigService;

    @ApiOperation(value = "知识库安全设置列表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/page")
    public ResponseEntity<Page<SecurityConfig>> pageSecurityConfig(
            @PathVariable("organizationId") Long organizationId,
            SecurityConfig queryParam,
            @ApiIgnore @SortDefault(value = SecurityConfig.FIELD_ID, direction = Sort.Direction.DESC) PageRequest pageRequest
    ) {
        Page<SecurityConfig> list = securityConfigRepository.pageAndSort(pageRequest, queryParam);
        return Results.success(list);
    }

    @ApiOperation(value = "知识库安全设置明细")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{id}")
    public ResponseEntity<SecurityConfig> findSecurityConfigById(
            @PathVariable("organizationId") Long organizationId,
            @PathVariable Long id
    ) {
        SecurityConfig securityConfig = securityConfigRepository.selectByPrimaryKey(id);
        return Results.success(securityConfig);
    }

    @ApiOperation(value = "创建知识库安全设置")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<SecurityConfig> createSecurityConfig(
            @PathVariable("organizationId") Long organizationId,
            @RequestBody SecurityConfig securityConfig
    ) {
        securityConfigService.create(organizationId, securityConfig);
        return Results.success(securityConfig);
    }

    @ApiOperation(value = "修改知识库安全设置")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PutMapping
    public ResponseEntity<SecurityConfig> updateSecurityConfig(
            @PathVariable("organizationId") Long organizationId,
            @RequestBody SecurityConfig securityConfig
    ) {
        securityConfigService.update(organizationId, securityConfig);
        return Results.success(securityConfig);
    }

    @ApiOperation(value = "删除知识库安全设置")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping
    public ResponseEntity<Void> removeSecurityConfig(
            @RequestBody SecurityConfig securityConfig
    ) {
        securityConfigService.remove(securityConfig);
        return Results.success();
    }

}
