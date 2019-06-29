package io.choerodon.kb.api.controller.v1;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.kb.api.dao.MigrationDTO;
import io.choerodon.kb.app.service.WikiMigrationService;
import io.choerodon.kb.infra.common.enums.PageResourceType;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Zenger on 2019/6/13.
 */
@RestController
@RequestMapping(value = "/v1")
public class WikiMigrationController {
    private WikiMigrationService wikiMigrationService;

    public WikiMigrationController(WikiMigrationService wikiMigrationService) {
        this.wikiMigrationService = wikiMigrationService;
    }

    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "迁移xwiki数据到猪齿鱼的组织和项目下")
    @GetMapping(value = "/site/xwiki_data/migration")
    public ResponseEntity migration() {
        wikiMigrationService.controllerMigration();
        return new ResponseEntity(HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "迁移XWiki组织数据")
    @PostMapping(value = "/organizations/{organization_id}/xwiki_data/migration")
    public ResponseEntity organizationLevelMigration(@ApiParam(value = "组织id", required = true)
                                                     @PathVariable(value = "organization_id") Long organizationId,
                                                     @ApiParam(value = "迁移信息")
                                                     @RequestBody MigrationDTO migrationDTO) {
        wikiMigrationService.levelMigration(migrationDTO,
                organizationId,
                PageResourceType.ORGANIZATION.getResourceType());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "迁移XWiki项目数据")
    @PostMapping(value = "/projects/{project_id}/xwiki_data/migration")
    public ResponseEntity projectLevelMigration(@ApiParam(value = "项目id", required = true)
                                                @PathVariable(value = "project_id") Long projectId,
                                                @ApiParam(value = "迁移信息")
                                                @RequestBody MigrationDTO migrationDTO) {
        wikiMigrationService.levelMigration(migrationDTO,
                projectId,
                PageResourceType.PROJECT.getResourceType());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "【修复接口】请勿调用")
    @GetMapping(value = "/site/xwiki_data/migration_fix_base_data")
    public ResponseEntity migrationFixData() {
        wikiMigrationService.controllerMigrationFix();
        return new ResponseEntity(HttpStatus.OK);
    }
}
