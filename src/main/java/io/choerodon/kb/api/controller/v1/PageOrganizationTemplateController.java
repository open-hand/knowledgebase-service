package io.choerodon.kb.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.app.service.PageService;
import io.choerodon.swagger.annotation.Permission;

import org.hzero.starter.keyencrypt.core.Encrypt;

@RestController
@RequestMapping(value = "/v1/organizations/{organization_id}/page")
public class PageOrganizationTemplateController {

    @Autowired
    private PageService pageService;


    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("导入word文档为markdown数据")
    @PostMapping(value = "/import_word")
    public ResponseEntity<String> importDocx2Md(@ApiParam(value = "组织id", required = true)
                                                @PathVariable(value = "organization_id") Long organizationId,
                                                @ApiParam(value = "知识库id, 用于鉴权", required = true)
                                                @RequestParam @Encrypt Long baseId,
                                                @ApiParam(value = "父工作空间id, 用于鉴权", required = true)
                                                @RequestParam @Encrypt(ignoreValue = "0") Long parentWorkSpaceId,
                                                @ApiParam(value = "word文档", required = true)
                                                @RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(pageService.importDocx2Md(organizationId, null, baseId, parentWorkSpaceId, file, true), HttpStatus.OK);
    }
}
