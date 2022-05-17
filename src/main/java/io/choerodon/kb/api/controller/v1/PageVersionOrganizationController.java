package io.choerodon.kb.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.api.vo.PageVersionCompareVO;
import io.choerodon.kb.api.vo.PageVersionInfoVO;
import io.choerodon.kb.api.vo.PageVersionVO;
import io.choerodon.kb.app.service.PageVersionService;
import io.choerodon.swagger.annotation.Permission;

/**
 * @author shinan.chen
 * @since 2019/5/17
 */
@RestController
@RequestMapping("/v1/organizations/{organization_id}/page_version")
public class PageVersionOrganizationController {

    @Autowired
    private PageVersionService pageVersionService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询页面的版本列表")
    @GetMapping("/list")
    public ResponseEntity<List<PageVersionVO>> listQuery(@ApiParam(value = "组织id", required = true)
                                                         @PathVariable("organization_id") Long organizationId,
                                                         @ApiParam(value = "页面id", required = true)
                                                         @RequestParam @Encrypt Long pageId) {
        return new ResponseEntity<>(pageVersionService.queryByPageId(organizationId, null, pageId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询版本内容")
    @GetMapping(value = "/{version_id}")
    public ResponseEntity<PageVersionInfoVO> queryById(@ApiParam(value = "组织id", required = true)
                                                       @PathVariable("organization_id") Long organizationId,
                                                       @ApiParam(value = "版本id", required = true)
                                                       @PathVariable("version_id") @Encrypt Long versionId,
                                                       @ApiParam(value = "页面id", required = true)
                                                       @RequestParam @Encrypt Long pageId) {
        return new ResponseEntity<>(pageVersionService.queryById(organizationId, null, pageId, versionId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "版本比较")
    @GetMapping(value = "/compare")
    public ResponseEntity<PageVersionCompareVO> compareVersion(@ApiParam(value = "组织id", required = true)
                                                               @PathVariable("organization_id") Long organizationId,
                                                               @ApiParam(value = "第一个版本id", required = true)
                                                               @RequestParam @Encrypt Long firstVersionId,
                                                               @ApiParam(value = "第二个版本id", required = true)
                                                               @RequestParam @Encrypt Long secondVersionId,
                                                               @ApiParam(value = "页面id", required = true)
                                                               @RequestParam @Encrypt Long pageId) {
        return new ResponseEntity<>(pageVersionService.compareVersion(organizationId, null, pageId, firstVersionId, secondVersionId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "版本回退")
    @GetMapping(value = "/rollback")
    public ResponseEntity rollbackVersion(@ApiParam(value = "组织id", required = true)
                                          @PathVariable("organization_id") Long organizationId,
                                          @ApiParam(value = "版本id", required = true)
                                          @RequestParam @Encrypt Long versionId,
                                          @ApiParam(value = "页面id", required = true)
                                          @RequestParam @Encrypt Long pageId) {
        pageVersionService.rollbackVersion(organizationId, null, pageId, versionId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

}
